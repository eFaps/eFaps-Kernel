/*
 * Copyright 2003 - 2009 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db.databases.information;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;

/**
 * Class used to return information about a SQL table.
 *
 * @author tmo
 * @version $Id$
 */
public class TableInformation
{
  /**
   * Stores the map between a column name and the column information itself.
   *
   * @see #evaluateColInfo
   */
  final Map<String, ColumnInformation> colMap = new HashMap<String, ColumnInformation>();

  /**
   * Stores the map between a unique key name and the unique key information
   * itself.
   *
   * @see #evaluateUniqueKeys
   */
  final Map<String, UniqueKeyInformation> ukMap = new HashMap<String, UniqueKeyInformation>();

  /**
   * Stores the map between comma separated string of column names of a unique
   * key and the unique key itself.
   *
   * @see #evaluateUniqueKeys
   */
  final Map<String, UniqueKeyInformation> ukColMap = new HashMap<String, UniqueKeyInformation>();

  /**
   * Stores the map between a name of a foreign key and the information about
   * the foreign key itself. The name of all foreign keys are in upper case.
   *
   * @see #evaluateForeignKeys
   */
  final Map<String, ForeignKeyInformation> fkMap = new HashMap<String, ForeignKeyInformation>();

  /**
   * Initialize this table information instance. The constructor should not
   * called be directly. Instead method
   * {@link org.efaps.db.databases.AbstractDatabase#getTableInformation}
   * should be used (because it is possible that a specific implementation of
   * this class is needed, depending on the database vendor).<br/>
   * The mapping between the column names and the unique key is generated after
   * fetching all unique keys by calling method {@link #evaluateUniqueKeys}.
   *
   * @param _con          SQL connection
   * @param _tableName    name of SQL table for which the information
   *                      is searched
   * @throws SQLException if meta data of SQL table could not be evaluated
   *                      (fetched) from database
   * @see #evaluateColInfo      called with upper and lower case table name to
   *                            fetch information about the SQL table
   * @see #evaluateUniqueKeys   called with upper and lower case able name to
   *                            fetch information about unique keys of this SQL
   *                            table
   * @see #evaluateForeignKeys  called with upper and lowe case table name to
   *                            fetch information about foreign keys of this
   *                            SQL table
   * @see #ukColMap             unique key map by column names of unique key
   */
  public TableInformation(final Connection _con,
                          final String _tableName)
      throws SQLException
  {
    final DatabaseMetaData metaData = _con.getMetaData();
    evaluateColInfo(metaData, _tableName.toLowerCase());
    evaluateColInfo(metaData, _tableName.toUpperCase());

    // evaluate all unique keys
    evaluateUniqueKeys(metaData, _tableName.toLowerCase(), null);
    evaluateUniqueKeys(metaData, _tableName.toUpperCase(), null);
    // create map between column names and unique keys
    for (final UniqueKeyInformation uk : this.ukMap.values())  {
      this.ukColMap.put(uk.getColumnNames(), uk);
    }

    evaluateForeignKeys(metaData, _tableName.toLowerCase());
    evaluateForeignKeys(metaData, _tableName.toUpperCase());
  }

  /**
   * Evaluates for given name of SQL table the column information by using
   * the database meta data.
   *
   * @param _metaData   database meta data
   * @param _tableName  name of table which must be evaluated
   * @throws SQLException if column information could not be fetched
   * @see #colMap
   */
  protected void evaluateColInfo(final DatabaseMetaData _metaData,
                                 final String _tableName)
      throws SQLException
  {
    final ResultSet result = _metaData.getColumns(null, null, _tableName, "%");
    while (result.next())  {
      final String colName = result.getString("COLUMN_NAME").toUpperCase();
      final String typeName = result.getString("TYPE_NAME").toLowerCase();
      final Set<AbstractDatabase.ColumnType> colTypes = Context.getDbType().getReadColumnTypes(typeName);
      if (colTypes == null)  {
        throw new SQLException("read unknown column type '" + typeName + "'");
      }
      final int size = result.getInt("COLUMN_SIZE");
      final boolean isNullable = !"NO".equalsIgnoreCase(result.getString("IS_NULLABLE"));

      this.colMap.put(colName,
                      new ColumnInformation(colName, colTypes, size, isNullable));
    }
    result.close();
  }

  /**
   * Fetches all unique keys for this table. If a SQL statement is given, this
   * SQL statement is used instead of using the JDBC meta data methods. The SQL
   * select statement must define the two columns <code>INDEX_NAME</code> for
   * the real name of the unique key name and <code>COLUMN_NAME</code> for the
   * name of a column within the unique key. If more than one column is used
   * to define the unique key, one line for each column name with same index
   * name must be used.
   *
   * @param _metaData       database meta data
   * @param _tableName      name of table which must be evaluated
   * @param _sqlStatement   sql statement which must be executed if the JDBC
   *                        functionality does not work (or null if JDBC meta
   *                        data is used to fetch the unique keys)
   * @throws SQLException if unique keys could not be fetched
   * @see #ukMap      unique key map by name of unique key
   */
  protected void evaluateUniqueKeys(final DatabaseMetaData _metaData,
                                    final String _tableName,
                                    final String _sqlStatement)
      throws SQLException
  {
    final Statement stmt = (_sqlStatement == null)
                           ? null
                           : _metaData.getConnection().createStatement();
    final ResultSet result = (_sqlStatement == null)
                             ? _metaData.getIndexInfo(null, null, _tableName, true, false)
                             : stmt.executeQuery(_sqlStatement);
    while (result.next())  {
      final String ukName = result.getString("INDEX_NAME").toUpperCase();
      final String colName = result.getString("COLUMN_NAME").toUpperCase();
      final UniqueKeyInformation ukInfo = this.ukMap.get(ukName);
      if (ukInfo == null)  {
        this.ukMap.put(ukName,
                       new UniqueKeyInformation(ukName, colName));
      } else  {
        ukInfo.appendColumnName(colName);
      }
    }

    result.close();
    if (stmt != null)  {
      stmt.close();
    }
  }

  /**
   * Fetches all foreign keys for this table.
   *
   * @param _metaData   database meta data
   * @param _tableName  name of table which must be evaluated
   * @throws SQLException if foreign keys could not be fetched
   * @see #fkMap
   */
  protected void evaluateForeignKeys(final DatabaseMetaData _metaData,
                                    final String _tableName)
      throws SQLException
  {
    final ResultSet result = _metaData.getImportedKeys(null, null, _tableName);
    while (result.next())  {
      final String fkName = result.getString("FK_NAME").toUpperCase();
      final String colName = result.getString("FKCOLUMN_NAME");
      final String refTableName = result.getString("PKTABLE_NAME");
      final String refColName = result.getString("PKCOLUMN_NAME");
      final boolean cascade = (result.getInt("DELETE_RULE") == DatabaseMetaData.importedKeyCascade);
     this.fkMap.put(fkName,
                     new ForeignKeyInformation(fkName, colName, refTableName, refColName, cascade));
    }
  }

  /**
   * Returns for given column name (of the SQL table) the column information.
   * If the column does not exists in the table, a <code>null</code> is
   * returned.<br/>
   * The name of given column is searched independently of upper and / or
   * lower case.
   *
   * @param _columnName   name of column for which the column information is
   *                      searched
   * @return column information for given column name; or <code>null</code>
   *         if column does not exists in the table
   * @see #colMap
   */
  public ColumnInformation getColInfo(final String _columnName)
  {
    return (_columnName != null)
           ? this.colMap.get(_columnName.toUpperCase())
           : null;
  }

  /**
   * Returns for given name of unique key (of the SQL table) the information
   * about the unique key, or if the given name of unique key is not defined,
   * a <code>null</code> is returned.<br/>
   * The name of the given unique key is searched independently of upper and /
   * or lower case.
   *
   * @param _ukName   name of unique key which is searched
   * @return unique key information
   * @see #ukMap
   */
  public UniqueKeyInformation getUKInfo(final String _ukName)
  {
    return (_ukName != null)
           ? this.ukMap.get(_ukName.toUpperCase())
           : null;
  }

  /**
   * Returns for given name of unique key (of the SQL table) the information
   * about the unique key, or if the given name of unique key is not defined,
   * a <code>null</code> is returned.<br/>
   * The name of the given unique key is searched independently of upper and /
   * or lower case.
   *
   * @param _ukColName  name of column names for which the unique key is
   *                    searched
   * @return unique key information
   * @see #ukColMap
   */
  public UniqueKeyInformation getUKInfoByColNames(final String _ukColName)
  {
    return (_ukColName != null)
           ? this.ukColMap.get(_ukColName.toUpperCase())
           : null;
  }

  /**
   * Returns for given name of foreign key (of the SQL table) the information
   * about the foreign key, or if the given name of foreign key is not defined,
   * a <code>null</code> is returned.<br/>
   * The name of the given foreign key is searched independently of upper and /
   * or lower case.
   *
   * @param _fkName   name of foreign key which is searched
   * @return foreign key information
   * @see #fkMap
   */
  public ForeignKeyInformation getFKInfo(final String _fkName)
  {
    return (_fkName != null)
           ? this.fkMap.get(_fkName.toUpperCase())
           : null;
  }

  /**
   * Returns the string representation of this class instance. The
   * information includes {@link #colMap}, {@link #ukMap} and {@link #fkMap}.
   */
  @Override
  public String toString()
  {
    return new ToStringBuilder(this)
          .append("columns", this.colMap.values())
          .append("unique keys", this.ukMap.values())
          .append("foreign keys", this.fkMap.values())
          .toString();
  }
}

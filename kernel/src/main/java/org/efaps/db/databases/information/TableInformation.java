/*
 * Copyright 2003-2008 The eFaps Team
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
   * Initialize this table information instance.
   *
   * @param _con          SQL connection
   * @param _tableName    name of SQL table for which the information
   *                      is searched
   * @throws SQLException
   * @see #evaluateColInfo  called with upper and lower case table name to
   *                        fetch information about the SQL table
   */
  public TableInformation(final Connection _con,
                          final String _tableName)
      throws SQLException
  {
    final DatabaseMetaData metaData = _con.getMetaData();
    evaluateColInfo(metaData, _tableName.toLowerCase());
    evaluateColInfo(metaData, _tableName.toUpperCase());
    evaluateUniqueKeys(metaData, _tableName.toLowerCase());
    evaluateUniqueKeys(metaData, _tableName.toUpperCase());
  }

  /**
   * Evaluates for given name of SQL table the column information by using
   * the database meta data.
   *
   * @param _metaData   database meta data
   * @param _tableName  name of table which must be evaluated
   * @throws SQLException
   * @see #colMap
   */
  protected void evaluateColInfo(final DatabaseMetaData _metaData,
                                 final String _tableName)
      throws SQLException
  {
    final ResultSet result = _metaData.getColumns(null, null, _tableName, "%");
    while (result.next())  {
      final String colName = result.getString("COLUMN_NAME").toUpperCase();
      final String typeName = result.getString("TYPE_NAME");
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
   * Fetches all unique keys for this table.
   *
   * @param _metaData   database meta data
   * @param _tableName  name of table which must be evaluated
   * @throws SQLException
   * @see #ukMap
   */
  protected void evaluateUniqueKeys(final DatabaseMetaData _metaData,
                                    final String _tableName)
      throws SQLException
  {
    final ResultSet result = _metaData.getIndexInfo(null, null, _tableName, true, false);
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
   */
  public UniqueKeyInformation getUKInfo(final String _ukName)
  {
    return (_ukName != null)
           ? this.ukMap.get(_ukName.toUpperCase())
           : null;
  }

  /**
   * Returns the string representation of this class instance. The
   * information includes {@link #colMap} and {@link #ukMap}.
   */
  @Override
  public String toString()
  {
    return new ToStringBuilder(this)
          .append("columns", this.colMap.values())
          .append("unique Keys", this.ukMap.values())
          .toString();
  }
}

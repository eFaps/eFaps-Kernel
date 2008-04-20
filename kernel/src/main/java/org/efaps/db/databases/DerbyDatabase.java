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

package org.efaps.db.databases;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class DerbyDatabase extends AbstractDatabase  {

  //////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DerbyDatabase.class);

  /**
   * SQL Select statement for all foreign keys and constraints.
   *
   * @see #deleteAll
   */
  private static final String SELECT_ALL_KEYS
          = "select t.TABLENAME, c.CONSTRAINTNAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLEID=c.TABLEID "
                    + "and c.TYPE='F'";

  /**
   * SQL Select statement for all views.
   *
   * @see #deleteAll
   */
  private static final String SELECT_ALL_VIEWS
          = "select t.TABLENAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLETYPE='V'";

  /**
   * SQL Select statement for all tables.
   *
   * @see #deleteAll
   */
  private static final String SELECT_ALL_TABLES
          = "select t.TABLENAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    +"and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLETYPE='T'";


  //////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @todo specificy real column type
   */
  public DerbyDatabase()  {
    addMapping(ColumnType.INTEGER,      "bigint",     "bigint");
//    this.columnMap.put(ColumnType.REAL,         "real");
    addMapping(ColumnType.STRING_SHORT, "char",       "char");
    addMapping(ColumnType.STRING_LONG,  "varchar",    "varchar");
    addMapping(ColumnType.DATETIME,     "timestamp",  "timestamp");
    addMapping(ColumnType.BLOB,         "blob(2G)",   "blob(2G)");
    addMapping(ColumnType.CLOB,         "clob(2G)",   "clob(2G)");
    addMapping(ColumnType.BOOLEAN,      "smallint",   "smallint");
  }

  @Override
  public String getCurrentTimeStamp()  {
    return "current_timestamp";
  }

  //////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * This is the Derby specific implementation of an all deletion. Following
   * order is used to remove all eFaps specific information:
   * <ul>
   * <li>remove all foreign keys of the user</li>
   * <li>remove all views of the user</li>
   * <li>remove all tables of the user</li>
   * </ul>
   * Attention! If application specific tables, views or contraints are defined,
   * this database objects are also removed!
   *
   * @param _con  sql connection
   * @throws SQLException
   */
  @Override
  public void deleteAll(final Connection _con) throws SQLException  {

    Statement stmtSel = _con.createStatement();
    Statement stmtExec = _con.createStatement();

    try  {
    // remove all foreign keys
      if (LOG.isInfoEnabled())  {
        LOG.info("Remove all Foreign Keys");
      }
      ResultSet rs = stmtSel.executeQuery(SELECT_ALL_KEYS);
      while (rs.next())  {
        String tableName = rs.getString(1);
        String constrName = rs.getString(2);
        if (LOG.isDebugEnabled())  {
          LOG.debug("  - Table '" + tableName + "' Constraint '" + constrName + "'");
        }
        stmtExec.execute("alter table " + tableName + " drop constraint " + constrName);
      }
      rs.close();

      // remove all views
      if (LOG.isInfoEnabled())  {
        LOG.info("Remove all Views");
      }
      rs = stmtSel.executeQuery(SELECT_ALL_VIEWS);
      while (rs.next())  {
        String viewName = rs.getString(1);
        if (LOG.isDebugEnabled())  {
          LOG.debug("  - View '" + viewName + "'");
        }
        stmtExec.execute("drop view " + viewName);
      }
      rs.close();

      // remove all tables
      if (LOG.isInfoEnabled())  {
        LOG.info("Remove all Tables");
      }
      rs = stmtSel.executeQuery(SELECT_ALL_TABLES);
      while (rs.next())  {
        String tableName = rs.getString(1);
        if (LOG.isDebugEnabled())  {
          LOG.debug("  - Table '" + tableName + "'");
        }
        stmtExec.execute("drop table " + tableName);
      }
      rs.close();
    } finally  {
      stmtSel.close();
      stmtExec.close();
    }
  }

  /**
   * For the derby database, an eFaps sql table is created in this steps:
   * <ul>
   * <li>sql table itself with column <code>ID</code> and unique key on the
   *     column is created</li>
   * <li>if the table is an autoincrement table (parent table is
   *     <code>null</code>, the column <code>ID</code> is set as autoincrement
   *     column</li>
   * <li>if no parent table is defined, the foreign key to the parent table is
   *     automatically set</li>
   * </ul>
   *
   * @throws SQLException if the table could not be created
   */
  @Override
  public void createTable(final Connection _con, final String _table,
          final String _parentTable) throws SQLException  {

    Statement stmt = _con.createStatement();

    try  {

      // create table itself
      StringBuilder cmd = new StringBuilder();
      cmd.append("create table ").append(_table).append(" (")
         .append("  ID bigint not null");

      // autoincrement
      if (_parentTable == null)  {
        cmd.append(" generated always as identity (start with 1, increment by 1)");
      }

      cmd.append(",")
         .append("  constraint ").append(_table).append("_UK_ID unique(ID)");

      // foreign key to parent sql table
      if (_parentTable != null)  {
        cmd.append(",")
           .append("constraint ").append(_table).append("_FK_ID ")
           .append("  foreign key(ID) ")
           .append("  references ").append(_parentTable).append("(ID)");
      }

      cmd.append(")");
      stmt.executeUpdate(cmd.toString());

    } finally  {
      stmt.close();
    }
  }

  /**
   * Adds a column to a SQL table. The method overrides the original method,
   * because Derby does not allow for <code>NOT NULL</code> columns that no
   * default value is defined. Is such column is created, the default value for
   * real and integer is <code>0</code>, for datetime, short and long string
   * a zero length string.
   *
   * @param _con            SQL connection
   * @param _tableName      name of table to update
   * @param _columnName     column to add
   * @param _columnType     type of column to add
   * @param _defaultValue   default value of the column (or null if not
   *                        specified)
   * @param _length         length of column to add (or 0 if not specified)
   * @param _isNotNull      <i>true</i> means that the column has no
   *                        <code>null</code> values
   * @throws SQLException if the column could not be added to the tables
   */
  @Override
  public void addTableColumn(final Connection _con,
                             final String _tableName,
                             final String _columnName,
                             final ColumnType _columnType,
                             final String _defaultValue,
                             final int _length,
                             final boolean _isNotNull)
      throws SQLException  {

    String defaultValue = _defaultValue;

    if (_isNotNull && (defaultValue == null))  {
      switch (_columnType)  {
        case INTEGER:
        case REAL:
          defaultValue = "0";
          break;
        case DATETIME:
        case STRING_LONG:
        case STRING_SHORT:
          defaultValue = "''";
          break;
      }
    }

    super.addTableColumn(_con, _tableName, _columnName, _columnType,
        defaultValue, _length, _isNotNull);
  }

  /**
   * Adds a new unique key to given table name, but only if for the column a
   * <code>NOT NULL</code> is defined.
   *
   * @param _con            SQL connection
   * @param _tableName      name of table for which the unique key must be
   *                        created
   * @param _uniqueKeyName  name of unique key
   * @param _columns        comma separated list of column names for which the
   *                        unique key is created
   * @throws SQLException if the unique key could not be created
   */
  @Override
  public void addUniqueKey(final Connection _con,
                           final String _tableName,
                           final String _uniqueKeyName,
                           final String _columns)
      throws SQLException  {

    if (_columns.indexOf(',') < 0)  {
      final ResultSet rs = _con.getMetaData().getColumns(null, null, _tableName, _columns);
      rs.next();
      // unique key is only allowed if 'not null' for the column is defined!
      if (rs.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls)  {
        super.addUniqueKey(_con, _tableName, _uniqueKeyName, _columns);
      }
// TODO: else??? what to do instead of unique key?
      rs.close();
    } else  {
      super.addUniqueKey(_con, _tableName, _uniqueKeyName, _columns);
    }
  }

  /**
   * @return always <i>true</i> because supported by Derby database
   */
  @Override
  public boolean supportsGetGeneratedKeys()  {
    return true;
  }

  /**
   * @return always <i>true</i> because supported by PostgreSQL database
   */
  @Override
  public boolean supportsBinaryInputStream()  {
    return false;
  }
}
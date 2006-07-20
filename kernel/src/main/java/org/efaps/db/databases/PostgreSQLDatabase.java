/*
 * Copyright 2006 The eFaps Team
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JoinRowSet;

public class PostgreSQLDatabase extends AbstractDatabase  {

// TODO: specificy real column type
  public PostgreSQLDatabase() throws ClassNotFoundException, IllegalAccessException  {
    super();

    this.columnMap.put(ColumnType.INTEGER,      "bigint");
    this.columnMap.put(ColumnType.REAL,         "real");
    this.columnMap.put(ColumnType.STRING_SHORT, "char");
    this.columnMap.put(ColumnType.STRING_LONG,  "varchar");
    this.columnMap.put(ColumnType.DATETIME,     "timestamp");
    this.columnMap.put(ColumnType.BLOB,         "bytea");
    this.columnMap.put(ColumnType.CLOB,         "text");
    this.columnMap.put(ColumnType.BOOLEAN,      "boolean");
  }

  public String getCurrentTimeStamp()  {
    return "current_timestamp";
  }

  /**
   * This is the PostgreSQL specific implementation of an all deletion.
   * Following order is used to remove all eFaps specific information:
   * <ul>
   * <li>remove all tables of the user</li>
   * </ul>
   * <p>The table are dropped with cascade, so all depending sequences, views
   * etc. are also dropped automatically.
   * </p>
   * Attention! If application specific tables, views or contraints are defined,
   * this database objects are also removed!
   *
   * @param _con  sql connection
   * @throws SQLException
   */
  public void deleteAll(final Connection _con) throws SQLException  {

    Statement stmtSel = _con.createStatement();
    Statement stmtExec = _con.createStatement();

    try  {
System.out.println("Remove Tables");
      // remove all tables
      // cascade drops all views, sequences
//    print("Remove Tables");
      ResultSet rs = stmtSel.executeQuery(
          "select c.RELNAME "
              + "from PG_CLASS c,PG_ROLES r "
              + "where c.RELKIND='r' "
                  + "and c.RELOWNER=r.oid "
                  + "and r.ROLNAME=user "
              + "order by c.RELNAME"
      );
      while (rs.next())  {
        String tableName = rs.getString(1);
System.out.println("  - Table '" + tableName + "'");
//      print("  - Table '"+table+"'");
        stmtExec.execute("drop table " + tableName + " cascade");
      }
      rs.close();
    } finally  {
      stmtSel.close();
      stmtExec.close();
    }
  }

  /**
   * The method tests, if the given view exists.
   *
   * @param _con      sql connection
   * @param _viewName name of view to test
   * @return <i>true</i> if view exists, otherwise <i>false</i>
   */
  public boolean existsView(final Connection _con,
                            final String _viewName) throws SQLException  {
    boolean ret = false;

    Statement stmt = _con.createStatement();

    try  {
      ResultSet rs = stmt.executeQuery(
          "select c.RELNAME "
              + "from PG_CLASS c,PG_ROLES r "
              + "where c.RELNAME='" + _viewName.toLowerCase() + "'"
                  + "and c.RELKIND='v' "
                  + "and c.RELOWNER=r.oid "
                  + "and r.ROLNAME=user"
      );
      if (rs.next())  {
        ret = true;
      }
      rs.close();
    } finally  {
      stmt.close();
    }
    return ret;
  }

  /**
   * For the PostgreSQL database, an eFaps sql table is created in this steps:
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
  public void createTable(final Connection _con, final String _table,
          final String _parentTable) throws SQLException  {

    Statement stmt = _con.createStatement();

    try  {

      // create table itself
      StringBuilder cmd = new StringBuilder();
      cmd.append("create table ").append(_table).append(" (");

      // autoincrement
      if (_parentTable == null)  {
        cmd.append("ID bigserial");
      } else  {
        cmd.append("ID bigint");
      }

      cmd.append(",")
         .append("constraint ").append(_table).append("_UK_ID unique(ID)");

      // foreign key to parent sql table
      if (_parentTable != null)  {
        cmd.append(",")
           .append("constraint ").append(_table).append("_FK_ID ")
           .append("foreign key(ID) ")
           .append("references ").append(_parentTable).append("(ID)");
      }

      cmd.append(") without OIDS;");
      stmt.executeUpdate(cmd.toString());

    } finally  {
      stmt.close();
    }
  }

  /**
   * A new id for given column of a sql table is returned (with
   * sequences!).
   *
   * @param _con          sql connection
   * @param _table        sql table for which a new id must returned
   * @param _column       sql table column for which a new id must returned
   * @throws SQLException if a new id could not be retrieved
   */
  public long getNewId(final Connection _con, final String _table,
          final String _column)  throws SQLException  {

    long ret = 0;
    Statement stmt = _con.createStatement();

    try  {

      StringBuilder cmd = new StringBuilder();
      cmd.append("select nextval('").append(_table).append("_").append(_column)
          .append("_SEQ')");

      ResultSet rs = stmt.executeQuery(cmd.toString());
      if (rs.next())  {
        ret = rs.getLong(1);
      }
      rs.close();

    } finally  {
      stmt.close();
    }
    return ret;
  }
}
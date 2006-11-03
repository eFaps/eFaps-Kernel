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

public class OracleDatabase extends AbstractDatabase  {

  public OracleDatabase()  {
    this.columnMap.put(ColumnType.INTEGER,      "number");
    this.columnMap.put(ColumnType.REAL,         "number");
    this.columnMap.put(ColumnType.STRING_SHORT, "varchar2");
    this.columnMap.put(ColumnType.STRING_LONG,  "varchar2");
    this.columnMap.put(ColumnType.DATETIME,     "date");
    this.columnMap.put(ColumnType.BLOB,         "blob");
    this.columnMap.put(ColumnType.CLOB,         "clob");
    this.columnMap.put(ColumnType.BOOLEAN,      "number");
  }

  /**
   * The method returns string <code>sysdate</code> which let Oracle set the
   * timestamp automatically from the database server.
   *
   * @return string <code>sysdate</code>
   */
  public String getCurrentTimeStamp()  {
    return "sysdate";
  }

  /**
   * This is the Oralce specific implementation of an all deletion. Following
   * order is used to remove all eFaps specific information of the current
   * Oracle database user:
   * <ul>
   * <li>remove all user views</li>
   * <li>remove all user tables</li>
   * <li>remove all user sequences</li>
   * </ul>
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

      // remove all views
//    print("Remove Views");
      ResultSet rs = stmtSel.executeQuery("select VIEW_NAME from USER_VIEWS");
      while (rs.next())  {
        String viewName = rs.getString(1);
//      print("  - View '"+table+"'");
        stmtExec.execute("drop view " + viewName);
      }
      rs.close();

      // remove all tables
//    print("Remove Tables");
      rs = stmtSel.executeQuery("select TABLE_NAME from USER_TABLES");
      while (rs.next())  {
        String tableName = rs.getString(1);
//      print("  - Table '"+table+"'");
        stmtExec.execute("drop table " + tableName + " cascade constraints");
      }
      rs.close();

      // remove all sequencesw
      rs = stmtSel.executeQuery("select SEQUENCE_NAME from USER_SEQUENCES");
      while (rs.next())  {
        String seqName = rs.getString(1);
        stmtExec.execute("drop sequence " + seqName);
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
            "select VIEW_NAME "
                + "from USER_VIEWS "
                + "where VIEW_NAME='" + _viewName.toUpperCase() + "'"
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
   * For the database from vendor Oracle, an eFaps sql table with autoincrement
   * is created in this steps:
   * <ul>
   * <li>sql table itself with column <code>ID</code> and unique key on the
   *     column is created</li>
   * <li>sequence with same name of table and suffix <code>_SEQ</code> is
   *     created</li>
   * <li>trigger with same name of table and suffix <code>_TRG</code> is
   *     created. The trigger sets automatically the column <code>ID</code>
   *     with the next value of the sequence</li>
   * </ul>
   * An eFaps sql table without autoincrement, but with parent table is created
   * in this steps:
   * <ul>
   * <li>sql table itself with column <code>ID</code> and unique key on the
   *     column is created</li>
   * <li>the foreign key to the parent table is automatically set</li>
   * </ul>
   *
   * @throws SQLException if the table, sequence or trigger could not be
   *                      created
   */
  public void createTable(final Connection _con, final String _table,
          final String _parentTable) throws SQLException  {

    Statement stmt = _con.createStatement();

    try  {

      // create table itself
      StringBuilder cmd = new StringBuilder();
      cmd.append("create table ").append(_table).append(" (")
         .append("  ID number not null,")
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

      if (_parentTable == null)  {
        // create sequence
        cmd = new StringBuilder();
        cmd.append("create sequence ").append(_table).append("_SEQ ")
           .append("  increment by 1 ")
           .append("  start with 1 ")
           .append("  nocache");
        stmt.executeUpdate(cmd.toString());

        // create trigger for autoincrement
        cmd = new StringBuilder();
        cmd.append("create trigger ").append(_table).append("_TRG")
           .append("  before insert on ").append(_table)
           .append("  for each row ")
           .append("begin")
           .append("  select ").append(_table).append("_SEQ.nextval ")
           .append("      into :new.ID from dual;")
           .append("end;");
        stmt.executeUpdate(cmd.toString());
      }
    } finally  {
      stmt.close();
    }
  }

  /**
   * @return always <i>true</i> because supported by Oracle database
   */
  public boolean supportsGetGeneratedKeys()  {
    return true;
  }
}
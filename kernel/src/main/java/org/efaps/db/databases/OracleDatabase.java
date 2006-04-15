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
 */

package org.efaps.db.databases;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JoinRowSet;

public class OracleDatabase extends AbstractDatabase  {

  public OracleDatabase() throws ClassNotFoundException, IllegalAccessException  {
    super(
        (Class < CachedRowSet >)Class.forName("oracle.jdbc.rowset.OracleCachedRowSet"),
        (Class < JoinRowSet >)Class.forName("oracle.jdbc.rowset.OracleJoinRowSet")
    );

    this.columnMap.put(ColumnType.INTEGER,      "number");
    this.columnMap.put(ColumnType.REAL,         "number");
    this.columnMap.put(ColumnType.STRING_SHORT, "varchar2");
    this.columnMap.put(ColumnType.STRING_LONG,  "varchar2");
    this.columnMap.put(ColumnType.DATETIME,     "date");
    this.columnMap.put(ColumnType.BLOB,         "blob");
    this.columnMap.put(ColumnType.CLOB,         "clob");
  }

  public String getCurrentTimeStamp()  {
    return "sysdate";
  }

  /**
   * This is the Oralce specific implementation of an all deletion. Following
   * order is used to remove all eFaps specific information:
   * <ul>
   * <li>remove all views of the user</li>
   * <li>remove all tables of the user</li>
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

    } finally  {
      stmtSel.close();
      stmtExec.close();
    }
  }
}
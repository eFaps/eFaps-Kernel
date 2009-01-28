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

package org.efaps.db.databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The database driver is used for Oracle databases starting with version 9i.
 * The differnce to {@link OracleDatabase} is, that this class supports auto
 * generated keys.
 *
 * @author tmo
 * @version $Id$
 */
public class OracleDatabaseWithAutoSequence extends OracleDatabase  {

  /////////////////////////////////////////////////////////////////////////////
  // constructor / desctructors

  public OracleDatabaseWithAutoSequence()  {
    super();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

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
   * The creation of the table itself is done by calling the inherited method
   * {@link OracleDatabase#createTable}
   *
   * @param _con        sql connection
   * @param _table      name of the table to create
   * @param _parenTable name of the parent table
   * @throws SQLException if trigger could not be created
   * @see OracleDatabase#createTable
   */
  public void createTable(final Connection _con, final String _table,
          final String _parentTable) throws SQLException  {

    super.createTable(_con, _table, _parentTable);
    
    if (_parentTable == null)  {
      Statement stmt = _con.createStatement();

      try  {
        // create trigger for autoincrement
        StringBuilder cmd = new StringBuilder();
        cmd.append("create trigger ").append(_table).append("_TRG")
           .append("  before insert on ").append(_table)
           .append("  for each row ")
           .append("begin")
           .append("  select ").append(_table).append("_SEQ.nextval ")
           .append("      into :new.ID from dual;")
           .append("end;");
        stmt.executeUpdate(cmd.toString());
      } finally  {
        stmt.close();
      }
    }
  }

  /**
   * This implementation of the vendor specific database driver implements the
   * auto generated keys. So always <i>true</i> is returned.
   *
   * @return always <i>true</i> because supported by Oracle database
   */
  public boolean supportsGetGeneratedKeys()  {
    return true;
  }

  /**
   * @return always <i>true</i> because supported by Derby database
   */
  public boolean supportsMultiGeneratedKeys()  {
    return true;
  }

  /**
   * This method normally returns for given table and column a new id. Because
   * this database driver support auto generated keys, an SQL exception is
   * always thrown.
   *
   * @param _con          sql connection
   * @param _table        sql table for which a new id must returned
   * @param _column       sql table column for which a new id must returned
   * @throws SQLException always, because this database driver supports auto
   *                      generating keys
   */
  public long getNewId(final Connection _con, final String _table,
          final String _column)  throws SQLException  {
    throw new SQLException("The database driver uses auto generated keys and "
                           + "a new id could not returned without making "
                           + "a new insert.");
  }
}
/*
 * Copyright 2003 - 2012 The eFaps Team
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
 * The difference to {@link OracleDatabase} is, that this class supports auto
 * generated keys.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class OracleDatabaseWithAutoSequence
    extends OracleDatabase
{
    /**
     * For the database from vendor Oracle, an eFaps SQL table with
     * auto increment is created in this steps:
     * <ul>
     * <li>SQL table itself with column <code>ID</code> and unique key on the
     *     column is created</li>
     * <li>sequence with same name of table and suffix <code>_SEQ</code> is
     *     created</li>
     * <li>trigger with same name of table and suffix <code>_TRG</code> is
     *     created. The trigger sets automatically the column <code>ID</code>
     *     with the next value of the sequence</li>
     * </ul>
     * An eFaps SQL table without auto increment, but with parent table is
     * created in this steps:
     * <ul>
     * <li>SQL table itself with column <code>ID</code> and unique key on the
     *     column is created</li>
     * <li>the foreign key to the parent table is automatically set</li>
     * </ul>
     * The creation of the table itself is done by calling the inherited method
     * {@link OracleDatabase#createTable}
     *
     * @param _con            SQL connection
     * @param _table          name of the table to create
     * @param _parentTable    name of the parent table
     * @return this vendor specific DB definition
     * @throws SQLException if trigger could not be created
     * @see OracleDatabase#createTable(Connection, String, String)
     */
/* TODO
    @Override()
    public OracleDatabaseWithAutoSequence createTable(final Connection _con,
                                                      final String _table,
                                                      final String _parentTable)
        throws SQLException
    {
        super.createTable(_con, _table, _parentTable);

        if (_parentTable == null)  {
            final Statement stmt = _con.createStatement();

            try  {
                // create trigger for auto increment
                final StringBuilder cmd = new StringBuilder()
                    .append("create trigger ").append(_table).append("_TRG")
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
        return this;
    }
*/


    /**
     * {@inheritDoc}
     */
    @Override
    public OracleDatabase defineTableAutoIncrement(final Connection _con,
                                                       final String _table)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            // create sequence
            StringBuilder cmd = new StringBuilder()
                .append("create sequence ").append(_table).append("_SEQ ")
                .append("  increment by 1 ")
                .append("  start with 1 ")
                .append("  nocache");
            stmt.executeUpdate(cmd.toString());

            // create trigger for auto increment
            cmd = new StringBuilder()
                .append("create trigger ").append(_table).append("_TRG")
                .append("  before insert on ").append(_table)
                .append("  for each row ")
                .append("begin")
                .append("  select ").append(_table).append("_SEQ.nextval ")
                .append("      into :new.ID from dual;")
                .append("end;");
            stmt.executeUpdate(cmd.toString());

        } finally {
            stmt.close();
        }
        return this;
    }

    /**
     * This implementation of the vendor specific database driver implements
     * the auto generated keys. So always <i>true</i> is returned.
     *
     * @return always <i>true</i> because supported by Oracle database
     */
    @Override
    public boolean supportsGetGeneratedKeys()
    {
        return true;
    }

    /**
     * @return always <i>true</i> because supported by Oracle database
     */
    @Override
    public boolean supportsMultiGeneratedKeys()
    {
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
   * @return nothing, because SQLException is always thrown
   * @throws SQLException always, because this database driver supports auto
   *                      generating keys
   */
    @Override
    public long getNewId(final Connection _con,
                         final String _table,
                         final String _column)
        throws SQLException
    {
        throw new SQLException("The database driver uses auto generated keys and "
                               + "a new id could not returned without making "
                               + "a new insert.");
    }
}

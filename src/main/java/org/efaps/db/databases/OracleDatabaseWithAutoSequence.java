/*
 * Copyright 2003 - 2016 The eFaps Team
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
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The database driver is used for Oracle databases starting with version 9i.
 * The difference to {@link OracleDatabase} is, that this class supports auto
 * generated keys.
 *
 * @author The eFaps Team
 *
 */
public class OracleDatabaseWithAutoSequence
    extends OracleDatabase
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OracleDatabaseWithAutoSequence.class);


    /**
     * {@inheritDoc}
     * @throws SQLException
     */
    @Override
    public boolean isConnected(final Connection _connection)
        throws SQLException
    {
        boolean ret = false;
        final Statement stmt = _connection.createStatement();
        try {
            OracleDatabaseWithAutoSequence.LOG.debug("Checking if connected");
            final ResultSet resultset = stmt
                        .executeQuery("select product from product_component_version where product like 'Oracle%'");
            ret = resultset.next();
            resultset.close();
            if (ret) {
                // check if this database is rally one with AutoSequence by checking for a trigger.
                final ResultSet resultset2 = stmt
                            .executeQuery(" select * from user_triggers where trigger_name = 'T_CMABSTRACT_TRG'");
                ret = resultset2.next();
                resultset2.close();
            }
        } finally {
            stmt.close();
        }
        return ret;
    }


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
     * {@inheritDoc}
     */
    @Override
    public OracleDatabase defineTableAutoIncrement(final Connection _con,
                                                       final String _table)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            final String tableName = getName4DB(_table, 25);
            // create sequence
            StringBuilder cmd = new StringBuilder()
                .append("create sequence ").append(tableName).append("_SEQ")
                .append("  increment by 1 ")
                .append("  start with 1 ")
                .append("  nocache");
            stmt.executeUpdate(cmd.toString());

            // create trigger for auto increment
            cmd = new StringBuilder()
                .append("create trigger ").append(tableName).append("_TRG")
                .append("  before insert on ").append(_table)
                .append("  for each row ")
                .append("begin")
                .append("  select ").append(tableName).append("_SEQ.nextval ")
                .append("      into :new.ID from dual;")
                .append("end;");
            stmt.executeUpdate(cmd.toString());

        } catch (final EFapsException e) {
            throw new SQLException(e);
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

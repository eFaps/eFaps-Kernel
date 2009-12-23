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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The database driver is used for Oracle databases starting with version 9i.
 * It does not support auto generated keys. To generate a new id number,
 * the Oracle sequences are used.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class OracleDatabase
    extends AbstractDatabase<OracleDatabase>
{


    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OracleDatabase.class);

    /**
     * The instance is initialised and sets the columns map used for this
     * database.
     */
    public OracleDatabase()
    {
        super();
        addMapping(ColumnType.INTEGER,      "number",     "null", "number");
        addMapping(ColumnType.REAL,         "number",     "null", "number");
        addMapping(ColumnType.STRING_SHORT, "nvarchar2",  "null", "nvarchar2");
        addMapping(ColumnType.STRING_LONG,  "nvarchar2",  "null", "nvarchar2");
        addMapping(ColumnType.DATETIME,     "timestamp",  "null", "timestamp");
        addMapping(ColumnType.BLOB,         "blob",       "null", "blob");
        addMapping(ColumnType.CLOB,         "nclob",      "null", "nclob");
        addMapping(ColumnType.BOOLEAN,      "number",     "null", "number");
    }

    @Override()
    public int getMaxExpressions()
    {
        return 1000;
    }

    /**
     * The method returns string <code>sysdate</code> which let Oracle set the
     * timestamp automatically from the database server.
     *
     * @return string <code>sysdate</code>
     */
    @Override()
    public String getCurrentTimeStamp()
    {
        return "sysdate";
    }

    /**
     * This is the Oracle specific implementation of an all deletion. Following
     * order is used to remove all eFaps specific information of the current
     * Oracle database user:
     * <ul>
     * <li>remove all user views</li>
     * <li>remove all user tables</li>
     * <li>remove all user sequences</li>
     * </ul>
     * Attention! If application specific tables, views or constraints are
     * defined, this database objects are also removed!
     *
     * @param _con  sql connection
     * @throws SQLException if delete of the views, tables or sequences failed
     */
    @Override()
    public void deleteAll(final Connection _con)
        throws SQLException
    {
        final Statement stmtSel = _con.createStatement();
        final Statement stmtExec = _con.createStatement();

        try  {
            // remove all views
            if (OracleDatabase.LOG.isInfoEnabled())  {
                OracleDatabase.LOG.info("Remove all Views");
            }
            ResultSet rs = stmtSel.executeQuery("select VIEW_NAME from USER_VIEWS");
            while (rs.next())  {
                final String viewName = rs.getString(1);
                if (OracleDatabase.LOG.isDebugEnabled())  {
                    OracleDatabase.LOG.debug("  - View '" + viewName + "'");
                }
                stmtExec.execute("drop view " + viewName);
            }
            rs.close();

            // remove all tables
            if (OracleDatabase.LOG.isInfoEnabled())  {
                OracleDatabase.LOG.info("Remove all Tables");
            }
            rs = stmtSel.executeQuery("select TABLE_NAME from USER_TABLES");
            while (rs.next())  {
                final String tableName = rs.getString(1);
                if (OracleDatabase.LOG.isDebugEnabled())  {
                    OracleDatabase.LOG.debug("  - Table '" + tableName + "'");
                }
                stmtExec.execute("drop table " + tableName + " cascade constraints");
            }
            rs.close();

            // remove all sequences
            if (OracleDatabase.LOG.isInfoEnabled())  {
                OracleDatabase.LOG.info("Remove all Sequences");
            }
            rs = stmtSel.executeQuery("select SEQUENCE_NAME from USER_SEQUENCES");
            while (rs.next())  {
                final String seqName = rs.getString(1);
                if (OracleDatabase.LOG.isDebugEnabled())  {
                    OracleDatabase.LOG.debug("  - Sequence '" + seqName + "'");
                }
                stmtExec.execute("drop sequence " + seqName);
            }
            rs.close();
        } finally  {
            stmtSel.close();
            stmtExec.close();
        }
    }

    /**
     * For the database from vendor Oracle, an eFaps SQL table
     * is created in this steps:
     * <ul>
     * <li>sql table itself with column <code>ID</code> and unique key on the
     *     column is created</li>
     * <li>sequence with same name of table and suffix <code>_SEQ</code> is
     *     created</li>
     * </ul>
     * An eFaps sql table with parent table is created in this steps:
     * <ul>
     * <li>sql table itself with column <code>ID</code> and unique key on the
     *     column is created</li>
     * <li>the foreign key to the parent table is automatically set</li>
     * </ul>
     *
     * @param _con          SQL connection
     * @param _table        name of the table to create
     * @param _parentTable  name of the parent table
     * @throws SQLException if the table or sequence could not be created
     */
    @Override()
    public OracleDatabase createTable(final Connection _con,
                                      final String _table/*,
                                      final String _parentTable*/)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();

        try  {

            // create table itself
            final StringBuilder cmd = new StringBuilder()
                .append("create table ").append(_table).append(" (")
                .append("  ID number not null,")
                .append("  constraint ").append(_table).append("_UK_ID unique(ID)");

            // foreign key to parent SQL table
/* TODO
            if (_parentTable != null)  {
                cmd.append(",")
                    .append("constraint ").append(_table).append("_FK_ID ")
                    .append("  foreign key(ID) ")
                    .append("  references ").append(_parentTable).append("(ID)");
            }
*/
            cmd.append(")");
            stmt.executeUpdate(cmd.toString());

/* TODO
            if (_parentTable == null)  {
                // create sequence
                cmd = new StringBuilder()
                    .append("create sequence ").append(_table).append("_SEQ ")
                    .append("  increment by 1 ")
                    .append("  start with 1 ")
                    .append("  nocache");
                stmt.executeUpdate(cmd.toString());
            }
*/
        } finally  {
            stmt.close();
        }

        return this;
    }

    /**
     * TODO: implement
     */
    @Override()
    public OracleDatabase defineTableAutoIncrement(final Connection _con,
                                                   final String _table)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * A new id for given column of a SQL table is returned (with
     * sequences!).
     *
     * @param _con          sql connection
     * @param _table        sql table for which a new id must returned
     * @param _column       sql table column for which a new id must returned
     * @return new ID of the used sequence
     * @throws SQLException if a new id could not be retrieved
     */
    @Override()
    public long getNewId(final Connection _con,
                         final String _table,
                         final String _column)
        throws SQLException
    {
        long ret = 0;
        final Statement stmt = _con.createStatement();

        try  {
            final StringBuilder cmd = new StringBuilder()
                .append("select ").append(_table).append("_SEQ.nextval from DUAL");

            final ResultSet rs = stmt.executeQuery(cmd.toString());
            if (rs.next())  {
                ret = rs.getLong(1);
            }
            rs.close();
        } finally  {
            stmt.close();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OracleDatabase createSequence(final Connection _con,
                                         final String _name,
                                         final long _startValue)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public OracleDatabase deleteSequence(final Connection _con,
                                         final String _name)
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsSequence(final Connection _con,
                                  final String _name)
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextSequence(final Connection _con,
                             final String _name)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OracleDatabase setSequence(final Connection _con,
                                      final String _name,
                                      final long _value)
        throws SQLException
    {
        throw new Error("not implemented");
    }
}

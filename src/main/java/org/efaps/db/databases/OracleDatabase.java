/*
 * Copyright 2003 - 2011 The eFaps Team
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.efaps.db.databases.information.TableInformation;
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
     * Select statement to select all unique keys for current logged in
     * PostgreSQL database user.
     *
     * @see #initTableInfoUniqueKeys(Connection, String, Map)
     */
    private static final String SQL_UNIQUE_KEYS = "select "
            + "a.index_name as INDEX_NAME, "
            + "a.table_name as TABLE_NAME, "
            + "b.column_name as COLUMN_NAME, "
            + "b.position as ORDINAL_POSITION "
        + "from "
            + "user_constraints a, "
            + "user_cons_columns b "
        + "where "
            + "a.constraint_type='U' "
            + "and a.index_name = b.constraint_name";

    /**
     * Select statement for all foreign keys for current logged in PostgreSQL
     * database user.
     *
     * @see #initTableInfoForeignKeys(Connection, String, Map)
     */
    private static final String SQL_FOREIGN_KEYS = "select "
            + "ucc1.TABLE_NAME as TABLE_NAME, "
            + "uc.constraint_name as FK_NAME, "
            + "ucc1.column_name as FKCOLUMN_NAME, "
            + "case "
                    + "when uc.delete_rule='NO ACTION' then '" + DatabaseMetaData.importedKeyNoAction + "' "
                    + "when uc.delete_rule='CASCASE' then '" + DatabaseMetaData.importedKeyCascade + "' "
                    + "else '' end as DELETE_RULE, "
            + "ucc2.table_name as PKTABLE_NAME, "
            + "ucc2.column_name as PKCOLUMN_NAME "
        + "from "
            + "user_constraints uc, "
            + "user_cons_columns ucc1, "
            + "user_cons_columns ucc2 "
        + "where "
            + "uc.constraint_name = ucc1.constraint_name "
            + "and uc.r_constraint_name = ucc2.constraint_name "
            + "and ucc1.POSITION = ucc2.POSITION "
            + "and uc.constraint_type = 'R'";

    /**
     * The instance is initialised and sets the columns map used for this
     * database.
     */
    public OracleDatabase()
    {
        super();
        addMapping(ColumnType.INTEGER,      "number",     "null", "number");
        addMapping(ColumnType.REAL,         "number",     "null", "number");
        addMapping(ColumnType.STRING_SHORT, "varchar2",   "null", "varchar2");
        addMapping(ColumnType.STRING_LONG,  "varchar2",   "null", "varchar2");
        addMapping(ColumnType.DATETIME,     "timestamp",  "null", "timestamp", "timestamp(6)");
        addMapping(ColumnType.BLOB,         "blob",       "null", "blob");
        addMapping(ColumnType.CLOB,         "nclob",      "null", "nclob");
        addMapping(ColumnType.BOOLEAN,      "number",     "null", "number");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected(final Connection _connection)
    {
        // FIXME must be implemented
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public OracleDatabase deleteView(final Connection _con,
                                     final String _name)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * For the database from vendor Oracle. An eFaps SQL table
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
     * @throws SQLException if the table or sequence could not be created
     * @return this
     */
    @Override
    public OracleDatabase createTable(final Connection _con,
                                      final String _table)
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
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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

    /**
     * Overwrites the original method to specify SQL statement
     * {@link #SQL_UNIQUE_KEYS} as replacement because the JDBC driver for
     * PostgreSQL does not handle matching table names.
     *
     * @param _con          SQL connection
     * @param _sql          SQL statement (not used)
     * @param _cache4Name   map used to fetch depending on the table name the
     *                      related table information
     * @throws SQLException if unique keys could not be fetched
     * @see #SQL_UNIQUE_KEYS
     */
    @Override
    protected void initTableInfoUniqueKeys(final Connection _con,
                                           final String _sql,
                                           final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        super.initTableInfoUniqueKeys(_con, OracleDatabase.SQL_UNIQUE_KEYS, _cache4Name);
    }

    /**
     * Overwrites the original method to specify SQL statement
     * {@link #SQL_FOREIGN_KEYS} as replacement because the JDBC driver for
     * PostgreSQL does not handle matching table names.
     *
     * @param _con          SQL connection
     * @param _sql          SQL statement (not used)
     * @param _cache4Name   map used to fetch depending on the table name the
     *                      related table information
     * @throws SQLException if foreign keys could not be fetched
     * @see #SQL_FOREIGN_KEYS
     */
    @Override
    protected void initTableInfoForeignKeys(final Connection _con,
                                            final String _sql,
                                            final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        super.initTableInfoForeignKeys(_con, OracleDatabase.SQL_FOREIGN_KEYS, _cache4Name);
    }

    @Override
    public String getConstrainName(final String _name) throws IOException
    {
        String ret = _name;
        if (_name.length() > 30) {
            final byte buffer[] = _name.getBytes();
            final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            final CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
            final byte readBuffer[] = new byte[5];
            long value = 0;
            if (cis.read(readBuffer) >= 0){
                value = cis.getChecksum().getValue();
            }

            final String valueSt = String.valueOf(value);
            ret = ret.substring(0, 30);
            final int sizeSuf = ret.length() - valueSt.length();
            ret = ret.substring(0, sizeSuf) + value;
        }
        return ret;
    }

}

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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.efaps.db.databases.information.TableInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database class for the PostgreSQL database.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PostgreSQLDatabase
    extends AbstractDatabase<PostgreSQLDatabase>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabase.class);

    /**
     * Select statement to select all unique keys for current logged in
     * PostgreSQL database user.
     *
     * @see #initTableInfoUniqueKeys(Connection, String, Map)
     */
    private static final String SQL_UNIQUE_KEYS = "select "
            + "a.constraint_name as INDEX_NAME, "
            + "a.table_name as TABLE_NAME, "
            + "b.column_name as COLUMN_NAME, "
            + "b.ordinal_position as ORDINAL_POSITION "
        + "from "
            + "information_schema.table_constraints a,"
            + "information_schema.key_column_usage b "
        + "where "
            + "a.constraint_type='UNIQUE' "
            + "and a.table_schema=b.table_schema "
            + "and a.table_name=b.table_name "
            + "and a.constraint_name=b.constraint_name";

    /**
     * Select statement for all foreign keys for current logged in PostgreSQL
     * database user.
     *
     * @see #initTableInfoForeignKeys(Connection, String, Map)
     */
    private static final String SQL_FOREIGN_KEYS = "select "
            + "a.table_name as TABLE_NAME, "
            + "a.constraint_name as FK_NAME, "
            + "b.column_name as FKCOLUMN_NAME, "
            + "case "
                    + "when c.delete_rule='NO ACTION' then '" + DatabaseMetaData.importedKeyNoAction + "' "
                    + "when c.delete_rule='CASCASE' then '" + DatabaseMetaData.importedKeyCascade + "' "
                    + "else '' end as DELETE_RULE, "
            + "d.table_name as PKTABLE_NAME, "
            + "d.column_name as PKCOLUMN_NAME "
        + "from "
            + "information_schema.table_constraints a, "
            + "information_schema.constraint_column_usage b, "
            + "information_schema.referential_constraints c, "
            + "information_schema.constraint_column_usage d "
        + "where "
            + "a.constraint_type='FOREIGN KEY' "
            + "and a.constraint_name=b.constraint_name "
            + "and a.constraint_name=c.constraint_name "
            + "and c.unique_constraint_name=d.constraint_name";

    /**
     * TODO: specificy real column type
     */
    public PostgreSQLDatabase()
    {
        addMapping(ColumnType.INTEGER,      "bigint",    "null", "int8", "int4", "bigserial");
        addMapping(ColumnType.DECIMAL,      "numeric",   "null", "decimal", "numeric");
        addMapping(ColumnType.REAL,         "real",      "null", "float4");
        addMapping(ColumnType.STRING_SHORT, "char",      "null", "bpchar");
        addMapping(ColumnType.STRING_LONG,  "varchar",   "null", "varchar");
        addMapping(ColumnType.DATETIME,     "timestamp", "null", "timestamp");
        addMapping(ColumnType.BLOB,         "bytea",     "null", "bytea");
        addMapping(ColumnType.CLOB,         "text",      "null", "text");
        addMapping(ColumnType.BOOLEAN,      "boolean",   "null", "bool");
    }

    /**
     * @see org.efaps.db.databases.AbstractDatabase#getCurrentTimeStamp()
     * @return "current_timestamp"
     */
    @Override()
    public String getCurrentTimeStamp()
    {
        return "current_timestamp";
    }

  /**
     * <p>This is the PostgreSQL specific implementation of an all deletion.
     * Following order is used to remove all eFaps specific information:
     * <ul>
     * <li>remove all views of the user</li>
     * <li>remove all tables of the user</li>
     * <li>remove all sequences of the user</li>
     * </ul></p>
     * <p>The table are dropped with cascade, so all depending sequences etc.
     * are also dropped automatically. </p>
     * <p>Attention! If application specific tables, views or constraints are
     * defined, this database objects are also removed!</p>
     *
     * @param _con sql connection
     * @throws SQLException on error while executing sql statements
     */
    @Override()
    public void deleteAll(final Connection _con)
        throws SQLException
    {

        final Statement stmtSel = _con.createStatement();
        final Statement stmtExec = _con.createStatement();

        try {
            if (PostgreSQLDatabase.LOG.isInfoEnabled()) {
                PostgreSQLDatabase.LOG.info("Remove all Tables");
            }

            final DatabaseMetaData metaData = _con.getMetaData();

            // delete all views
            final ResultSet rsViews = metaData.getTables(null, null, "%", new String[] { "VIEW" });
            while (rsViews.next()) {
                final String viewName = rsViews.getString("TABLE_NAME");
                if (PostgreSQLDatabase.LOG.isDebugEnabled()) {
                    PostgreSQLDatabase.LOG.debug("  - View '" + viewName + "'");
                }
                stmtExec.execute("drop view " + viewName);
            }
            rsViews.close();

            // delete all tables
            final ResultSet rsTables = metaData.getTables(null, null, "%", new String[] { "TABLE" });
            while (rsTables.next()) {
                final String tableName = rsTables.getString("TABLE_NAME");
                if (PostgreSQLDatabase.LOG.isDebugEnabled()) {
                    PostgreSQLDatabase.LOG.debug("  - Table '" + tableName + "'");
                }
                stmtExec.execute("drop table " + tableName + " cascade");
            }
            rsTables.close();

            //delete all sequences
            final ResultSet rsSeq = stmtSel.executeQuery("SELECT sequence_name FROM information_schema.sequences");
            while (rsSeq.next()) {
                final String seqName = rsSeq.getString("sequence_name");
                if (PostgreSQLDatabase.LOG.isDebugEnabled()) {
                    PostgreSQLDatabase.LOG.debug("  - Sequence '" + seqName + "'");
                }
                stmtExec.execute("drop sequence " + seqName);
            }
            rsSeq.close();

        } finally {
            stmtSel.close();
            stmtExec.close();
        }
    }

    /**
     * For the PostgreSQL database, an eFaps SQL table is created in this steps.
     * <ul>
     * <li>SQL table itself with column <code>ID</code> and unique key on the
     * column is created</li>
     * <li>if the table is an auto increment table (parent table is
     * <code>null</code>, the column <code>ID</code> is set as auto increment
     * column</li>
     * <li>if no parent table is defined, the foreign key to the parent table is
     * automatically set</li>
     * </ul>
     *
     * @see org.efaps.db.databases.AbstractDatabase#createTable(java.sql.Connection, java.lang.String, java.lang.String)
     * @param _con          Connection to be used for the SQL statements
     * @param _table        name for the table
     * @return this PostgreSQL DB definition instance
     * @throws SQLException if the table could not be created
     */
    @Override()
    public PostgreSQLDatabase createTable(final Connection _con,
                                          final String _table)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            stmt.executeUpdate(new StringBuilder()
                .append("create table ").append(_table).append(" (")
                    .append("ID bigint")
                    .append(",").append("constraint ").append(_table).append("_PK_ID primary key (ID)")
                .append(") without OIDS;")
                .toString());
        } finally {
            stmt.close();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public PostgreSQLDatabase defineTableParent(final Connection _con,
                                                final String _table,
                                                final String _parentTable)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder()
            .append("alter table ").append(_table).append(" ")
            .append("add constraint ").append(_table).append("_FK_ID foreign key (ID) ")
            .append("references ").append(_parentTable).append(" (ID)");

        if (PostgreSQLDatabase.LOG.isDebugEnabled())  {
            PostgreSQLDatabase.LOG.info("    ..SQL> " + cmd.toString());
        }

        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally  {
            stmt.close();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public PostgreSQLDatabase defineTableAutoIncrement(final Connection _con,
                                                       final String _table)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            // create sequence
            stmt.execute(new StringBuilder()
                .append("create sequence ").append(_table).append("_id_seq")
                .toString());
            // define for ID column the auto increment value
            stmt.execute(new StringBuilder()
                .append("alter table ").append(_table)
                .append(" alter column id set default nextval('")
                .append(_table).append("_id_seq')")
                .toString());
            // sequence owned by table
            stmt.execute(new StringBuilder()
                .append("alter sequence ").append(_table).append("_id_seq owned by ")
                .append(_table).append(".id")
                .toString());
        } finally {
            stmt.close();
        }
        return this;
    }

    /**
     * A new id for given column of a SQL table is returned (with sequences!).
     * The method must be implemented because the JDBC driver from PostgreSQL
     * does not support that the generated ID of a new table row is returned
     * while the row is inserted.
     *
     * @param _con      sql connection
     * @param _table    sql table for which a new id must returned
     * @param _column   sql table column for which a new id must returned
     * @throws SQLException if a new id could not be retrieved
     * @return new id for the sequence
     */
    @Override()
    public long getNewId(final Connection _con,
                         final String _table,
                         final String _column)
        throws SQLException
    {

        long ret = 0;
        final Statement stmt = _con.createStatement();

        try {
            final StringBuilder cmd = new StringBuilder();
            cmd.append("select nextval('").append(_table).append("_").append(_column).append("_SEQ')");

            final ResultSet rs = stmt.executeQuery(cmd.toString());
            if (rs.next()) {
                ret = rs.getLong(1);
            }
            rs.close();

        } finally {
            stmt.close();
        }
        return ret;
    }

    /**
     * @return always <i>true</i> because supported by PostgreSQL database
     */
    @Override()
    public boolean supportsBinaryInputStream()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostgreSQLDatabase createSequence(final Connection _con,
                                             final String _name,
                                             final String _startValue)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder();
        cmd.append(" create sequence ").append(_name)
            .append(" increment 1 ")
            .append(" minvalue 1 ")
            .append(" maxvalue 9223372036854775807 ")
            .append(" start ").append(_startValue)
            .append(" cache 1;");

        PreparedStatement stmt = null;
        stmt = _con.prepareStatement(cmd.toString());
        stmt.execute();
        stmt.close();
        _con.commit();
        return this;
    }

    /**
     * {@inheritDoc}
     * @throws SQLException
     */
    @Override
    public boolean existsSequence(final Connection _con,
                                  final String _name)
        throws SQLException
    {
        boolean ret = false;
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select relname from pg_class where relkind = 'S' and relname = '")
            .append(_name).append("'");
        final PreparedStatement stmt = _con.prepareStatement(cmd.toString());
        final ResultSet resultset = stmt.executeQuery();
        if (resultset.next()) {
            ret = true;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextSequence(final Connection _con,
                             final String _name)
        throws SQLException
    {
        Long num = null;
        final StringBuilder cmd = new StringBuilder();
        cmd.append(" select nextval('" + _name + "') ");
        final PreparedStatement stmt = _con.prepareStatement(cmd.toString());
        final ResultSet resultset = stmt.executeQuery();
        if (resultset.next()) {
            num = resultset.getLong(1);
        }
        resultset.close();
        stmt.close();
        _con.commit();
        return num != null ? num : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostgreSQLDatabase setSequence(final Connection _con,
                                          final String _name,
                                          final String _value)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select setval('")
            .append(_name).append("',").append(_value).append(")");
        final PreparedStatement stmt = _con.prepareStatement(cmd.toString());
        stmt.executeQuery();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostgreSQLDatabase createView(final Connection _con,
                                         final String _view)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            stmt.executeUpdate(new StringBuilder().append("create view ").append(_view).append(" as select 1")
                            .toString());
        } finally {
            stmt.close();
        }
        return this;
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
    @Override()
    protected void initTableInfoUniqueKeys(final Connection _con,
                                           final String _sql,
                                           final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        super.initTableInfoUniqueKeys(_con, PostgreSQLDatabase.SQL_UNIQUE_KEYS, _cache4Name);
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
    @Override()
    protected void initTableInfoForeignKeys(final Connection _con,
                                            final String _sql,
                                            final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        super.initTableInfoForeignKeys(_con, PostgreSQLDatabase.SQL_FOREIGN_KEYS, _cache4Name);
    }
}

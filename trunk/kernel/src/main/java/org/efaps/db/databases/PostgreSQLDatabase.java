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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database class for the Postgre SQL Database.
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class PostgreSQLDatabase extends AbstractDatabase
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabase.class);

    /**
     * @todo specificy real column type
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
    @Override
    public String getCurrentTimeStamp()
    {
        return "current_timestamp";
    }

  /**
     * This is the PostgreSQL specific implementation of an all deletion.
     * Following order is used to remove all eFaps specific information:
     * <ul>
     * <li>remove all views of the user</li>
     * <li>remove all tables of the user</li>
     * </ul>
     * <p>
     * The table are dropped with cascade, so all depending sequences etc. are
     * also dropped automatically.
     * </p>
     * Attention! If application specific tables, views or constraints are
     * defined, this database objects are also removed!
     *
     * @param _con sql connection
     * @throws SQLException on error while executing sql statements
     */
    @Override
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
     * For the PostgreSQL database, an eFaps sql table is created in this steps.
     * <ul>
     * <li>sql table itself with column <code>ID</code> and unique key on the
     * column is created</li>
     * <li>if the table is an autoincrement table (parent table is
     * <code>null</code>, the column <code>ID</code> is set as autoincrement
     * column</li>
     * <li>if no parent table is defined, the foreign key to the parent table is
     * automatically set</li>
     * </ul>
     *
     * @see org.efaps.db.databases.AbstractDatabase#createTable(java.sql.Connection, java.lang.String, java.lang.String)
     * @param _con          Connection to be used for the sql statements
     * @param _table        name for the table
     * @param _parentTable  name of a parenttable, null if no exists
     * @throws SQLException if the table could not be created
     */
    @Override
    public void createTable(final Connection _con, final String _table, final String _parentTable)
            throws SQLException
    {
        final Statement stmt = _con.createStatement();

        try {
            // create table itself
            final StringBuilder cmd = new StringBuilder();
            cmd.append("create table ").append(_table).append(" (");

            // autoincrement
            if (_parentTable == null) {
                cmd.append("ID bigserial");
            } else {
                cmd.append("ID bigint");
            }

            cmd.append(",").append("constraint ").append(_table).append("_PK_ID primary key (ID)");

            // foreign key to parent sql table
            if (_parentTable != null) {
                cmd.append(",").append("constraint ").append(_table).append("_FK_ID ").append("foreign key (ID) ")
                                .append("references ").append(_parentTable).append(" (ID)");
            }
            cmd.append(") without OIDS;");

            stmt.executeUpdate(cmd.toString());
        } finally {
            stmt.close();
        }
    }

    /**
     * A new id for given column of a sql table is returned (with sequences!).
     *
     * @param _con      sql connection
     * @param _table    sql table for which a new id must returned
     * @param _column   sql table column for which a new id must returned
     * @throws SQLException if a new id could not be retrieved
     * @return new id for the sequence
     */
    @Override
    public long getNewId(final Connection _con, final String _table, final String _column)
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
    @Override
    public boolean supportsBinaryInputStream()
    {
        return true;
    }
}

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
import java.util.Map;

import org.efaps.db.databases.information.TableInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database class for the MySQL database.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MySQLDatabase
    extends AbstractDatabase<MySQLDatabase>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabase.class);

    /**
     * Select statement to select all unique keys for current logged in
     * MySQL database user.
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
     * Select statement for all foreign keys for current logged in MySQL
     * database user.
     *
     * @see #initTableInfoForeignKeys(Connection, String, Map)
     */
    private static final String SQL_FOREIGN_KEYS = "select "
            + "a.TABLE_NAME as TABLE_NAME, "
            + "a.CONSTRAINT_NAME as FK_NAME, "
            + "b.COLUMN_NAME as FKCOLUMN_NAME, "
            + "'' as DELETE_RULE, "
            + "b.REFERENCED_TABLE_NAME as PKTABLE_NAME, "
            + "b.REFERENCED_COLUMN_NAME as PKCOLUMN_NAME "
        + "from "
            + "information_schema.table_constraints a, "
            + "information_schema.key_column_usage b "
        + "where "
            + "a.constraint_type='FOREIGN KEY' "
            + "and a.CONSTRAINT_SCHEMA=b.CONSTRAINT_SCHEMA "
            + "and a.CONSTRAINT_NAME=b.CONSTRAINT_NAME ";

    /**
     * Initializes the mapping between the eFaps column types and the MySQL
     * specific column types.
     */
    public MySQLDatabase()
    {
        addMapping(ColumnType.INTEGER,      "bigint",    "null", "bigint", "integer", "int", "mediumint");
        addMapping(ColumnType.DECIMAL,      "decimal",   "null", "decimal", "dec");
        addMapping(ColumnType.REAL,         "double",    "null", "double", "float");
        addMapping(ColumnType.STRING_SHORT, "varchar",   "null", "text", "tinytext");
        addMapping(ColumnType.STRING_LONG,  "varchar",   "null", "varchar");
        addMapping(ColumnType.DATETIME,     "datetime",  "null", "datetime", "timestamp");
        addMapping(ColumnType.BLOB,         "longblob",  "null", "longblob", "mediumblob", "blob", "tinyblob",
                                                                 "varbinary", "binary");
        addMapping(ColumnType.CLOB,         "longtext",  "null", "longtext");
        addMapping(ColumnType.BOOLEAN,      "boolean",   "null", "boolean", "bool", "tinyint", "bit");
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
     * <p>This is the MySQL specific implementation of an all deletion.
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
            if (MySQLDatabase.LOG.isInfoEnabled()) {
                MySQLDatabase.LOG.info("Remove all Tables");
            }

            final DatabaseMetaData metaData = _con.getMetaData();

            // delete all views
            final ResultSet rsViews = metaData.getTables(null, null, "%", new String[] { "VIEW" });
            while (rsViews.next()) {
                final String viewName = rsViews.getString("TABLE_NAME");
                if (MySQLDatabase.LOG.isDebugEnabled()) {
                    MySQLDatabase.LOG.debug("  - View '" + viewName + "'");
                }
                stmtExec.execute("drop view " + viewName);
            }
            rsViews.close();

            // delete all constraints
            final ResultSet rsTables = metaData.getTables(null, null, "%", new String[] { "TABLE" });
            while (rsTables.next()) {
                final String tableName = rsTables.getString("TABLE_NAME");
                final ResultSet rsf = _con.getMetaData().getImportedKeys(null, null, tableName);
                while (rsf.next())  {
                    final String fkName = rsf.getString("FK_NAME").toUpperCase();
                    if (MySQLDatabase.LOG.isDebugEnabled()) {
                        MySQLDatabase.LOG.debug("  - Foreign Key '" + fkName + "'");
                    }
                    stmtExec.execute("alter table " + tableName + " drop foreign key " + fkName);
                }
            }

            // delete all tables
            rsTables.beforeFirst();
            while (rsTables.next()) {
                final String tableName = rsTables.getString("TABLE_NAME");
                if (MySQLDatabase.LOG.isDebugEnabled()) {
                    MySQLDatabase.LOG.debug("  - Table '" + tableName + "'");
                }
                stmtExec.execute("drop table " + tableName + " cascade");
            }
            rsTables.close();

        } finally {
            stmtSel.close();
            stmtExec.close();
        }
    }

    /**
     * For the MySQL database, an eFaps SQL table is created in this steps.
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
     * @return this MySQL DB definition instance
     * @throws SQLException if the table could not be created
     */
    @Override()
    public MySQLDatabase createTable(final Connection _con,
                                     final String _table)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            stmt.executeUpdate(new StringBuilder()
                .append("create table `").append(_table).append("` (")
                    .append("`ID` bigint ")
                    .append(",").append("constraint `").append(_table).append("_PK_ID` primary key (`ID`)")
                .append(") engine InnoDB character set utf8;")
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
    public MySQLDatabase defineTableAutoIncrement(final Connection _con,
                                                  final String _table)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            // define for ID column the auto increment value
            stmt.execute(new StringBuilder()
                .append("alter table `").append(_table)
                .append("` modify column `ID` bigint not null auto_increment")
                .toString());
        } finally {
            stmt.close();
        }
        return this;
    }

    /**
     * Overwrites original method because MySQL supports automatically
     * generated keys.
     *
     * @return always <i>true</i> because generated keys are supported by MySQL
     *         database
     * @see AbstractDatabase#supportsGetGeneratedKeys()
     */
    @Override()
    public boolean supportsGetGeneratedKeys()
    {
        return true;
    }

    /**
     * Overwrites original method because MySQL supports binary input stream.
     *
     * @return always <i>true</i> because supported by MySQL database
     * @see AbstractDatabase#supportsBinaryInputStream()
     */
    @Override()
    public boolean supportsBinaryInputStream()
    {
        return true;
    }

    /**
     * Returns a single reversed apostrophe &#96; used to select tables within
     * SQL statements for a MySQL database..
     *
     * @return always single reversed apostrophe
     */
    @Override()
    public String getTableQuote()
    {
        return "`";
    }

    /**
     * Returns a single reversed apostrophe &#96; used to select columns within
     * SQL statements for a MySQL database..
     *
     * @return always single reversed apostrophe
     */
    @Override()
    public String getColumnQuote()
    {
        return "`";
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public MySQLDatabase createSequence(final Connection _con,
                                             final String _name,
                                             final String _startValue)
        throws SQLException
    {
        throw new SQLException("sequence not supported");
    }

    /**
     * {@inheritDoc}
     * @throws SQLException
     */
    @Override()
    public boolean existsSequence(final Connection _con,
                                  final String _name)
        throws SQLException
    {
        throw new SQLException("sequence not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public long nextSequence(final Connection _con,
                             final String _name)
        throws SQLException
    {
        throw new SQLException("sequence not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public MySQLDatabase setSequence(final Connection _con,
                                     final String _name,
                                     final String _value)
        throws SQLException
    {
        throw new SQLException("sequence not supported");
    }

    /**
     * Overwrites the original method to specify SQL statement
     * {@link #SQL_UNIQUE_KEYS} as replacement because the JDBC driver for
     * MySQL does not handle matching table names.
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
        super.initTableInfoUniqueKeys(_con, MySQLDatabase.SQL_UNIQUE_KEYS, _cache4Name);
    }

    /**
     * Overwrites the original method to specify SQL statement
     * {@link #SQL_FOREIGN_KEYS} as replacement because the JDBC driver for
     * MySQL does not handle matching table names.
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
        super.initTableInfoForeignKeys(_con, MySQLDatabase.SQL_FOREIGN_KEYS, _cache4Name);
    }
}

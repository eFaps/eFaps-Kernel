/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.Set;
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
        addMapping(ColumnType.INTEGER,      "number(*,0)",     "null", "number(38,0)");
        addMapping(ColumnType.DECIMAL,      "numeric",    "null", "decimal", "numeric");
        addMapping(ColumnType.REAL,         "number",     "null", "number");
        addMapping(ColumnType.STRING_SHORT, "varchar2",   "null", "varchar2", "char");
        addMapping(ColumnType.STRING_LONG,  "varchar2",   "null", "varchar2");
        addMapping(ColumnType.DATETIME,     "timestamp",  "null", "timestamp", "timestamp(6)", "date");
        addMapping(ColumnType.BLOB,         "blob",       "null", "blob");
        addMapping(ColumnType.CLOB,         "nclob",      "null", "nclob");
        addMapping(ColumnType.BOOLEAN,      "number",     "null", "number");
    }

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
            final ResultSet resultset = stmt
                        .executeQuery("select product from product_component_version where product like 'Oracle%';");
            ret = resultset.next();
            resultset.close();
        } finally {
            stmt.close();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxExpressions()
    {
        return 999;
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
     * {@inheritDoc}
     */
    @Override
    public String getTimestampValue(final String _isoDateTime)
    {
        final String format = "'yyyy-mm-dd\"T\"hh24:mi:ss.ff3'";
        return "to_timestamp('" + _isoDateTime + "', " + format + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBooleanValue(final Boolean _value)
    {
        Integer ret = 0;
        if (_value) {
            ret = 1;
        }
        return ret;
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
        final Statement stmtExec = _con.createStatement();
        stmtExec.execute("drop view " + _name);
        return this;
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
                .append("  constraint ");

            final String consName = getConstrainName(_table + "_UK_ID");
            cmd.append(consName).append(" unique(ID)");

            cmd.append(")");
            stmt.executeUpdate(cmd.toString());

        } catch (final IOException e) {
            e.printStackTrace();
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
        final Statement stmt = _con.createStatement();
        // create sequence
        final StringBuilder cmd = new StringBuilder()
            .append("create sequence ").append(_name)
            .append("  increment by 1 ")
            .append("  start with ").append(_startValue)
            .append("  nocache");
        try {
            stmt.executeUpdate(cmd.toString());
        } finally {
            stmt.close();
        }

        nextSequence(_con, _name);
        return this;
    }

    /**
     * {@inheritDoc}
     * @throws SQLException
     */
    @Override
    public OracleDatabase deleteSequence(final Connection _con,
                                         final String _name)
        throws SQLException
    {
        final String cmd = new StringBuilder()
                        .append("drop sequence ").append(_name)
                        .toString();
        final Statement stmt = _con.createStatement();
        try {
            stmt.executeUpdate(cmd);
        } finally {
            stmt.close();
        }
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
        final boolean ret;
        final String cmd = new StringBuilder()
                        .append("SELECT sequence_name FROM user_sequences WHERE sequence_name='")
                        .append(_name.toLowerCase()).append("'")
                        .toString();
        final Statement stmt = _con.createStatement();
        try {
            final ResultSet resultset = stmt.executeQuery(cmd);
            ret = resultset.next();
            resultset.close();
        } finally {
            stmt.close();
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
        final long ret;
        final String cmd = new StringBuilder()
                .append("SELECT " + _name + ".nextval from dual")
                .toString();
        final Statement stmt = _con.createStatement();
        try {
            final ResultSet resultset = stmt.executeQuery(cmd);
            if (resultset.next()) {
                ret = resultset.getLong(1);
            } else  {
                throw new SQLException("fetching new value from sequence '" + _name + "' failed");
            }
            resultset.close();
        } finally {
            stmt.close();
        }
        return ret;
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
        deleteSequence(_con, _name);
        createSequence(_con, _name, _value);
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
    protected void initTableInfoColumns(final Connection _con,
                                        final String _sql,
                                        final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        Statement stmt = null;
        final ResultSet rs;
        if (_sql == null) {
           rs = _con.getMetaData().getColumns(AbstractDatabase.CATALOG, AbstractDatabase.SCHEMAPATTERN, "%", "%");
        } else        {
            stmt = _con.createStatement();
            rs = stmt.executeQuery(_sql);
        }
        try {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME").toUpperCase();
                if (_cache4Name.containsKey(tableName))  {
                    final String colName = rs.getString("COLUMN_NAME").toUpperCase();
                    final String typeName = rs.getString("TYPE_NAME").toLowerCase();
                    final Set<AbstractDatabase.ColumnType> colTypes
                        = OracleDatabase.this.getReadColumnTypes(typeName);
                    if (colTypes == null)  {
                        throw new SQLException("read unknown column type '" + typeName + "'");
                    }
                    final int size = rs.getInt("COLUMN_SIZE");
                    final int scale = rs.getInt("DECIMAL_DIGITS");
                    final boolean isNullable = !"NO".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
                    _cache4Name.get(tableName).addColInfo(colName, colTypes, size, scale, isNullable);
                }
            }
        } finally {
            rs.close();
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    @Override
    public String getConstrainName(final String _name) throws IOException
    {
        String ret = _name;
        if (_name.length() > 30) {
            final byte[] buffer = _name.getBytes();
            final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            final CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
            final byte[] readBuffer = new byte[5];
            long value = 0;
            while (cis.read(readBuffer) >= 0) {
                value = cis.getChecksum().getValue();
            }

            final String valueSt = String.valueOf(value);
            ret = ret.substring(0, 30);
            final int sizeSuf = ret.length() - valueSt.length();
            ret = ret.substring(0, sizeSuf) + value;
        }
        return ret;
    }

    /**
     * Returns a single " used to select columns within
     * SQL statements for a Oracle database..
     *
     * @return always single reversed apostrophe
     */
    @Override
    public String getColumnQuote()
    {
        return "\"";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHibernateDialect()
    {
        return "org.hibernate.dialect.Oracle10gDialect";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected StringBuilder getAlterColumn(final String _columnName,
                                           final org.efaps.db.databases.AbstractDatabase.ColumnType _columnType)
    {
        final StringBuilder ret = new StringBuilder()
            .append(" alter ").append(getColumnQuote()).append(_columnName).append(getColumnQuote())
            .append(" type ")
            .append(getWriteSQLTypeName(_columnType));
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean check4NullValues(final Connection _con,
                                       final String _tableName,
                                       final String _columnName)
        throws SQLException
    {
        boolean ret = true;
        final StringBuilder cmd = new StringBuilder();
        cmd.append("select count(*) from ").append(getTableQuote()).append(_tableName).append(getTableQuote())
                .append(" where ").append(getColumnQuote()).append(_columnName).append(getColumnQuote())
                .append(" is null");

        OracleDatabase.LOG.debug("    ..SQL> {}", cmd);

        final Statement stmt = _con.createStatement();
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(cmd.toString());
            rs.next();
            ret = rs.getInt(1) > 0;
        } finally {
            if (rs != null) {
                rs.close();
            }
            stmt.close();
        }
        return ret;
    }

}

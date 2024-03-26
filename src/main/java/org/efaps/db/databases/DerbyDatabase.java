/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.databases;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.RowProcessor;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.transaction.ConnectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The class implements Apache Derby specific methods for data base access.
 *
 * @author The eFaps Team
 *
 */
public class DerbyDatabase
    extends AbstractDatabase<DerbyDatabase>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DerbyDatabase.class);

    /**
     * SQL Select statement for all foreign keys and constraints.
     *
     * @see #deleteAll(Connection)
     */
    private static final String SELECT_ALL_KEYS
        = "select t.TABLENAME, c.CONSTRAINTNAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLEID=c.TABLEID "
                    + "and c.TYPE='F'";

    /**
     * SQL Select statement for all views.
     *
     * @see #deleteAll(Connection)
     */
    private static final String SELECT_ALL_VIEWS
        = "select t.TABLENAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLETYPE='V'";

    /**
     * SQL Select statement for all tables.
     *
     * @see #deleteAll(Connection)
     */
    private static final String SELECT_ALL_TABLES
        = "select t.TABLENAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLETYPE='T'";


    /**
     * Singleton processor instance that handlers share to save memory. Notice
     * the default scoping to allow only classes in this package to use this
     * instance.
     */
    private static final RowProcessor ROWPROCESSOR = new BasicRowProcessor();

    /**
     * Constructur.
     * TODO: specificy real column type
     */
    public DerbyDatabase()
    {
        addMapping(ColumnType.INTEGER,      "bigint",     "cast(null as bigint)",     "bigint");
//    this.columnMap.put(ColumnType.REAL,         "real");
        addMapping(ColumnType.STRING_SHORT, "char",       "cast(null as char)",       "char");
        addMapping(ColumnType.STRING_LONG,  "varchar",    "cast(null as varchar)",    "varchar");
        addMapping(ColumnType.DATETIME,     "timestamp",  "cast(null as timestamp)",  "timestamp");
        addMapping(ColumnType.BLOB,         "blob(2G)",   "cast(null as blob)",       "blob");
        addMapping(ColumnType.CLOB,         "clob(2G)",   "cast(null as clob)",       "clob");
        addMapping(ColumnType.BOOLEAN,      "smallint",   "cast(null as smallint)",   "smallint");
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
    public String getCurrentTimeStamp()
    {
        return "current_timestamp";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTimestampValue(final String _isoDateTime)
    {
        return "timestamp '" + _isoDateTime + "'";
    }

    @Override
    public Object getBooleanValue(final Boolean _value)
    {
        // TODO Auto-generated method stub
        return _value;
    }

    /**
     * This is the Derby specific implementation of an all deletion. Following
     * order is used to remove all eFaps specific information:
     * <ul>
     * <li>remove all foreign keys of the user</li>
     * <li>remove all views of the user</li>
     * <li>remove all tables of the user</li>
     * </ul>
     * Attention! If application specific tables, views or constraints are
     * defined, this database objects are also removed!
     *
     * @param _con  sql connection
     * @throws SQLException if remove of keys, views or tables failed
     */
    @Override
    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    public void deleteAll(final Connection _con)
        throws SQLException
    {
        final Statement stmtSel = _con.createStatement();
        final Statement stmtExec = _con.createStatement();

        try  {
            // remove all foreign keys
            if (DerbyDatabase.LOG.isInfoEnabled())  {
                DerbyDatabase.LOG.info("Remove all Foreign Keys");
            }
            ResultSet rs = stmtSel.executeQuery(DerbyDatabase.SELECT_ALL_KEYS);
            while (rs.next())  {
                final String tableName = rs.getString(1);
                final String constrName = rs.getString(2);
                if (DerbyDatabase.LOG.isDebugEnabled())  {
                    DerbyDatabase.LOG.debug("  - Table '" + tableName + "' Constraint '" + constrName + "'");
                }
                stmtExec.execute("alter table " + tableName + " drop constraint " + constrName);
            }
            rs.close();

            // remove all views
            if (DerbyDatabase.LOG.isInfoEnabled())  {
                DerbyDatabase.LOG.info("Remove all Views");
            }
            rs = stmtSel.executeQuery(DerbyDatabase.SELECT_ALL_VIEWS);
            while (rs.next())  {
                final String viewName = rs.getString(1);
                if (DerbyDatabase.LOG.isDebugEnabled())  {
                    DerbyDatabase.LOG.debug("  - View '" + viewName + "'");
                }
                stmtExec.execute("drop view " + viewName);
            }
            rs.close();

            // remove all tables
            if (DerbyDatabase.LOG.isInfoEnabled())  {
                DerbyDatabase.LOG.info("Remove all Tables");
            }
            rs = stmtSel.executeQuery(DerbyDatabase.SELECT_ALL_TABLES);
            while (rs.next())  {
                final String tableName = rs.getString(1);
                if (DerbyDatabase.LOG.isDebugEnabled())  {
                    DerbyDatabase.LOG.debug("  - Table '" + tableName + "'");
                }
                stmtExec.execute("drop table " + tableName);
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
    public DerbyDatabase deleteView(final Connection _con,
                                    final String _name)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DerbyDatabase createTable(final Connection _con,
                                     final String _table/*,
                                     final String _parentTable*/)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();

        try  {

            // create table itself
            final StringBuilder cmd = new StringBuilder()
                .append("create table ").append(_table).append(" (")
                .append("  ID bigint not null");
/* TODO
            // auto increment
            if (_parentTable == null)  {
                cmd.append(" generated always as identity (start with 1, increment by 1)");
            }
*/
            cmd.append(",")
                .append("  constraint ").append(_table).append("_UK_ID unique(ID)");
/* TODO
            // foreign key to parent sql table
            if (_parentTable != null)  {
                cmd.append(",")
                    .append("constraint ").append(_table).append("_FK_ID ")
                    .append("  foreign key(ID) ")
                    .append("  references ").append(_parentTable).append("(ID)");
            }
*/
            cmd.append(")");
            stmt.executeUpdate(cmd.toString());
        } finally  {
            stmt.close();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public DerbyDatabase defineTableAutoIncrement(final Connection _con,
                                                  final String _table)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * Adds a column to a SQL table. The method overrides the original method,
     * because Derby does not allow for <code>NOT NULL</code> columns that no
     * default value is defined. Is such column is created, the default value
     * for real and integer is <code>0</code>, for date time, short and long
     * string a zero length string.
     *
     * @param _con          SQL connection
     * @param _tableName    name of table to update
     * @param _columnName   column to add
     * @param _columnType   type of column to add
     * @param _defaultValue default value of the column (or null if not
     *                      specified)
     * @param _length       length of column to add (or 0 if not specified)
     * @param _scale        scale of the column to add (or 0 if not
     *                      specified)
     * @throws SQLException if the column could not be added to the tables
     * @return this
     */
    @Override
    //CHECKSTYLE:OFF
    public DerbyDatabase addTableColumn(final Connection _con,
                                        final String _tableName,
                                        final String _columnName,
                                        final ColumnType _columnType,
                                        final String _defaultValue,
                                        final int _length,
                                        final int _scale)
        throws SQLException
    {
      //CHECKSTYLE:ON
        String defaultValue = _defaultValue;

        if (defaultValue == null)  {
            switch (_columnType)  {
                case INTEGER:
                case REAL:
                    defaultValue = "0";
                    break;
                case DATETIME:
                case STRING_LONG:
                case STRING_SHORT:
                    defaultValue = "''";
                    break;
                default:
                    break;
            }
        }
        return super.addTableColumn(_con, _tableName, _columnName, _columnType,
                                    defaultValue, _length, _scale);
    }

    /**
     * @return always <i>true</i> because supported by Derby database
     */
    @Override
    public boolean supportsGetGeneratedKeys()
    {
        return true;
    }

    /**
     * @return always <i>true</i> because supported by PostgreSQL database
     */
    @Override
    public boolean supportsBinaryInputStream()
    {
        return false;
    }

    /**
     * @return always <i>false</i> because Apache Derby has some problems to
     *         handle to big transactions
     */
    @Override
    public boolean supportsBigTransactions()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DerbyDatabase createSequence(final Connection _con,
                                        final String _name,
                                        final long _startValue)
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DerbyDatabase deleteSequence(final Connection _con,
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
    public long nextSequence(final ConnectionResource _con,
                             final String _name)
        throws SQLException
    {
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DerbyDatabase setSequence(final Connection _con,
                                     final String _name,
                                     final long _value)
        throws SQLException
    {
        // TODO Auto-generated method stub
        throw new Error("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHibernateDialect()
    {
        return "org.hibernate.dialect.DerbyTenSevenDialect";
    }

    /**
     * Evaluates for given table name all information about the table and returns
     * them as instance of {@link TableInformation}.<br/>
     * This method overwrites the original method because the standard JDBC
     * methods do not work for the Derby database to get unique keys.
     *
     * @param _con        SQL connection
     * @param _tableName  name of SQL table for which the information is fetched
     * @return instance of {@link TableInformation} with table information
     * @throws SQLException if information about the table could not be fetched
     * @see TableInformation
     */
    /**
     * Fetches all unique keys for this table. Instead of using the JDBC
     * meta data functionality, a SQL statement on system tables are used,
     * because the JDBC meta data functionality returns for unique keys
     * internal names and not the real names. Also if a unique key includes
     * also columns with null values, this unique keys are not included.
     *
     * @param _con        Connection
     * @param _sql        Statement
     * @param _cache4Name   cache
     * @throws SQLException if unique keys could not be fetched
     */
    @Override
    protected void initTableInfoUniqueKeys(final Connection _con,
                                           final String _sql,
                                           final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        final String sqlStmt = new StringBuilder()
            .append("select t.tablename as TABLE_NAME, c.CONSTRAINTNAME as INDEX_NAME, g.DESCRIPTOR as COLUMN_NAME")
            .append(" from SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c, SYS.SYSKEYS k, SYS.SYSCONGLOMERATES g ")
            .append(" where t.TABLEID=c.TABLEID")
                .append(" AND c.TYPE='U'")
                .append(" AND c.CONSTRAINTID = k.CONSTRAINTID")
                .append(" AND k.CONGLOMERATEID = g.CONGLOMERATEID")
            .toString();
        super.initTableInfoUniqueKeys(_con, sqlStmt, _cache4Name);
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
    protected StringBuilder getAlterColumnIsNotNull(final String _columnName,
                                                    final boolean _isNotNull)
    {
        final StringBuilder ret = new StringBuilder()
            .append(" alter column ").append(getColumnQuote()).append(_columnName).append(getColumnQuote());
        if (_isNotNull) {
            ret.append(" set ");
        } else {
            ret.append(" drop ");
        }
        ret.append(" not null");
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

        DerbyDatabase.LOG.debug("    ..SQL> {}", cmd);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public RowProcessor getRowProcessor()
    {
        return DerbyDatabase.ROWPROCESSOR;
    }
}

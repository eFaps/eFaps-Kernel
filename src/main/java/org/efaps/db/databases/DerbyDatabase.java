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
import java.util.Map;

import org.efaps.db.databases.information.TableInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class implements Apache Derby specific methods for data base access.
 *
 * @author The eFaps Team
 * @version $Id$
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

    @Override()
    public String getCurrentTimeStamp()
    {
        return "current_timestamp";
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
    @Override()
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
     * For the derby database, an eFaps SQL table is created in this steps:
     * <ul>
     * <li>SQL table itself with column <code>ID</code> and unique key on the
     *     column is created</li>
     * <li>if the table is an auto-increment table (parent table is
     *     <code>null</code>, the column <code>ID</code> is set as
     *     auto-increment column</li>
     * <li>if no parent table is defined, the foreign key to the parent table is
     *     automatically set</li>
     * </ul>
     *
     * @throws SQLException if the table could not be created
     */
    @Override()
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
     * TODO: implement
     */
    @Override()
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
     * @param _isNotNull    <i>true</i> means that the column has no
     *                      <code>null</code> values
     * @throws SQLException if the column could not be added to the tables
     */
    @Override()
    public DerbyDatabase addTableColumn(final Connection _con,
                                        final String _tableName,
                                        final String _columnName,
                                        final ColumnType _columnType,
                                        final String _defaultValue,
                                        final int _length,
                                        final int _scale,
                                        final boolean _isNotNull)
        throws SQLException
    {
        String defaultValue = _defaultValue;

        if (_isNotNull && (defaultValue == null))  {
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
            }
        }
        return super.addTableColumn(_con, _tableName, _columnName, _columnType,
                                    defaultValue, _length, _scale, _isNotNull);
    }

    /**
     * @return always <i>true</i> because supported by Derby database
     */
    @Override()
    public boolean supportsGetGeneratedKeys()
    {
        return true;
    }

    /**
     * @return always <i>true</i> because supported by PostgreSQL database
     */
    @Override()
    public boolean supportsBinaryInputStream()
    {
        return false;
    }

    /**
     * @return always <i>false</i> because Apache Derby has some problems to
     *         handle to big transactions
     */
    @Override()
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
                                        final String _startValue)
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
    public DerbyDatabase setSequence(final Connection _con,
                                     final String _name,
                                     final String _value)
        throws SQLException
    {
        // TODO Auto-generated method stub
        throw new Error("not implemented");
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
     * @param _metaData   database meta data
     * @param _tableName  name of table which must be evaluated
     * @throws SQLException if unique keys could not be fetched
     */
    @Override()
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
}

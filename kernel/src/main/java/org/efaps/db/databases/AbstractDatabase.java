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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.efaps.db.databases.information.TableInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract definition of database specific information and methods like alter
 * of table columns.
 *
 * @author The eFaps Team
 * @version $Id$
 * @param <DB>  derived DB class
 */
public abstract class AbstractDatabase<DB extends AbstractDatabase<?>>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DerbyDatabase.class);

    /**
     * The enumeration defines the known column types in the database.
     */
    public enum ColumnType {
        /** integer number. */
        INTEGER,
        /** numeric/decimal numbers. */
        DECIMAL,
        /** real number. */
        REAL,
        /** short string. */
        STRING_SHORT,
        /** long string. */
        STRING_LONG,
        /** date and time. */
        DATETIME,
        /** binary large object. */
        BLOB,
        /** character large object. */
        CLOB,
        /** boolean. */
        BOOLEAN,
    }

    /**
     * The map stores the mapping between the column types used in eFaps and the
     * database specific column types.
     *
     * @see #addMapping(ColumnType, String, String, String...)
     * @see #getWriteSQLTypeName(ColumnType)
     */
    private final Map<AbstractDatabase.ColumnType, String> writeColTypeMap
        = new HashMap<AbstractDatabase.ColumnType, String>();

    /**
     * The map stores the mapping between column types used in the database and
     * eFaps.
     *
     * @see #addMapping(ColumnType, String, String, String...)
     * @see #getReadColumnTypes(String)
     */
    private final Map<String, Set<AbstractDatabase.ColumnType>> readColTypeMap
        = new HashMap<String, Set<AbstractDatabase.ColumnType>>();

    /**
     * The map stores the mapping between column types used in eFaps and the
     * related null value select statement of the database.
     *
     * @see #addMapping(ColumnType, String, String, String...)
     */
    private final Map<AbstractDatabase.ColumnType, String> nullValueColTypeMap
        = new HashMap<AbstractDatabase.ColumnType, String>();

    /**
     * Adds a new mapping for given eFaps column type used for mapping from and
     * to the SQL database.
     *
     * @param _columnType       column type within eFaps
     * @param _writeTypeName    SQL type name used to write (create new column
     *                          within a SQL table)
     * @param _nullValueSelect  null value select used within the query if a
     *                          link target could be a null (and so all
     *                          selected values must null in the SQL statement
     *                          for objects without this link)
     * @param _readTypeNames    list of SQL type names returned from the
     *                          database meta data reading
     * @see #readColTypeMap   to map from an eFaps column type to a SQL type name
     * @see #writeColTypeMap  to map from a SQL type name to possible eFaps
     *                        column types
     */
    protected void addMapping(final ColumnType _columnType,
                              final String _writeTypeName,
                              final String _nullValueSelect,
                              final String... _readTypeNames)
    {
        this.writeColTypeMap.put(_columnType, _writeTypeName);
        this.nullValueColTypeMap.put(_columnType, _nullValueSelect);
        for (final String readTypeName : _readTypeNames)  {
            Set<AbstractDatabase.ColumnType> colTypes = this.readColTypeMap.get(readTypeName);
            if (colTypes == null)  {
                colTypes = new HashSet<AbstractDatabase.ColumnType>();
                this.readColTypeMap.put(readTypeName, colTypes);
            }
            colTypes.add(_columnType);
        }
    }


    /**
     * Returns for given column type the database vendor specific type name.
     *
     * @param _columnType   column type for which the vendor specific column
     *                      type should be returned
     * @return SQL specific column type name
     * @see #writeColTypeMap
     * @see #addMapping       used to define the map
     */
    protected String getWriteSQLTypeName(final ColumnType _columnType)
    {
        return this.writeColTypeMap.get(_columnType);
    }

    /**
     * Converts given SQL column type name in a set of eFaps column types. If
     * no mapping is specified, a <code>null</code> is returned.
     *
     * @param _readTypeName SQL column type name read from the database
     * @return set of eFaps column types (or <code>null</code> if not specified)
     * @see #readColTypeMap
     * @see #addMapping       used to define the map
     */
    public Set<AbstractDatabase.ColumnType> getReadColumnTypes(final String _readTypeName)
    {
        return this.readColTypeMap.get(_readTypeName);
    }

    /**
     * Returns for given column type the database vendor specific null value
     * select statement.
     *
     * @param _columnType   column type for which the database vendor specific
     *                      null value select is searched
     * @return null value select
     * @see #nullValueColTypeMap
     */
    public String getNullValueSelect(final AbstractDatabase.ColumnType _columnType)
    {
        return this.nullValueColTypeMap.get(_columnType);
    }

    /**
     * The method returns the database vendor specific value for the current
     * time stamp.
     *
     * @return vendor specific string of the current time stamp
     */
    public abstract String getCurrentTimeStamp();

    /**
     * The method implements a delete all of database user specific objects
     * (e.g. tables, views etc...). The method is called before a complete
     * rebuild is done.
     *
     * @param _con    sql connection
     * @throws SQLException if delete of the SQL data model failed
     */
    public abstract void deleteAll(final Connection _con) throws SQLException;

    /**
     * The method tests, if a view with given name exists.
     *
     * @param _con        sql connection
     * @param _viewName   name of view to test
     * @return <i>true</i> if view exists, otherwise <i>false</i>
     * @throws SQLException if the exist check failed
     */
    public boolean existsView(final Connection _con,
                              final String _viewName)
        throws SQLException
    {
        boolean ret = false;

        final DatabaseMetaData metaData = _con.getMetaData();

        // first test with lower case
        final ResultSet rs = metaData.getTables(null, null, _viewName.toLowerCase(), new String[]{"VIEW"});
        if (rs.next())  {
            ret = true;
        }
        rs.close();

        // then test with upper case
        if (!ret)  {
            final ResultSet rsUC = metaData.getTables(null, null, _viewName.toUpperCase(), new String[]{"VIEW"});
            if (rsUC.next())  {
                ret = true;
            }
            rsUC.close();
        }

        return ret;
    }

    /**
     * The method tests, if a view with given name exists.
     *
     * @param _con        sql connection
     * @param _tableName  name of table to test
     * @return <i>true</i> if SQL table exists, otherwise <i>false</i>
     * @throws SQLException if the exist check for the table failed
     */
    public boolean existsTable(final Connection _con,
                               final String _tableName)
        throws SQLException
    {
        boolean ret = false;

        final DatabaseMetaData metaData = _con.getMetaData();

        // first test with lower case
        final ResultSet rs = metaData.getTables(null, null, _tableName.toLowerCase(), new String[]{"TABLE"});
        if (rs.next())  {
            ret = true;
        }
        rs.close();

        // then test with upper case
        if (!ret)  {
            final ResultSet rsUC = metaData.getTables(null, null, _tableName.toUpperCase(), new String[]{"TABLE"});
            if (rsUC.next())  {
                ret = true;
            }
            rsUC.close();
        }
        return ret;
    }

    /**
     * Evaluates for given table name all information about the table and
     * returns them as instance of {@link TableInformation}.
     *
     * @param _con          SQL connection
     * @param _tableName    name of SQL table for which the information is
     *                      fetched
     * @return instance of {@link TableInformation} with table information
     * @throws SQLException if information about the table could not be fetched
     * @see TableInformation
     */
    public TableInformation getTableInformation(final Connection _con,
                                                final String _tableName)
        throws SQLException
    {
        return new TableInformation(_con, _tableName);
    }

    /**
     * A new SQL table with unique column <code>ID</code> is created.
     *
     * @param _con          SQL connection
     * @param _table        name of the table to create
     * @return this instance
     * @throws SQLException if the create of the table failed
     */
    public abstract DB createTable(final Connection _con,
                                   final String _table)
        throws SQLException;

    /**
     * For a new created SQL table the column <code>ID</code> is update with a
     * foreign key to a parent table.
     *
     * @param _con          SQL connection
     * @param _table        name of the SQL table to update
     * @param _parentTable  name of the parent table
     * @return this instance
     * @throws SQLException if the update of the table failed
     */
    public abstract DB defineTableParent(final Connection _con,
                                         final String _table,
                                         final String _parentTable)
        throws SQLException;

    /**
     * Defines a new created SQL table as auto incremented.
     *
     * @param _con          SQL connection
     * @param _table        name of the SQL table to update
     * @return this instance
     * @throws SQLException if the update of the table failed
     */
    public abstract DB defineTableAutoIncrement(final Connection _con,
                                                final String _table)
        throws SQLException;

    /**
     * Adds a column to a SQL table.
     *
     * @param _con              SQL connection
     * @param _tableName        name of table to update
     * @param _columnName       column to add
     * @param _columnType       type of column to add
     * @param _defaultValue     default value of the column (or null if not
     *                          specified)
     * @param _length           length of column to add (or 0 if not specified)
     * @param _scale            scale of the column (or 0 if not specified)
     * @param _isNotNull        <i>true</i> means that the column has no
     *                          <code>null</code> values
     * @throws SQLException if the column could not be added to the tables
     */
    public void addTableColumn(final Connection _con,
                               final String _tableName,
                               final String _columnName,
                               final ColumnType _columnType,
                               final String _defaultValue,
                               final int _length,
                               final int _scale,
                               final boolean _isNotNull)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder();
        cmd.append("alter table ").append(_tableName).append(" ")
           .append("add ").append(_columnName).append(" ")
           .append(getWriteSQLTypeName(_columnType));
        if (_length > 0)  {
            cmd.append("(").append(_length);
            if (_scale > 0) {
                cmd.append(",").append(_scale);
            }
            cmd.append(")");
        }
        if (_defaultValue != null)  {
            cmd.append(" default ").append(_defaultValue);
        }
        if (_isNotNull)  {
            cmd.append(" not null");
        }

        // log statement
        if (AbstractDatabase.LOG.isDebugEnabled())  {
            AbstractDatabase.LOG.info("    ..SQL> " + cmd.toString());
        }

        // excecute statement
        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally  {
            stmt.close();
        }
    }

    /**
     * Adds a new unique key to given table name.
     *
     * @param _con            SQL connection
     * @param _tableName      name of table for which the unique key must be
     *                        created
     * @param _uniqueKeyName  name of unique key
     * @param _columns        comma separated list of column names for which the
     *                        unique key is created
     * @throws SQLException if the unique key could not be created
     */
    public void addUniqueKey(final Connection _con,
                             final String _tableName,
                             final String _uniqueKeyName,
                             final String _columns)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder();
        cmd.append("alter table ").append(_tableName).append(" ")
           .append("add constraint ").append(_uniqueKeyName).append(" ")
           .append("unique(").append(_columns).append(")");

        // log statement
        if (AbstractDatabase.LOG.isDebugEnabled())  {
            AbstractDatabase.LOG.info("    ..SQL> " + cmd.toString());
        }

        // execute statement
        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally  {
            stmt.close();
        }
    }

    /**
     * Adds a foreign key to given SQL table.
     *
     * @param _con            SQL connection
     * @param _tableName      name of table for which the foreign key must be
     *                        created
     * @param _foreignKeyName name of foreign key to create
     * @param _key            key in the table (column name)
     * @param _reference      external reference (external table and column name)
     * @param _cascade        if the value in the external table is deleted,
     *                        should this value also automatically deleted?
     * @throws SQLException if foreign key could not be defined for SQL table
     */
    public void addForeignKey(final Connection _con,
                              final String _tableName,
                              final String _foreignKeyName,
                              final String _key,
                              final String _reference,
                              final boolean _cascade)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder()
            .append("alter table ").append(_tableName).append(" ")
            .append("add constraint ").append(_foreignKeyName).append(" ")
            .append("foreign key(").append(_key).append(") ")
            .append("references ").append(_reference);
        if (_cascade)  {
            cmd.append(" on delete cascade");
        }

        // log statement
        if (AbstractDatabase.LOG.isDebugEnabled())  {
            AbstractDatabase.LOG.info("    ..SQL> " + cmd.toString());
        }

        // execute statement
        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally  {
            stmt.close();
        }
    }

    /**
     * Adds a new check key to given SQL table.
     *
     * @param _con          SQL connection
     * @param _tableName    name of the SQL table for which the check key must
     *                      be created
     * @param _checkKeyName name of check key to create
     * @param _condition    condition of the check key
     * @throws SQLException if check key could not be defined for SQL table
     */
    public void addCheckKey(final Connection _con,
                            final String _tableName,
                            final String _checkKeyName,
                            final String _condition)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder()
            .append("alter table ").append(_tableName).append(" ")
            .append("add constraint ").append(_checkKeyName).append(" ")
            .append("check(").append(_condition).append(")");

        // log statement
        if (AbstractDatabase.LOG.isDebugEnabled())  {
            AbstractDatabase.LOG.info("    ..SQL> " + cmd.toString());
        }

        // excecute statement
        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally  {
            stmt.close();
        }
    }

    /**
     * <p>This int is used for the maximum numbers of Values inside an
     * expression.</p>
     * <p>The value is used in the OneRounQuery. The SQL-Statemenat looks like
     * "SELECT...WHERE..IN (val1,val2,val3,...valn)" The int is the maximum
     * Value for n before making a new Select.</p>
     *
     * @return max Number of Value in an Expression, 0 if no max is kown
     */
    public int getMaxExpressions()
    {
        return 0;
    }

    /**
     * A new id for given column of a SQL table is returned (e.g. with
     * sequences). This abstract class always throws a SQLException, because
     * for default, it is not needed to implement (only if the JDBC drive does
     * not implement method 'getGeneratedKeys' for java.sql.Statements).
     *
     * @param _con    sql connection
     * @param _table  sql table for which a new id must returned
     * @param _column sql table column for which a new id must returned
     * @return new id number
     * @throws SQLException always, because method itself is not implemented not
     *                      not allowed to call
     */
    public long getNewId(final Connection _con,
                         final String _table,
                         final String _column)
        throws SQLException
    {
        throw new SQLException("method 'getNewId' not imlemented");
    }

    /**
     * The method returns if a database implementation supports to get
     * generated keys while inserting a new line in a SQL table.
     *
     * @return always <i>false</i> because not implemented in this class
     */
    public boolean supportsGetGeneratedKeys()
    {
        return false;
    }

    /**
     * The method returns if a database implementation support to get multiple
     * auto generated keys. If defined to <i>true</i>, the insert is done with
     * defined column names for the auto generated columns. Otherwise only
     * {@link java.sql.Statement#RETURN_GENERATED_KEYS} is given for the
     * insert.
     *
     * @return always <i>false</i> because not implemented in this class
     * @see #supportsGetGeneratedKeys
     */
    public boolean supportsMultiGeneratedKeys()
    {
        return false;
    }

    /**
     * The method returns if a database implementation supports for blobs
     * binary input stream supports the available method or not.
     *
     * @return always <i>false</i> because not implemented in this class
     * @see #supportsBinaryInputStream
     */
    public boolean supportsBlobInputStreamAvailable()
    {
        return false;
    }

    /**
     * The method returns if a database implementation supports directly binary
     * stream for result sets (instead of using first blobs).
     *
     * @return always <i>false</i> because not implemented in this class
     * @see #supportsBlobInputStreamAvailable
     */
    public boolean supportsBinaryInputStream()
    {
        return false;
    }

    /**
     * Returns <i>true</i> if a database could handle big transactions used
     * within the eFaps updates.
     *
     * @return always <i>true</i> because normally a database should implement
     *          big transactions
     */
    public boolean supportsBigTransactions()
    {
        return true;
    }

    /**
     * Instantiate the given DB class name and returns them.
     *
     * @param _dbClassName  name of the class to instantiate
     * @return new database definition instance
     * @throws ClassNotFoundException   if class for the DB is not found
     * @throws InstantiationException   if DB class could not be instantiated
     * @throws IllegalAccessException   if DB class could not be accessed
     */
    public static AbstractDatabase findByClassName(final String _dbClassName)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return (AbstractDatabase) Class.forName(_dbClassName).newInstance();
    }
}

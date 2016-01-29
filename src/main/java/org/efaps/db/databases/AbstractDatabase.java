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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.lang3.StringUtils;
import org.efaps.bpm.NamingStrategy;
import org.efaps.db.Context;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.wrapper.SQLDelete;
import org.efaps.db.wrapper.SQLDelete.DeleteDefintion;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.init.INamingBinds;
import org.efaps.init.IeFapsProperties;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheReloadException;
import org.joda.time.ReadableDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Abstract definition of database specific information and methods like alter
 * of table columns.
 *
 * @author The eFaps Team
 * @param <T> derived DB class
 */
public abstract class AbstractDatabase<T extends AbstractDatabase<?>>
{

    /**
     * Pattern that  will be translated in a pattern that matches any sequence
     * of zero or more characters.
     */
    public static final String WILDCARDPATTERN = "*";

    /**
     * Pattern that  will be translated in a pattern that matches any any
     * single character.
     */
    public static final String SINGLECHARACTERPATTERN = "?";

    /**
     * Character used to escape the above patterns.
     */
    public static final String ESCAPECHARACTER = "\\";

    /**
     * Pattern for the schema.
     */
    private static String SCHEMAPATTERN = null;

    /**
     * Name of the catalog.
     */
    private static String CATALOG = null;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabase.class);

    static {
        try {
            final InitialContext initCtx = new InitialContext();
            javax.naming.Context envCtx = null;
            try {
                envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            } catch (final NamingException e) {
                AbstractDatabase.LOG.info("Catched NamingException on evaluation for DataBase.");
            }
            // for a build the context might be different, try this before surrender
            if (envCtx == null) {
                envCtx = (javax.naming.Context) initCtx.lookup("java:/comp/env");
            }
            try {
                final Map<?, ?> props = (Map<?, ?>) envCtx.lookup(INamingBinds.RESOURCE_CONFIGPROPERTIES);
                if (props != null) {
                    AbstractDatabase.SCHEMAPATTERN = (String) props.get(IeFapsProperties.DBSCHEMAPATTERN);
                    AbstractDatabase.CATALOG = (String) props.get(IeFapsProperties.DBCATALOG);
                }
            } catch (final NamingException e) {
                AbstractDatabase.LOG.info("Catched NamingException on evaluation for Properties.");
            }
        } catch (final NamingException e) {
            AbstractDatabase.LOG.error("NamingException", e);
        }
    }

    /**
     * The enumeration defines the known column types in the database.
     */
    public enum ColumnType
    {
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
     * Caching for table information read from the SQL database.
     *
     * @see #getTableInformation(Connection, String)
     */
    private final TableInfoCache cache = new TableInfoCache();

    /**
     * Method is used to determine if this DataBase is connected. It uses SQL
     * statements against the database to determine if it is the right database
     * using unique identifiers e.g. systemtables, version info etc.
     *
     * @param _connection Connection to be used foo analyze
     * @return true if this database is connected
     * @throws SQLException on error
     */
    public abstract boolean isConnected(final Connection _connection)
        throws SQLException;

    /**
     * Initializes the {@link #cache} for the table informations.
     *
     * @see #cache
     */
    public void initialize()
    {
        initialize(AbstractDatabase.class);
    }

    /**
     * Initializes the {@link #cache} for the table informations with given
     * initializer.
     *
     * @param _class initializer class
     * @see #cache
     */
    public void initialize(final Class<?> _class)
    {
        this.cache.initialize(_class);
    }

    /**
     * @return {@link #SCHEMAPATTERN}
     */
    protected String getSchemaPattern()
    {
        return AbstractDatabase.SCHEMAPATTERN;
    }

    /**
     * @return {@link #CATALOG}
     */
    protected String getCatalog()
    {
        return AbstractDatabase.CATALOG;
    }

    /**
     * Adds a new mapping for given eFaps column type used for mapping from and
     * to the SQL database.
     *
     * @param _columnType column type within eFaps
     * @param _writeTypeName SQL type name used to write (create new column
     *            within a SQL table)
     * @param _nullValueSelect null value select used within the query if a link
     *            target could be a null (and so all selected values must null
     *            in the SQL statement for objects without this link)
     * @param _readTypeNames list of SQL type names returned from the database
     *            meta data reading
     * @see #readColTypeMap to map from an eFaps column type to a SQL type name
     * @see #writeColTypeMap to map from a SQL type name to possible eFaps
     *      column types
     */
    protected void addMapping(final ColumnType _columnType,
                              final String _writeTypeName,
                              final String _nullValueSelect,
                              final String... _readTypeNames)
    {
        this.writeColTypeMap.put(_columnType, _writeTypeName);
        this.nullValueColTypeMap.put(_columnType, _nullValueSelect);
        for (final String readTypeName : _readTypeNames) {
            Set<AbstractDatabase.ColumnType> colTypes = this.readColTypeMap.get(readTypeName);
            if (colTypes == null) {
                colTypes = new HashSet<AbstractDatabase.ColumnType>();
                this.readColTypeMap.put(readTypeName, colTypes);
            }
            colTypes.add(_columnType);
        }
    }

    /**
     *
     * @param _tableName name of the table to insert
     * @param _idCol column holding the id
     * @param _newId <i>true</i> if a new id must be created; otherwise
     *            <i>false</i>
     * @return new SQL insert statement
     */
    public SQLInsert newInsert(final String _tableName,
                               final String _idCol,
                               final boolean _newId)
    {
        return new SQLInsert(_tableName, _idCol, _newId);
    }

    /**
     *
     * @param _tableName name of the table to insert
     * @param _idCol column holding the id
     * @param _id id to update
     * @return new SQL insert statement
     */
    public SQLUpdate newUpdate(final String _tableName,
                               final String _idCol,
                               final long _id)
    {
        return new SQLUpdate(_tableName, _idCol, _id);
    }

    /**
     * @param _definition deleteDefinitions
     * @return new SQLDelete
     */
    public SQLDelete newDelete(final DeleteDefintion... _definition)
    {
        return new SQLDelete(_definition);
    }

    /**
     * @return new SQL select statement
     */
    public SQLSelect newSelect()
    {
        return new SQLSelect();
    }

    /**
     * Returns for given column type the database vendor specific type name.
     *
     * @param _columnType column type for which the vendor specific column type
     *            should be returned
     * @return SQL specific column type name
     * @see #writeColTypeMap
     * @see #addMapping used to define the map
     */
    protected String getWriteSQLTypeName(final ColumnType _columnType)
    {
        return this.writeColTypeMap.get(_columnType);
    }

    /**
     * Converts given SQL column type name in a set of eFaps column types. If no
     * mapping is specified, a <code>null</code> is returned.
     *
     * @param _readTypeName SQL column type name read from the database
     * @return set of eFaps column types (or <code>null</code> if not specified)
     * @see #readColTypeMap
     * @see #addMapping used to define the map
     */
    public Set<AbstractDatabase.ColumnType> getReadColumnTypes(final String _readTypeName)
    {
        return this.readColTypeMap.get(_readTypeName);
    }

    /**
     * Returns for given column type the database vendor specific null value
     * select statement.
     *
     * @param _columnType column type for which the database vendor specific
     *            null value select is searched
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
     * Get the vendor specific Timestamp cast implementation.
     *
     * @param _dateString dateTime that will be casted to an timestamp
     * @return vendor specific implementation of timestamp
     */
    public abstract String getTimestampValue(final String _dateString);

    /**
     * Method is used to generate the "dateString" used by the
     * vendor specific Timestamp cast implementation.
     *
     * @param _value    ReadableDateTime to be converted in a String
     * @return the str4 date time
     * @see AbstractDatabase#getTimestampValue(String)
     */
    public String getStr4DateTime(final ReadableDateTime _value)
    {
        return _value.toDateTime().toString(ISODateTimeFormat.dateHourMinuteSecondFraction());
    }

    /**
     * Get the vendor specific Boolean cast implementation.
     *
     * @param _value boolean that will be casted to an number for oracle
     * @return vendor specific implementation of boolean
     */
    public abstract Object getBooleanValue(final Boolean _value);

    /**
     * The method implements a delete all of database user specific objects
     * (e.g. tables, views etc...). The method is called before a complete
     * rebuild is done.
     *
     * @param _con sql connection
     * @throws SQLException if delete of the SQL data model failed
     */
    public abstract void deleteAll(final Connection _con)
        throws SQLException;

    /**
     * The method tests, if a view with given name exists.
     *
     * @param _con sql connection
     * @param _viewName name of view to test
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
        final ResultSet rs = metaData.getTables(null, null, _viewName.toLowerCase(), new String[] { "VIEW" });
        if (rs.next()) {
            ret = true;
        }
        rs.close();

        // then test with upper case
        if (!ret) {
            final ResultSet rsUC = metaData.getTables(null, null, _viewName.toUpperCase(), new String[] { "VIEW" });
            if (rsUC.next()) {
                ret = true;
            }
            rsUC.close();
        }

        return ret;
    }

    /**
     * Deletes given view <code>_name</code> in this database.
     *
     * @param _con SQL connection
     * @param _name name of the sequence
     * @return this instance
     * @throws SQLException if delete of the sequence failed
     */
    public abstract T deleteView(final Connection _con,
                                 final String _name)
        throws SQLException;

    /**
     * The method tests, if a view with given name exists.
     *
     * @param _con sql connection
     * @param _tableName name of table to test
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
        final ResultSet rs = metaData.getTables(null, null, _tableName.toLowerCase(), new String[] { "TABLE" });
        if (rs.next()) {
            ret = true;
        }
        rs.close();

        // then test with upper case
        if (!ret) {
            final ResultSet rsUC = metaData.getTables(null, null, _tableName.toUpperCase(), new String[] { "TABLE" });
            if (rsUC.next()) {
                ret = true;
            }
            rsUC.close();
        }
        return ret;
    }

    /**
     * Returns for given table name all information about the table and returns
     * them as instance of {@link TableInformation}. The information is cached
     * and NOT evaluated directly.
     *
     * @param _tableName name of SQL table for which the information is fetched
     * @return instance of {@link TableInformation} with table information
     * @throws SQLException if information about the table could not be fetched
     * @see TableInformation
     * @see #getRealTableInformation(Connection, String)
     * @see #cache
     */
    public TableInformation getCachedTableInformation(final String _tableName)
        throws SQLException
    {
        return this.cache.get(_tableName.toUpperCase());
    }

    /**
     * Evaluates for given table name all current information about the table
     * and returns them as instance of {@link TableInformation}.
     *
     * @param _con SQL connection
     * @param _tableName name of SQL table for which the information is fetched
     * @return instance of {@link TableInformation} with table information
     * @throws SQLException if information about the table could not be fetched
     * @see TableInformation
     * @see #getCachedTableInformation(String)
     */
    public TableInformation getRealTableInformation(final Connection _con,
                                                    final String _tableName)
        throws SQLException
    {
        final TableInformation tableInfo = new TableInformation(_tableName.toUpperCase());

        final Map<String, TableInformation> tableInfos = new HashMap<String, TableInformation>(1);
        tableInfos.put(_tableName.toUpperCase(), tableInfo);
        this.initTableInfoColumns(_con, null, tableInfos);
        this.initTableInfoUniqueKeys(_con, null, tableInfos);
        this.initTableInfoForeignKeys(_con, null, tableInfos);

        return tableInfo;
    }

    /**
     * A new SQL view <code>_view</code> is created. To create a correct view a
     * dummy select on the value one is done (which will overwritten).
     *
     * @param _con SQL connection
     * @param _view name of the view to create
     * @return this instance
     * @throws SQLException if the create of the table failed TODO: really
     *             neeeded? not referenced anymore...
     */
    @SuppressWarnings("unchecked")
    public T createView(final Connection _con,
                        final String _view)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();
        try {
            stmt.executeUpdate(new StringBuilder().append("create view ").append(_view)
                            .append(" as select 1").toString());
        } finally {
            stmt.close();
        }
        return (T) this;
    }

    /**
     * Method to create new sequence <code>_name</code>in this database. The
     * next time the value for sequence <code>_name</code> will return
     * <code>_value</code> (by calling {@link #nextSequence(Connection, String)}
     * ).
     *
     * @param _con SQL connection
     * @param _name name of the sequence
     * @param _startValue start value for the sequence
     * @return this instance
     * @throws SQLException on error
     */
    public abstract T createSequence(final Connection _con,
                                     final String _name,
                                     final long _startValue)
        throws SQLException;

    /**
     * Deletes given sequence <code>_name</code> in this database.
     *
     * @param _con SQL connection
     * @param _name name of the sequence
     * @return this instance
     * @throws SQLException if delete of the sequence failed
     */
    public abstract T deleteSequence(final Connection _con,
                                     final String _name)
        throws SQLException;

    /**
     * Method to check for an existing Sequence in this Database.
     *
     * @param _con SQL connection
     * @param _name name of the sequence
     * @return true if exists, else false
     * @throws SQLException on error
     */
    public abstract boolean existsSequence(final Connection _con,
                                           final String _name)
        throws SQLException;

    /**
     * Method to get the next value from a given sequence in this database.
     *
     * @param _con SQL connection
     * @param _name name of the sequence
     * @return next value in sequence
     * @throws SQLException on error
     */
    public abstract long nextSequence(final Connection _con,
                                      final String _name)
        throws SQLException;

    /**
     * Method to define current value for sequence <code>_name</code>. The next
     * time the value for sequence <code>_name</code> will return
     * <code>_value</code> (by calling {@link #nextSequence(Connection, String)}
     * ).
     *
     * @param _con SQL connection
     * @param _name name of the sequence
     * @param _value value for the sequence
     * @return this instance
     * @throws SQLException on error
     */
    public abstract T setSequence(final Connection _con,
                                  final String _name,
                                  final long _value)
        throws SQLException;

    /**
     * A new SQL table with unique column <code>ID</code> is created.
     *
     * @param _con SQL connection
     * @param _table name of the table to create
     * @return name of the table as it is insert into the database
     * @throws SQLException if the create of the table failed
     */
    public abstract T createTable(final Connection _con,
                                  final String _table)
        throws SQLException;

    /**
     * For a new created SQL table the column <code>ID</code> is update with a
     * foreign key to a parent table.
     *
     * @param _con SQL connection
     * @param _table name of the SQL table to update
     * @param _parentTable name of the parent table
     * @return this instance
     * @throws InstallationException if the update of the table failed
     */
    public T defineTableParent(final Connection _con,
                               final String _table,
                               final String _parentTable)
        throws InstallationException
    {
        return addForeignKey(_con, _table, _table + "_FK_ID", "ID", _parentTable + "(ID)", false);
    }

    /**
     * Defines a new created SQL table as auto incremented.
     *
     * @param _con SQL connection
     * @param _table name of the SQL table to update
     * @return this instance
     * @throws SQLException if the update of the table failed
     */
    public abstract T defineTableAutoIncrement(final Connection _con,
                                               final String _table)
        throws SQLException;

    /**
     * Adds a column to a SQL table.
     *
     * @param _con SQL connection
     * @param _tableName name of table to update
     * @param _columnName column to add
     * @param _columnType type of column to add
     * @param _defaultValue default value of the column (or null if not
     *            specified)
     * @param _length length of column to add (or 0 if not specified)
     * @param _scale scale of the column (or 0 if not specified)
     * @return this instance
     * @throws SQLException if the column could not be added to the tables
     */
    // CHECKSTYLE:OFF
    public T addTableColumn(final Connection _con,
                            final String _tableName,
                            final String _columnName,
                            final ColumnType _columnType,
                            final String _defaultValue,
                            final int _length,
                            final int _scale)
        throws SQLException
    {
        // CHECKSTYLE:ON
        final StringBuilder cmd = new StringBuilder();
        cmd.append("alter table ").append(getTableQuote()).append(_tableName).append(getTableQuote()).append(' ')
                        .append("add ").append(getColumnQuote()).append(_columnName).append(getColumnQuote())
                        .append(' ')
                        .append(getWriteSQLTypeName(_columnType));
        if (_length > 0) {
            cmd.append("(").append(_length);
            if (_scale > 0) {
                cmd.append(",").append(_scale);
            }
            cmd.append(")");
        }
        if (_defaultValue != null) {
            cmd.append(" default ").append(_defaultValue);
        }

        AbstractDatabase.LOG.debug("    ..SQL> " + cmd.toString());

        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally {
            stmt.close();
        }

        @SuppressWarnings("unchecked")
        final T ret = (T) this;
        return ret;
    }

    /**
     * @param _con          SQL connection
     * @param _tableName    name of table to update
     * @param _columnName   column to update
     * @param _isNotNull    actual isnull status
     * @return this instance
     * @throws SQLException if the column could not be added to the tables
     */
    public T updateColumnIsNotNull(final Connection _con,
                                   final String _tableName,
                                   final String _columnName,
                                   final boolean _isNotNull)
        throws SQLException
    {
        // to set not null it must be checked that there are no null values!
        boolean executable = true;
        if (_isNotNull) {
            executable = !check4NullValues(_con, _tableName, _columnName);
        }

        if (executable) {
            final StringBuilder cmd = new StringBuilder();
            cmd.append("alter table ").append(getTableQuote()).append(_tableName).append(getTableQuote())
                .append(getAlterColumnIsNotNull(_columnName, _isNotNull));

            AbstractDatabase.LOG.debug("    ..SQL> {}", cmd);

            // excecute statement
            final Statement stmt = _con.createStatement();
            try {
                stmt.execute(cmd.toString());
            } finally {
                stmt.close();
            }
        } else {
            AbstractDatabase.LOG.warn("Could not alter \"Not NUll\" on table '{}' column '{}'. "
                            + "Perhaps the column contains null values?", _tableName, _columnName);
        }
        @SuppressWarnings("unchecked")
        final T ret = (T) this;
        return ret;
    }

    /**
     * @param _columnName column to update is not null.
     * @param _isNotNull is the column not null
     * @return sql snipplet
     */
    protected abstract StringBuilder getAlterColumnIsNotNull(final String _columnName,
                                                             boolean _isNotNull);

    /**
     * Check if a specific column contains null values.
     * @param _con          SQL connection
     * @param _tableName    name of table to check
     * @param _columnName   column to check
     * @return true if the column contains nulls, else false
     * @throws SQLException on error
     */
    protected abstract boolean check4NullValues(final Connection _con,
                                                final String _tableName,
                                                final String _columnName)
        throws SQLException;

    /**
     * Adds a column to a SQL table.
     *
     * @param _con SQL connection
     * @param _tableName name of table to update
     * @param _columnName column to update
     * @param _columnType type of column to update
     * @param _length length of column to update (or 0 if not specified)
     * @param _scale scale of the column (or 0 if not specified)
     * @return this instance
     * @throws SQLException if the column could not be added to the tables
     */
    public T updateColumn(final Connection _con,
                          final String _tableName,
                          final String _columnName,
                          final ColumnType _columnType,
                          final int _length,
                          final int _scale)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder();
        cmd.append("alter table ").append(getTableQuote()).append(_tableName).append(getTableQuote())
            .append(getAlterColumn(_columnName, _columnType));
        if (_length > 0) {
            cmd.append("(").append(_length);
            if (_scale > 0) {
                cmd.append(",").append(_scale);
            }
            cmd.append(")");
        }
        AbstractDatabase.LOG.debug("    ..SQL> " + cmd.toString());

        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally {
            stmt.close();
        }
        @SuppressWarnings("unchecked")
        final T ret = (T) this;
        return ret;
    }

    /**
     * @param _columnName column to update
     * @param _columnType type of column to update
     * @return sql snipplet
     */
    protected abstract StringBuilder getAlterColumn(final String _columnName,
                                                    final ColumnType _columnType);

    /**
     * Adds a new unique key to given table name.
     *
     * @param _con SQL connection
     * @param _tableName name of table for which the unique key must be created
     * @param _uniqueKeyName name of unique key
     * @param _columns comma separated list of column names for which the unique
     *            key is created
     * @return this instance
     * @throws SQLException if the unique key could not be created
     */
    public T addUniqueKey(final Connection _con,
                          final String _tableName,
                          final String _uniqueKeyName,
                          final String _columns)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder();
        cmd.append("alter table ").append(_tableName).append(" ")
                        .append("add constraint ").append(_uniqueKeyName).append(" ")
                        .append("unique(").append(_columns).append(")");

        AbstractDatabase.LOG.debug("    ..SQL> " + cmd.toString());

        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally {
            stmt.close();
        }

        @SuppressWarnings("unchecked") final T ret = (T) this;
        return ret;
    }

    /**
     * Adds a foreign key to given SQL table.
     *
     * @param _con SQL connection
     * @param _tableName name of table for which the foreign key must be created
     * @param _foreignKeyName name of foreign key to create
     * @param _key key in the table (column name)
     * @param _reference external reference (external table and column name)
     * @param _cascade if the value in the external table is deleted, should
     *            this value also automatically deleted?
     * @return this instance
     * @throws InstallationException if foreign key could not be defined for SQL
     *             table
     */
    public T addForeignKey(final Connection _con,
                           final String _tableName,
                           final String _foreignKeyName,
                           final String _key,
                           final String _reference,
                           final boolean _cascade)
        throws InstallationException
    {
        final StringBuilder cmd = new StringBuilder()
                        .append("alter table ").append(_tableName).append(" ")
                        .append("add constraint ").append(_foreignKeyName).append(" ")
                        .append("foreign key(").append(_key).append(") ")
                        .append("references ").append(_reference);
        if (_cascade) {
            cmd.append(" on delete cascade");
        }
        AbstractDatabase.LOG.debug("    ..SQL> " + cmd.toString());

        try {
            final Statement stmt = _con.createStatement();
            try {
                stmt.execute(cmd.toString());
            } finally {
                stmt.close();
            }
        } catch (final SQLException e) {
            throw new InstallationException("Foreign key could not be created. SQL statement was:\n"
                            + cmd.toString(), e);
        }
        @SuppressWarnings("unchecked")
        final T ret = (T) this;
        return ret;
    }

    /**
     * Adds a new check key to given SQL table.
     *
     * @param _con SQL connection
     * @param _tableName name of the SQL table for which the check key must be
     *            created
     * @param _checkKeyName name of check key to create
     * @param _condition condition of the check key
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

        AbstractDatabase.LOG.debug("    ..SQL> " + cmd.toString());
        // excecute statement
        final Statement stmt = _con.createStatement();
        try {
            stmt.execute(cmd.toString());
        } finally {
            stmt.close();
        }
    }

    /**
     * Returns the quote used to select tables.
     *
     * @return always empty string as default
     */
    public String getTableQuote()
    {
        return "";
    }

    /**
     * Returns the quote used to select columns.
     *
     * @return always empty string as default
     */
    public String getColumnQuote()
    {
        return "";
    }

    /**
     * @param _part Part the SQL is needed for
     * @return String
     */
    public String getSQLPart(final SQLPart _part)
    {
        return _part.getDefaultValue();
    }

    /**
     * @param _value String value to be escaped
     * @return escaped value in "'"
     */
    public String escapeForWhere(final String _value)
    {
        return "'" + StringUtils.replace(_value, "'", "''") + "'";
    }

    /**
     * @param _value value to be prepared for use in a match
     * @return string prepared for match
     */
    public String prepare4Match(final String _value)
    {
        // Remove double escapes
        String ret = StringUtils.replace(_value, "\\\\", "\\");
        // escape '%' percent and '_' underscore
        ret = StringUtils.replace(ret, "%", "\\%");
        ret = StringUtils.replace(ret, "_", "\\_");
        // replace any '*' that is not escaped by a '\'
        ret = ret.replaceAll("(?<!\\\\)\\" + AbstractDatabase.WILDCARDPATTERN, "%");
        // remove the escapecharacter from the '\*' and replace with a simple '*'
        ret = ret.replaceAll("(?>\\\\)\\" + AbstractDatabase.WILDCARDPATTERN, AbstractDatabase.WILDCARDPATTERN);
        // replace any '?' that is not escaped by a '_'
        ret = ret.replaceAll("(?<!\\\\)\\" + AbstractDatabase.SINGLECHARACTERPATTERN, "_");
        // remove the escapecharacter from the '\?' and replace with a simple '?'
        ret = ret.replaceAll("(?>\\\\)\\" + AbstractDatabase.SINGLECHARACTERPATTERN,
                        AbstractDatabase.SINGLECHARACTERPATTERN);
        return ret;
    }

    /**
     * <p>
     * This integer is used for the maximum numbers of Values inside an
     * expression.
     * </p>
     * <p>
     * The value is used in the OneRounQuery. The SQL statement looks like
     * "SELECT...WHERE..IN (val1,val2,val3,...valn)" The integer is the maximum
     * value for n before making a new Select.
     * </p>
     *
     * @return max Number of Value in an Expression, -1 if no max is known
     */
    public int getMaxExpressions()
    {
        return -1;
    }

    /**
     * A new id for given column of a SQL table is returned (e.g. with
     * sequences). This abstract class always throws a SQLException, because for
     * default, it is not needed to implement (only if the JDBC drive does not
     * implement method 'getGeneratedKeys' for java.sql.Statements).
     *
     * @param _con sql connection
     * @param _table sql table for which a new id must returned
     * @param _column sql table column for which a new id must returned
     * @return new id number
     * @throws SQLException always, because method itself is not implemented not
     *             not allowed to call
     */
    public long getNewId(final Connection _con,
                         final String _table,
                         final String _column)
        throws SQLException
    {
        throw new SQLException("method 'getNewId' not imlemented");
    }

    /**
     * The method returns if a database implementation supports to get generated
     * keys while inserting a new line in a SQL table.
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
     * {@link java.sql.Statement#RETURN_GENERATED_KEYS} is given for the insert.
     *
     * @return always <i>false</i> because not implemented in this class
     * @see #supportsGetGeneratedKeys
     */
    public boolean supportsMultiGeneratedKeys()
    {
        return false;
    }

    /**
     * The method returns if a database implementation supports for blobs binary
     * input stream supports the available method or not.
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
     *         big transactions
     */
    public boolean supportsBigTransactions()
    {
        return true;
    }

    /**
     * @param _name name of the constraint as defined
     * @return name as used by the database
     * @throws EFapsException on error
     */
    public String getConstrainName(final String _name)
        throws EFapsException
    {
        return _name;
    }

    /**
     * @param _name name of the table as defined
     * @return name as used by the database
     * @throws EFapsException on error
     */
    public String getTableName(final String _name)
        throws EFapsException
    {
        return _name;
    }

    /**
     * Get the Dialect to access the DataBase using Hibernate.
     * @return the Dialect for Hibernate
     */
    public abstract String getHibernateDialect();

    /**
     * Instantiate the given DB class name and returns them.
     *
     * @param _dbClassName name of the class to instantiate
     * @return new database definition instance
     * @throws ClassNotFoundException if class for the DB is not found
     * @throws InstantiationException if DB class could not be instantiated
     * @throws IllegalAccessException if DB class could not be accessed
     */
    public static AbstractDatabase<?> findByClassName(final String _dbClassName)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return (AbstractDatabase<?>) Class.forName(_dbClassName).newInstance();
    }

    /**
     * <p>
     * Fetches all table name for all tables and views. If a SQL statement is
     * given, this SQL statement is used instead of using the JDBC meta data
     * methods. The SQL select statement must define this column
     * <ul>
     * <li><b><code>TABLE_NAME</code></b> for the real name of the table.</li>
     * </ul>
     * </p>
     *
     * @param _con SQL connection
     * @param _sql SQL statement which must be executed if the JDBC
     *            functionality does not work (or null if JDBC meta data is used
     *            to fetch all tables and views)
     * @param _cache4Name map used to fetch depending on the table name the
     *            related table information
     * @throws SQLException if information could not be fetched from the data
     *             base
     */
    protected void initTableInfo(final Connection _con,
                                 final String _sql,
                                 final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        Statement stmt = null;
        final ResultSet rs;
        if (_sql == null) {
            rs = _con.getMetaData().getTables(null, null, "%", new String[] { "TABLE", "VIEW" });
        } else        {
            stmt = _con.createStatement();
            rs = stmt.executeQuery(_sql);
        }
        try {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME").toUpperCase();
                // ignore the tables managed by hibernate
                if (!tableName.startsWith(NamingStrategy.HIBERNATEPREFIX.toUpperCase())
                                && (tableName.startsWith("T_") || (tableName.startsWith("V_")))) {
                    _cache4Name.put(tableName, new TableInformation(tableName));
                }
            }
        } finally {
            rs.close();
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * <p>
     * Fetches all unique keys for all tables. If a SQL statement is given, this
     * SQL statement is used instead of using the JDBC meta data methods. The
     * SQL select statement must define this four columns
     * <ul>
     * <li><b><code>TABLE_NAME</code></b> for the real name of the table,</li>
     * <li><b><code>COLUMN_NAME</code></b> for the name of a column,</li>
     * <li><b><code>TYPE_NAME</code></b> for the name of the column type,</li>
     * <li><b><code>COLUMN_SIZE</code></b> for the size of the column,</li>
     * <li><b><code>DECIMAL_DIGITS</code></b> for the count of decimal digits
     * (if the <b><code>TYPE_NAME</code></b> is number) and</li>
     * <li><b><code>IS_NULLABLE</code></b> if the column could have no value
     * (with value &quot;NO&quot; if no null value is allowed).</li>
     * </ul>
     * </p>
     *
     * @param _con SQL connection
     * @param _sql SQL statement which must be executed if the JDBC
     *            functionality does not work (or null if JDBC meta data is used
     *            to fetch the table columns)
     * @param _cache4Name map used to cache depending on the table name the
     *            related table information
     * @throws SQLException if column information could not be fetched
     */
    protected void initTableInfoColumns(final Connection _con,
                                        final String _sql,
                                        final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        Statement stmt = null;
        final ResultSet rs;
        if (_sql == null) {
            rs = _con.getMetaData().getColumns(null, null, "%", "%");
        } else        {
            stmt = _con.createStatement();
            rs = stmt.executeQuery(_sql);
        }
        try {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME").toUpperCase();
                if (_cache4Name.containsKey(tableName)) {
                    final String colName = rs.getString("COLUMN_NAME").toUpperCase();
                    final String typeName = rs.getString("TYPE_NAME").toLowerCase();
                    final Set<AbstractDatabase.ColumnType> colTypes = AbstractDatabase.this
                                    .getReadColumnTypes(typeName);
                    if (colTypes == null) {
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

    /**
     * <p>
     * Fetches all unique keys for all tables. If a SQL statement is given, this
     * SQL statement is used instead of using the JDBC meta data methods. The
     * SQL select statement must define this four columns
     * <ul>
     * <li><b><code>TABLE_NAME</code></b> for the real name of the table,</li>
     * <li><b><code>INDEX_NAME</code></b> for the real name of the unique key
     * name,</li>
     * <li><b><code>COLUMN_NAME</code></b> for the name of a column within the
     * unique key and</li>
     * <li><b><code>ORDINAL_POSITION</code></b> for the position of the column
     * name within the unique key.</li>
     * </ul>
     * If more than one column is used to define the unique key, one line for
     * each column name with same index name must be used.
     * </p>
     *
     * @param _con SQL connection
     * @param _sql SQL statement which must be executed if the JDBC
     *            functionality does not work (or null if JDBC meta data is used
     *            to fetch the unique keys)
     * @param _cache4Name map used to fetch depending on the table name the
     *            related table information
     * @throws SQLException if unique keys could not be fetched
     */
    protected void initTableInfoUniqueKeys(final Connection _con,
                                           final String _sql,
                                           final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        Statement stmt = null;
        final ResultSet rs;
        if (_sql == null) {
            rs = _con.getMetaData().getIndexInfo(null, null, "%", true, false);
        } else        {
            stmt = _con.createStatement();
            rs = stmt.executeQuery(_sql);
        }
        try {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME").toUpperCase();
                if (_cache4Name.containsKey(tableName)) {
                    final String ukName = rs.getString("INDEX_NAME").toUpperCase();
                    final String colName = rs.getString("COLUMN_NAME").toUpperCase();
                    final int colIdx = rs.getInt("ORDINAL_POSITION");
                    _cache4Name.get(tableName).addUniqueKeyColumn(ukName, colIdx, colName);
                }
            }
        } finally {
            rs.close();
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * <p>
     * Fetches all foreign keys for all tables. If a SQL statement is given,
     * this SQL statement is used instead of using the JDBC meta data methods.
     * The SQL select statement must define this six columns
     * <ul>
     * <li><b><code>TABLE_NAME</code></b> for the real name of the table,</li>
     * <li><b><code>FK_NAME</code></b> for the real name of the foreign key
     * name,</li>
     * <li><b><code>FKCOLUMN_NAME</code></b> for the name of the column for
     * which the foreign key is defined,</li>
     * <li><b><code>PKTABLE_NAME</code></b> for the name of the referenced
     * table,</li>
     * <li><b><code>PKCOLUMN_NAME</code></b> for the name of column within the
     * referenced table and</li>
     * <li><b><code>DELETE_RULE</code></b> defining the rule what happens in the
     * case a row of the table is deleted (with value
     * {@link DatabaseMetaData#importedKeyCascade} in the case the delete is
     * cascaded).</li>
     * </ul>
     * </p>
     *
     * @param _con SQL connection
     * @param _sql SQL statement which must be executed if the JDBC
     *            functionality does not work (or null if JDBC meta data is used
     *            to fetch the foreign keys)
     * @param _cache4Name map used to fetch depending on the table name the
     *            related table information
     * @throws SQLException if foreign keys could not be fetched
     */
    protected void initTableInfoForeignKeys(final Connection _con,
                                            final String _sql,
                                            final Map<String, TableInformation> _cache4Name)
        throws SQLException
    {
        Statement stmt = null;
        final ResultSet rs;
        if (_sql == null) {
            rs = _con.getMetaData().getImportedKeys(null, null, "%");
        } else        {
            stmt = _con.createStatement();
            rs = stmt.executeQuery(_sql);
        }
        try {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME").toUpperCase();
                if (_cache4Name.containsKey(tableName)) {
                    final String fkName = rs.getString("FK_NAME").toUpperCase();
                    final String colName = rs.getString("FKCOLUMN_NAME").toUpperCase();
                    final String refTableName = rs.getString("PKTABLE_NAME").toUpperCase();
                    final String refColName = rs.getString("PKCOLUMN_NAME").toUpperCase();
                    final boolean cascade = rs.getInt("DELETE_RULE") == DatabaseMetaData.importedKeyCascade;
                    _cache4Name.get(tableName).addForeignKey(fkName, colName, refTableName, refColName, cascade);
                }
            }
        } finally {
            rs.close();
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Get the RowProcessor responsible to read the data from a recordset into
     * eFaps.
     *
     * @return a RowProcessor
     */
    public abstract RowProcessor getRowProcessor();

    /**
     * Implements the cache for the table information.
     *
     * @see AbstractDatabase#cache
     * @see TableInformation
     */
    private class TableInfoCache
        extends AbstractCache<TableInformation>
    {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, TableInformation> _cache4Id,
                                 final Map<String, TableInformation> _cache4Name,
                                 final Map<UUID, TableInformation> _cache4UUID)
            throws CacheReloadException
        {
            try {
                final Connection con = Context.getThreadContext().getConnectionResource().getConnection();
                AbstractDatabase.this.initTableInfo(con, null, _cache4Name);
                AbstractDatabase.this.initTableInfoColumns(con, null, _cache4Name);
                AbstractDatabase.this.initTableInfoUniqueKeys(con, null, _cache4Name);
                AbstractDatabase.this.initTableInfoForeignKeys(con, null, _cache4Name);
            } catch (final SQLException e) {
                throw new CacheReloadException("cache for table information could not be read", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("cache for table information could not be read", e);
            }
        }
    }
}

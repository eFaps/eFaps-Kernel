/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.admin.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * This is the class for the table description. The table description holds
 * information in which table attributes are stored.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class SQLTable
    extends AbstractDataModelObject
{
    /**
     * This is the SQL select statement to select all SQL tables from the
     * database.
     */
    private static final SQLSelect SQL_SELECT = new SQLSelect()
                                                    .column("ID")
                                                    .column("UUID")
                                                    .column("NAME")
                                                    .column("SQLTABLE")
                                                    .column("SQLCOLUMNID")
                                                    .column("SQLCOLUMNTYPE")
                                                    .column("DMTABLEMAIN")
                                                    .from("V_ADMINSQLTABLE");

    /**
     * Stores all instances of SQLTable.
     *
     * @see #getCache
     */
    private static final SQLTableCache CACHE = new SQLTableCache();

    /**
     * Instance variable for the name of the SQL table.
     *
     * @see #getSqlTable
     */
    private final String sqlTable;

    /**
     * This instance variable stores the SQL column name of the id of a table.
     *
     * @see #getSqlColId
     */
    private final String sqlColId;

    /**
     * The instance variable stores the SQL column name of the type id.
     *
     * @see #getSqlColType
     */
    private final String sqlColType;

    /**
     * Stores the information about the SQL table within the database.
     *
     * @see #getTableInformation
     */
    private final TableInformation tableInformation;

    /**
     * The instance variable stores the main table for this table instance. The
     * main table is the table, which holds the information about the SQL select
     * statement to get a new id. Also the main table must be inserted as first
     * insert (e.g. the id in the table with a main table has a foreign key to
     * the id of the main table).
     *
     * @see #getMainTable()
     */
    private SQLTable mainTable = null;

    /**
     * The instance variable stores all types which stores information in this
     * table.
     *
     * @see #getTypes()
     */
    private final Set<Type> types = new HashSet<Type>();

    /**
     * The instance variables is set to <i>true</i> if this table is only a
     * read only SQL table. This means, that no insert and no update on this
     * table is allowed and made.
     *
     * @see #isReadOnly()
     */
    private boolean readOnly = false;

    /**
     * This is the constructor for class {@link Attribute}. Every instance of
     * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
     * identifier (parameter <i>_id</i>).
     *
     * @param _con          Connection
     * @param _id eFaps     id of the SQL table
     * @param _uuid         unique identifier
     * @param _name         eFaps name of the SQL table
     * @param _sqlTable     name of the SQL Table in the database
     * @param _sqlColId     name of column for the id within SQL table
     * @param _sqlColType   name of column for the type within SQL table
     * @throws SQLException on error
     */
    private SQLTable(final Connection _con,
                     final long _id,
                     final String _uuid,
                     final String _name,
                     final String _sqlTable,
                     final String _sqlColId,
                     final String _sqlColType)
        throws SQLException
    {
        super(_id, _uuid, _name);
        this.sqlTable = _sqlTable.trim();
        this.sqlColId = _sqlColId.trim();
        this.sqlColType = (_sqlColType != null) ? _sqlColType.trim() : null;
        this.tableInformation = Context.getDbType().getCachedTableInformation(this.sqlTable);
    }

    /**
     * The instance method adds a new type to the type list.
     *
     * @param _type TYpe to add
     * @see #types
     */
    protected void add(final Type _type)
    {
        this.types.add(_type);
    }

    /**
     * The instance method sets a new property value.
     *
     * @param _name name of the property
     * @param _value value of the property
     * @throws CacheReloadException on error
     */
    @Override
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if (_name.equals("ReadOnly")) {
            this.readOnly = "true".equalsIgnoreCase("true");
        }
    }

    /**
     * This is the getter method for instance variable {@link #sqlTable}.
     *
     * @return value of instance variable {@link #sqlTable}
     * @see #sqlTable
     */
    public String getSqlTable()
    {
        return this.sqlTable;
    }

    /**
     * This is the getter method for instance variable {@link #sqlColId}.
     *
     * @return value of instance variable {@link #sqlColId}
     * @see #sqlColId
     */
    public String getSqlColId()
    {
        return this.sqlColId;
    }

    /**
     * This is the getter method for instance variable {@link #sqlColType}.
     *
     * @return value of instance variable {@link #sqlColType}
     * @see #sqlColType
     */
    public String getSqlColType()
    {
        return this.sqlColType;
    }

    /**
     * This is the getter method for instance variable {@link #tableInformation}
     * .
     *
     * @return value of instance variable {@link #tableInformation}
     * @see #tableInformation
     */
    public TableInformation getTableInformation()
    {
        return this.tableInformation;
    }

    /**
     * This is the getter method for instance variable {@link #mainTable}.
     *
     * @return value of instance variable {@link #mainTable}
     * @see #mainTable
     */
    public SQLTable getMainTable()
    {
        return this.mainTable;
    }

    /**
     * This is the getter method for instance variable {@link #types}.
     *
     * @return value of instance variable {@link #types}
     * @see #types
     */
    public Set<Type> getTypes()
    {
        return this.types;
    }

    /**
     * This is the getter method for instance variable {@link #readOnly}.
     *
     * @return value of instance variable {@link #readOnly}
     * @see #readOnly
     */
    public boolean isReadOnly()
    {
        return this.readOnly;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     * @param _class Clas that started the initialization
     */
    public static void initialize(final Class<?> _class)
    {
        SQLTable.CACHE.initialize(_class);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        SQLTable.initialize(SQLTable.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link SQLTable}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link SQLTable}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static SQLTable get(final long _id)
        throws CacheReloadException
    {
        return SQLTable.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link SQLTable}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link SQLTable}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static SQLTable get(final String _name)
        throws CacheReloadException
    {
        return SQLTable.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link SQLTable}.
     * @param _uuid UUID the tanel is wanted for
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static SQLTable get(final UUID _uuid)
        throws CacheReloadException
    {
        return SQLTable.CACHE.get(_uuid);
    }

    /**
     * Cache for SQLTable.
     */
    private static class SQLTableCache
        extends Cache<SQLTable>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, SQLTable> _cache4Id,
                                 final Map<String, SQLTable> _cache4Name,
                                 final Map<UUID, SQLTable> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {
                    final Map<Long, Long> mainTables = new HashMap<Long, Long>();

                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(SQLTable.SQL_SELECT.getSQL());
                    while (rs.next()) {
                        final long id = rs.getLong(1);
                        final SQLTable table = new SQLTable(con.getConnection(),
                                                            id,
                                                            rs.getString(2),
                                                            rs.getString(3),
                                                            rs.getString(4),
                                                            rs.getString(5),
                                                            rs.getString(6));
                        _cache4Id.put(table.getId(), table);
                        _cache4Name.put(table.getName(), table);
                        _cache4UUID.put(table.getUUID(), table);
                        final long tableMainId = rs.getLong(7);
                        if (tableMainId > 0) {
                            mainTables.put(id, tableMainId);
                        }
                        table.readFromDB4Properties();
                    }
                    rs.close();

                    // initialize main tables
                    for (final Map.Entry<Long, Long> entry : mainTables.entrySet()) {
                        final SQLTable table = _cache4Id.get(entry.getKey());
                        final SQLTable mainTable = _cache4Id.get(entry.getValue());
                        table.mainTable = mainTable;
                    }

                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read sql tables", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read sql tables", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read sql tables", e);
                    }
                }
            }
        }
    }
}

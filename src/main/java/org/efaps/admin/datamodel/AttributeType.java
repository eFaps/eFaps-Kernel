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

package org.efaps.admin.datamodel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 *
 *          TODO: description
 */
public class AttributeType
    extends AbstractDataModelObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeType.class);

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("UUID")
                    .column("CLASSNAME")
                    .column("CLASSNAMEUI")
                    .column("ALWAYSUPDATE")
                    .column("CREATEUPDATE")
                    .from("V_DMATTRIBUTETYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("UUID")
                    .column("CLASSNAME")
                    .column("CLASSNAMEUI")
                    .column("ALWAYSUPDATE")
                    .column("CREATEUPDATE")
                    .from("V_DMATTRIBUTETYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = AttributeType.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = AttributeType.class.getName() + ".Name";

    /**
     * The instance variable store the class representation for the attribute
     * type. With the class representation, a new instance of the value can be
     * created.
     *
     * @see #getClassRepr()
     * @see #setClassRepr(String)
     */
    private IAttributeType dbAttrType;

    /**
     * The instance variable stores the instance for the user interface.
     */
    private IUIProvider uiAttrType;

    /**
     * The instance variable store the behavior, if an update is made. If the
     * value is set to <i>true</i>, the attribute must be always updated.
     *
     * @see #isAlwaysUpdate()
     */
    private boolean alwaysUpdate = false;

    /**
     * The instance variable store the behavior, if an insert is made. If the
     * value is set to <i>true</i>, the attribute must be updated for an insert.
     *
     * @see #isCreateUpdate()
     */
    private boolean createUpdate = false;

    /**
     * The instance for the user interface provider.
     */
    private IUIProvider uiProvider;

    /**
     * This is the constructor for class {@link Attribute}. Every instance of
     * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
     * identifier (parameter <i>_id</i>).
     *
     * @param _id id of the attribute
     * @param _uuid universal unique identifier
     * @param _name name of the instance
     * @param _dbAttrTypeName name of the database attribute type
     * @param _uiAttrTypeName name of the user interface attribute type
     * @throws EFapsException if attribute type for the data model or user
     *             interface could not be initialized
     */
    protected AttributeType(final long _id,
                            final String _uuid,
                            final String _name,
                            final String _dbAttrTypeName,
                            final String _uiAttrTypeName)
        throws EFapsException
    {
        super(_id, _uuid, _name);

        try {
            this.dbAttrType = (IAttributeType) Class.forName(_dbAttrTypeName).newInstance();
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(getClass(), "setUIClass.ClassNotFoundException", e, _uiAttrTypeName);
        } catch (final InstantiationException e) {
            throw new EFapsException(getClass(), "newInstance.InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(getClass(), "newInstance.IllegalAccessException", e);
        }

        try {
            this.uiAttrType = (IUIProvider) Class.forName(_uiAttrTypeName).newInstance();
            this.uiProvider = (IUIProvider) Class.forName(_uiAttrTypeName).newInstance();
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(getClass(), "setUIClass.ClassNotFoundException", e, _uiAttrTypeName);
        } catch (final InstantiationException e) {
            throw new EFapsException(getClass(), "setUIClass.InstantiationException", e, _uiAttrTypeName);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(getClass(), "setUIClass.IllegalAccessException", e, _uiAttrTypeName);
        } catch (final ClassCastException e) {
            throw new EFapsException(getClass(), "setUIClass.ClassCastException", e, _uiAttrTypeName);
        }
    }

    /**
     *
     *
     * @return new instance of the class representation
     * @see #dbAttrType
     */
    public IAttributeType getDbAttrType()
    {
        return this.dbAttrType;
    }

    /**
     * This is the getter method for instance variable {@link #classRepr}.
     *
     * @return value of instance variable {@link #classRepr}
     * @see #classRepr
     * @see #setClassRepr(Class)
     */
    public Class<?> getClassRepr()
    {
        return this.dbAttrType.getClass();
    }

    /**
     * This is the getter method for instance variable {@link #uiProvider}.
     *
     * @return value of instance variable {@link #uiProvider}
     */
    public IUIProvider getUIProvider()
    {
        return this.uiProvider;
    }

    /**
     * This is the getter method for instance variable {@link #alwaysUpdate}.
     *
     * @return value of instance variable {@link #alwaysUpdate}
     * @see #alwaysUpdate
     * @see #setAlwaysUpdate
     */
    public boolean isAlwaysUpdate()
    {
        return this.alwaysUpdate;
    }

    /**
     * This is the getter method for instance variable {@link #createUpdate}.
     *
     * @return value of instance variable {@link #createUpdate}
     * @see #createUpdate
     * @see #setCreateUpdate
     */
    public boolean isCreateUpdate()
    {
        return this.createUpdate;
    }

    /**
     * @return string representation of this attribute type
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                        .appendSuper(super.toString())
                        .append("dbAttrType", this.dbAttrType)
                        .append("uiAttrType", this.uiAttrType)
                        .append("alwaysUpdate", this.alwaysUpdate)
                        .append("createUpdate", this.createUpdate)
                        .toString();
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof AttributeType) {
            ret = ((AttributeType) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }

    /**
     *
     * @param _class attribute type class
     */
    public static void initialize(final Class<?> _class)
    {
        if (InfinispanCache.get().exists(AttributeType.IDCACHE)) {
            InfinispanCache.get().<Long, AttributeType>getCache(AttributeType.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, AttributeType>getCache(AttributeType.IDCACHE)
                            .addListener(new CacheLogListener(AttributeType.LOG));
        }
        if (InfinispanCache.get().exists(AttributeType.NAMECACHE)) {
            InfinispanCache.get().<String, AttributeType>getCache(AttributeType.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, AttributeType>getCache(AttributeType.NAMECACHE)
                            .addListener(new CacheLogListener(AttributeType.LOG));
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        AttributeType.initialize(AttributeType.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link AttributeType}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link AttributeType}
     * @see #CACHE
     * @throws CacheReloadException on error
     */
    public static AttributeType get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, AttributeType> cache = InfinispanCache.get().<Long, AttributeType>getCache(
                        AttributeType.IDCACHE);
        if (!cache.containsKey(_id)) {
            AttributeType.getAttributeTypeFromDB(AttributeType.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link AttributeType}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link AttributeType}
     * @see #CACHE
     * @throws CacheReloadException on error
     */
    public static AttributeType get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, AttributeType> cache = InfinispanCache.get().<String, AttributeType>getCache(
                        AttributeType.NAMECACHE);
        if (!cache.containsKey(_name)) {
            AttributeType.getAttributeTypeFromDB(AttributeType.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * @param _role AttributeType to be cached
     */
    private static void cacheAttributeType(final AttributeType _role)
    {
        final Cache<String, AttributeType> nameCache = InfinispanCache.get()
                        .<String, AttributeType>getIgnReCache(AttributeType.NAMECACHE);
        nameCache.putIfAbsent(_role.getName(), _role);

        final Cache<Long, AttributeType> idCache = InfinispanCache.get().<Long, AttributeType>getIgnReCache(
                        AttributeType.IDCACHE);
        idCache.putIfAbsent(_role.getId(), _role);
    }

    /**
     * @param _sql      SQL Statement to be executed
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getAttributeTypeFromDB(final String _sql,
                                                  final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(2).trim();
                    String uuid = rs.getString(3);
                    uuid = uuid == null ? null : uuid.trim();

                    if (AttributeType.LOG.isDebugEnabled()) {
                        AttributeType.LOG.debug("read attribute type '" + name + "' " + "(id = " + id + ", uuid = '"
                                        + uuid + "')");
                    }

                    final AttributeType attrType = new AttributeType(id,
                                    uuid,
                                    name,
                                    rs.getString(4).trim(),
                                    rs.getString(5).trim());
                    if (rs.getInt(6) != 0) {
                        attrType.alwaysUpdate = true;
                    }
                    if (rs.getInt(7) != 0) {
                        attrType.createUpdate = true;
                    }
                    AttributeType.cacheAttributeType(attrType);
                }
                ret = true;
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            if (con != null && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read roles", e);
                }
            }
        }
        return ret;
    }
}

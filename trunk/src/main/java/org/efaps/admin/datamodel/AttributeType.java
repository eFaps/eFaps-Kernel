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

package org.efaps.admin.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO: description
 */
public class AttributeType
    extends AbstractDataModelObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeType.class);

    /**
     * This is the SQL select statement to select all attribute types from the
     * database.
     */
    private static final String SQL_SELECT = "select ID,NAME,UUID,CLASSNAME,CLASSNAMEUI,"
                    + "ALWAYSUPDATE,CREATEUPDATE from V_DMATTRIBUTETYPE";

    /**
     * Stores all instances of class {@link AttributeType}.
     *
     * @see #initialize()
     * @see #get(long)
     * @see #get(String)
     */
    private static AttributeTypeCache CACHE = new AttributeTypeCache();

    /**
     * The instance variable store the class representation for the attribute
     * type. With the class representation, a new instance of the value can be
     * created.
     *
     * @see #getClassRepr()
     * @see #setClassRepr(String)
     */
    private Class<?> classRepr = null;

    /**
     * The instance variable stores the instance for the user interface.
     */
    private UIInterface ui = null;

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
     * This is the constructor for class {@link Attribute}. Every instance of
     * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
     * identifier (parameter <i>_id</i>).
     *
     * @param _id id of the attribute
     * @param _uuid universal unique identifier
     * @param _name name of the instance
     */
    protected AttributeType(final long _id, final String _uuid, final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     *
     *
     * @return new instance of the class representation
     * @throws EFapsException if instance for {@link #classRepr} could not be
     *                        initiated
     * @see #classRepr
     */
    public IAttributeType newInstance()
        throws EFapsException
    {
        IAttributeType ret = null;
        try {
            ret = (IAttributeType) this.classRepr.newInstance();
        } catch (final InstantiationException e) {
            throw new EFapsException(getClass(), "newInstance.InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(getClass(), "newInstance.IllegalAccessException", e);
        }
        return ret;
    }

    /**
     * The parameter <i>_classRepr</i> is the name of the class representation.
     * The method searches for the class and stores the class representation in
     * instance variable {@link #classRepr}.
     *
     * @param _classRepr class name of the class representation
     * @throws ClassNotFoundException if class <code>_classRepr</code> could
     *                                not be found
     * @see #classRepr
     * @see #setClassRepr(Class)
     */
    private void setClassRepr(final String _classRepr)
        throws ClassNotFoundException
    {
        this.classRepr = Class.forName(_classRepr);
    }

    /**
     * @param _className    name of the user interface class
     * @throws EFapsException if class could not be found or not instantiated
     */
    private void setUI(final String _className)
        throws EFapsException
    {
        try {
            setUI((UIInterface) Class.forName(_className).newInstance());
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(getClass(), "setUIClass.ClassNotFoundException", e, _className);
        } catch (final InstantiationException e) {
            throw new EFapsException(getClass(), "setUIClass.InstantiationException", e, _className);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(getClass(), "setUIClass.IllegalAccessException", e, _className);
        } catch (final ClassCastException e) {
            throw new EFapsException(getClass(), "setUIClass.ClassCastException", e, _className);
        }
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
        return this.classRepr;
    }

    /**
     * This is the getter method for instance variable {@link #ui}.
     *
     * @return value of instance variable {@link #ui}
     * @see #ui
     * @see #setUI(Class)
     */
    public UIInterface getUI()
    {
        return this.ui;
    }

    /**
     * This is the setter method for instance variable {@link #ui}.
     *
     * @param _ui new value for instance variable {@link #ui}
     * @see #ui
     * @see #getUI
     */
    private void setUI(final UIInterface _ui)
    {
        this.ui = _ui;
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
    @Override()
    public String toString()
    {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("classRepr", this.classRepr)
            .append("alwaysUpdate", this.alwaysUpdate)
            .append("createUpdate", this.createUpdate)
            .toString();
    }

    /**
     *
     * @param _class    attribute type class
     */
    public static void initialize(final Class<?> _class)
    {
        AttributeType.CACHE.initialize(_class);
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
     */
    public static AttributeType get(final long _id)
    {
        return AttributeType.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link AttributeType}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link AttributeType}
     * @see #CACHE
     */
    public static AttributeType get(final String _name)
    {
        return AttributeType.CACHE.get(_name);
    }

    /**
     * Cache class for attribute types.
     *
     * @see AttributeType#CACHE
     */
    private static class AttributeTypeCache
        extends Cache<AttributeType>
    {
        /**
         * @param _cache4Id     map depending on the id and the attribute type
         *                      instance which will be used as cache
         * @param _cache4Name   map depending on the name and the attribute
         *                      type instance which will be used as cache
         * @param _cache4UUID   map depending on the UUID and the attribute
         *                      type instance which will be used as cache
         * @throws CacheReloadException if cache could not be reloaded
         */
        @Override()
        protected void readCache(final Map<Long, AttributeType> _cache4Id,
                                 final Map<String, AttributeType> _cache4Name,
                                 final Map<UUID, AttributeType> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {
                    stmt = con.getConnection().createStatement();
                    final ResultSet rs = stmt.executeQuery(AttributeType.SQL_SELECT);
                    while (rs.next()) {
                        final long id = rs.getLong(1);
                        final String name = rs.getString(2).trim();
                        String uuid = rs.getString(3);
                        uuid = (uuid == null) ? null : uuid.trim();

                        if (AttributeType.LOG.isDebugEnabled()) {
                            AttributeType.LOG.debug("read attribute type '" + name + "' " + "(id = " + id + ", uuid = '"
                                            + uuid + "')");
                        }

                        final AttributeType attrType = new AttributeType(id, uuid, name);
                        attrType.setClassRepr(rs.getString(4).trim());
                        attrType.setUI(rs.getString(5).trim());
                        if (rs.getInt(6) != 0) {
                            attrType.alwaysUpdate = true;
                        }
                        if (rs.getInt(7) != 0) {
                            attrType.createUpdate = true;
                        }
                        _cache4Id.put(attrType.getId(), attrType);
                        _cache4Name.put(attrType.getName(), attrType);
                        _cache4UUID.put(attrType.getUUID(), attrType);
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final ClassNotFoundException e) {
                throw new CacheReloadException("could not read attribute types", e);
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read attribute types", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read attribute types", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read attribute types", e);
                    }
                }
            }
        }
    }
}

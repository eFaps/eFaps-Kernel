/*
 * Copyright 2003 - 2011 The eFaps Team
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Status implements CacheObjectInterface
{

    /**
     * Select statement that will be executed against the database
     * on reading the cache.
     *
     * @see StatusGroupCache#readCache(Map, Map, Map)
     */
    private static final SQLSelect SELECT = new SQLSelect()
                                            .column("ID")
                                            .column("TYPEID")
                                            .column("KEY")
                                            .column("DESCR")
                                            .from("T_DMSTATUS");

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Status.class);

    /**
     * Stores all instances of GroupCache.
     *
     */
    private static StatusGroupCache GROUPCACHE = new StatusGroupCache();

    /**
     * Stores all instances of type.
     *
     *
     */
    private static StatusCache CACHE = new StatusCache();

    /**
     * Id of this Status.
     */
    private final long id;

    /**
     * Key of this status.
     */
    private final String key;

    /**
     * Description for this status.
     */
    private final String desc;

    /**
     * StatusGroup this Status belongs to.
     */
    private final StatusGroup statusGroup;

    /**
     * @param _statusGroup  StatusGroup this Status belongs to
     * @param _id           Id of this Status
     * @param _key          Key of this status.
     * @param _desc         Description for this status
     */
    private Status(final StatusGroup _statusGroup, final long _id, final String _key, final String _desc)
    {
        this.statusGroup = _statusGroup;
        this.id = _id;
        this.key = _key;
        this.desc = _desc;
    }

    /**
     * Getter method for instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUUID()
    {
        throw new Error();
    }

    /**
     * Getter method for instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * Getter method for instance variable {@link #desc}.
     *
     * @return value of instance variable {@link #desc}
     */
    public String getDescription()
    {
        return this.desc;
    }

    /**
     * Method to get the key to the label.
     * @return key to the label
     */
    public String getLabelKey()
    {
        final StringBuilder keyStr = new StringBuilder();
        return keyStr.append(this.statusGroup.getName()).append("/Key.Status.").append(this.key).toString();
    }

    /**
     * Method to get the translated label for this Status.
     * @return translated Label
     */
    public String getLabel()
    {
        return DBProperties.getProperty(getLabelKey());
    }

    /**
     * Getter method for instance variable {@link #statusGroup}.
     *
     * @return value of instance variable {@link #statusGroup}
     */
    public StatusGroup getStatusGroup()
    {
        return this.statusGroup;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @param _class class that called the method
     * @throws CacheReloadException on error
     */
    public static void initialize(final Class<?> _class) throws CacheReloadException
    {
        Status.GROUPCACHE.initialize(_class);
        Status.CACHE.initialize(_class);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize() throws CacheReloadException
    {
        Status.initialize(Status.class);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _typeName name of the StatusGroup
     * @param _key      key of the Status
     * @return Status
     */
    public static Status find(final String _typeName, final String _key)
    {
        return Status.GROUPCACHE.get(_typeName).get(_key);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _uuid     uuid of the StatusGroup
     * @param _key      key of the Status
     * @return Status
     */
    public static Status find(final UUID _uuid, final String _key)
    {
        return Status.GROUPCACHE.get(Type.get(_uuid).getName()).get(_key);
    }

    /**
     * Method to get a Status from the cache.
     * @param _id id of the status wanted.
     * @return Status
     */
    public static Status get(final long _id)
    {
        return Status.CACHE.get(_id);
    }

    /**
     * Method to get a StatusGroup from the cache.
     * @param _typeName name  of the StatusGroup wanted.
     * @return StatusGroup
     */
    public static StatusGroup get(final String _typeName)
    {
        return Status.GROUPCACHE.get(_typeName);
    }


    /**
     * Class for a group of stati.
     */
    public static class StatusGroup extends HashMap<String, Status> implements CacheObjectInterface
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Type this StatusGroup represents.
         */
        private final Type type;

        /**
         * @param _type type to set
         */
        public StatusGroup(final Type _type)
        {
            this.type = _type;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getId()
        {
            return this.type.getId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName()
        {
            return this.type.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UUID getUUID()
        {
            return this.type.getUUID();
        }
    }

    /**
     * Cache for Status.
     */
    private static class StatusGroupCache extends AbstractCache<Status.StatusGroup>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, Status.StatusGroup> _cache4Id,
                                 final Map<String, Status.StatusGroup> _cache4Name,
                                 final Map<UUID, Status.StatusGroup> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {

                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(Status.SELECT.getSQL());
                    while (rs.next()) {
                        final long id = rs.getLong(1);
                        final long typeid = rs.getLong(2);
                        final String key = rs.getString(3).trim();
                        final String desc = rs.getString(4).trim();

                        if (Status.LOG.isDebugEnabled()) {
                            Status.LOG.debug("read status '" + typeid + "' (id = " + id + ") + key = " + key);
                        }
                        final Type type = Type.get(typeid);
                        StatusGroup statusGroup = _cache4Name.get(type.getName());
                        if (statusGroup == null) {
                            statusGroup = new StatusGroup(type);
                            _cache4Id.put(type.getId(), statusGroup);
                            _cache4Name.put(type.getName(), statusGroup);
                            _cache4UUID.put(type.getUUID(), statusGroup);
                        }
                        final Status status = new Status(statusGroup, id, key, desc);
                        statusGroup.put(status.getKey(), status);
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                // initialize parents

                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read types", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read types", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read types", e);
                    }
                }
            }
        }
    }


    /**
     * Cache for Stati.
     */
    private static class StatusCache extends AbstractCache<Status>
    {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, Status> _newCache4Id,
                                 final Map<String, Status> _newCache4Name,
                                 final Map<UUID, Status> _newCache4UUID)
            throws CacheReloadException
        {
            for (final StatusGroup statusGroup : Status.GROUPCACHE.getCache4Id().values()) {

                for (final Status status : statusGroup.values()) {
                    _newCache4Id.put(status.getId(), status);
                }
            }
        }
    }
}

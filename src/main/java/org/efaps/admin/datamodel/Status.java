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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ci.CIStatus;
import org.efaps.ci.CIType;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class Status
    implements CacheObjectInterface, Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID4STATUS = new SQLSelect()
                    .column("ID")
                    .column("TYPEID")
                    .column("KEY")
                    .column("DESCR")
                    .from("T_DMSTATUS", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME4GRP = new SQLSelect()
                    .column(0, "ID")
                    .column(0, "TYPEID")
                    .column("KEY")
                    .column("DESCR")
                    .from("T_DMSTATUS", 0)
                    .innerJoin("T_CMABSTRACT", 1, "ID", 0, "TYPEID")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID4GRP = new SQLSelect()
                    .column(0, "ID")
                    .column(0, "TYPEID")
                    .column("KEY")
                    .column("DESCR")
                    .from("T_DMSTATUS", 0)
                    .innerJoin("T_CMABSTRACT", 1, "ID", 0, "TYPEID")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE4GRP = Status.class.getName() + ".Group4UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE4STATUS = Status.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE4GRP = Status.class.getName() + ".Group4Name";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Status.class);

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
     * @param _statusGroup StatusGroup this Status belongs to
     * @param _id Id of this Status
     * @param _key Key of this status.
     * @param _desc Description for this status
     */
    private Status(final StatusGroup _statusGroup,
                   final long _id,
                   final String _key,
                   final String _desc)
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
     *
     * @return key to the label
     */
    public String getLabelKey()
    {
        final StringBuilder keyStr = new StringBuilder();
        return keyStr.append(this.statusGroup.getName()).append("/Key.Status.").append(this.key).toString();
    }

    /**
     * Method to get the translated label for this Status.
     *
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
    public static void initialize(final Class<?> _class)
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(Status.UUIDCACHE4GRP)) {
            InfinispanCache.get().<UUID, Status>getCache(Status.UUIDCACHE4GRP).clear();
        } else {
            InfinispanCache.get().<UUID, Status>getCache(Status.UUIDCACHE4GRP)
                            .addListener(new CacheLogListener(Status.LOG));
        }
        if (InfinispanCache.get().exists(Status.IDCACHE4STATUS)) {
            InfinispanCache.get().<Long, Status>getCache(Status.IDCACHE4STATUS).clear();
        } else {
            InfinispanCache.get().<Long, Status>getCache(Status.IDCACHE4STATUS)
                            .addListener(new CacheLogListener(Status.LOG));
        }
        if (InfinispanCache.get().exists(Status.NAMECACHE4GRP)) {
            InfinispanCache.get().<String, StatusGroup>getCache(Status.NAMECACHE4GRP).clear();
        } else {
            InfinispanCache.get().<String, StatusGroup>getCache(Status.NAMECACHE4GRP)
                            .addListener(new CacheLogListener(Status.LOG));
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        Status.initialize(Status.class);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _typeName name of the StatusGroup
     * @param _key key of the Status
     * @return Status
     * @throws CacheReloadException on error
     */
    public static Status find(final String _typeName,
                              final String _key)
        throws CacheReloadException
    {
        return Status.get(_typeName).get(_key);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _uuid uuid of the StatusGroup
     * @param _key key of the Status
     * @return Status
     * @throws CacheReloadException on error
     */
    public static Status find(final UUID _uuid,
                              final String _key)
        throws CacheReloadException
    {
        return Status.get(_uuid).get(_key);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _ciType CIType of the StatusGroup
     * @param _key key of the Status
     * @return Status
     * @throws CacheReloadException on error
     */
    public static Status find(final CIType _ciType,
                              final String _key)
        throws CacheReloadException
    {
        return Status.get(_ciType.uuid).get(_key);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _ciType CIType of the StatusGroup
     * @param _status CIStatus the Status is wanted for
     * @return Status
     * @throws CacheReloadException on error
     */
    public static Status find(final CIType _ciType,
                              final CIStatus _status)
        throws CacheReloadException
    {
        return Status.get(_ciType.uuid).get(_status.key);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _status CIStatus the Status is wanted for
     * @return Status
     * @throws CacheReloadException on error
     */
    public static Status find(final CIStatus _status)
        throws CacheReloadException
    {
        return Status.get(_status.ciType.uuid).get(_status.key);
    }

    /**
     * Method to get a Status from the cache.
     *
     * @param _id id of the status wanted.
     * @return Status
     * @throws CacheReloadException on error
     */
    public static Status get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Status> cache = InfinispanCache.get().<Long, Status>getCache(Status.IDCACHE4STATUS);
        if (!cache.containsKey(_id)) {
            Status.getStatusFromDB(Status.SQL_ID4STATUS, _id);
        }
        return cache.get(_id);
    }

    /**
     * Method to get a StatusGroup from the cache.
     *
     * @param _typeName name of the StatusGroup wanted.
     * @return StatusGroup
     * @throws CacheReloadException on error
     */
    public static StatusGroup get(final String _typeName)
        throws CacheReloadException
    {
        final Cache<String, StatusGroup> cache = InfinispanCache.get().<String, StatusGroup>getCache(
                        Status.NAMECACHE4GRP);
        if (!cache.containsKey(_typeName)) {
            Status.getStatusGroupFromDB(Status.SQL_NAME4GRP, _typeName);
        }
        return cache.get(_typeName);
    }

    /**
     * Method to get a StatusGroup from the cache.
     *
     * @param _uuid UUID of the StatusGroup wanted.
     * @return StatusGroup
     * @throws CacheReloadException on error
     */
    public static StatusGroup get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, StatusGroup> cache = InfinispanCache.get().<UUID, StatusGroup>getCache(Status.UUIDCACHE4GRP);
        if (!cache.containsKey(_uuid)) {
            Status.getStatusGroupFromDB(Status.SQL_UUID4GRP, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _grp StatusGroup to be cached
     */
    private static void cacheStatusGroup(final StatusGroup _grp)
    {
        final Cache<String, StatusGroup> nameCache = InfinispanCache.get().<String, StatusGroup>getIgnReCache(
                        Status.NAMECACHE4GRP);
        nameCache.put(_grp.getName(), _grp);

        final Cache<UUID, StatusGroup> uuidCache = InfinispanCache.get().<UUID, StatusGroup>getIgnReCache(
                        Status.UUIDCACHE4GRP);
        uuidCache.put(_grp.getUUID(), _grp);
    }

    /**
     * @param _status Status to be cached
     */
    private static void cacheStatus(final Status _status)
    {
        final Cache<Long, Status> idCache = InfinispanCache.get().<Long, Status>getIgnReCache(Status.IDCACHE4STATUS);
        idCache.put(_status.getId(), _status);
    }

    /**
     * @param _sql      SQL Statement to be executed
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getStatusGroupFromDB(final String _sql,
                                                final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            final List<Object[]> values = new ArrayList<Object[]>();
            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(new Object[] {
                                    rs.getLong(1),
                                    rs.getLong(2),
                                    rs.getString(3).trim(),
                                    rs.getString(4).trim()
                    });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Object[] row : values) {
                final long id = (Long) row[0];
                final long typeid = (Long) row[1];
                final String key = (String) row[2];
                final String desc = (String) row[3];

                Status.LOG.debug("read status '{}' (id = {}) + key = {}", typeid, id, key);

                final Type type = Type.get(typeid);
                final Cache<UUID, StatusGroup> cache = InfinispanCache.get().<UUID, StatusGroup>getCache(
                                Status.UUIDCACHE4GRP);
                StatusGroup statusGroup;
                if (cache.containsKey(type.getUUID())) {
                    statusGroup = cache.get(type.getUUID());
                } else {
                    statusGroup = new StatusGroup(type);
                }
                final Status status = new Status(statusGroup, id, key, desc);
                statusGroup.put(status.getKey(), status);
                Status.cacheStatus(status);
                Status.cacheStatusGroup(statusGroup);
                ret = true;
            }

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
        return ret;
    }

    /**
     * @param _sql      SQL Statement to be executed
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getStatusFromDB(final String _sql,
                                           final Object _criteria)
        throws CacheReloadException
    {
        final boolean ret = false;
        ConnectionResource con = null;
        try {
            final List<Object[]> values = new ArrayList<Object[]>();
            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(new Object[] {
                                    rs.getLong(1),
                                    rs.getLong(2),
                                    rs.getString(3).trim(),
                                    rs.getString(4).trim()
                    });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Object[] row : values) {
                final long id = (Long) row[0];
                final long typeid = (Long) row[1];
                final String key = (String) row[2];

                Status.LOG.debug("read status '{}' (id = {}) + key = {}", typeid, id, key);

                final Type type = Type.get(typeid);
                Status.get(type.getUUID());
            }
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
        return ret;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Status) {
            ret = ((Status) _obj).getId() == getId();
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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Class for a group of stati.
     */
    public static class StatusGroup
        extends HashMap<String, Status>
        implements CacheObjectInterface, Serializable
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Id of the Type this StatusGroup represents.
         */
        private final UUID uuid;

        /**
         * UUID of the Type this StatusGroup represents.
         */
        private final long id;

        /**
         * Name of the Type this StatusGroup represents.
         */
        private final String name;

        /**
         * @param _type type to set
         */
        public StatusGroup(final Type _type)
        {
            this.uuid = _type.getUUID();
            this.id = _type.getId();
            this.name = _type.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
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
            return this.name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UUID getUUID()
        {
            return this.uuid;
        }
    }
}

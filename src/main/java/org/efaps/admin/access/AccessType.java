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

package org.efaps.admin.access;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
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
 * The class handles the caching for access types like &quot;checkin&quot; or
 * &quot;read&quot;.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class AccessType
    extends AbstractAdminObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccessType.class);

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .from("T_ACCESSTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .from("T_ACCESSTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .from("T_ACCESSTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = "AccessType4UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = "AccessType4ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = "AccessType4Name";

    /**
     * This is the constructor.
     *
     * @param _id id of this access type
     * @param _uuid universal unique identifier of this access type
     * @param _name name of this access type
     */
    private AccessType(final long _id,
                       final String _uuid,
                       final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * The method checks, if the given object represents the same access type as
     * this instance. Equals means, that the object to compare is not null, an
     * instance of this class {@link AccessType} and both id's are the same.
     *
     * @param _toCompare object used to compare
     * @return <i>true</i> if equals, otherwise <i>false</i>
     */
    @Override
    public boolean equals(final Object _toCompare)
    {
        return (_toCompare != null)
                        && (_toCompare instanceof AccessType)
                        && (((AccessType) _toCompare).getId() == getId());
    }

    /**
     * @see java.lang.Object#hashCode()
     * @return int hashCode
     */
    @Override
    public int hashCode()
    {
        return (new Long(getId())).hashCode();
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(AccessType.UUIDCACHE)) {
            InfinispanCache.get().<UUID, AccessType>getCache(AccessType.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, AccessType>getCache(AccessType.UUIDCACHE)
                            .addListener(new CacheLogListener(AccessType.LOG));
        }
        if (InfinispanCache.get().exists(AccessType.IDCACHE)) {
            InfinispanCache.get().<Long, AccessType>getCache(AccessType.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, AccessType>getCache(AccessType.IDCACHE)
                            .addListener(new CacheLogListener(AccessType.LOG));
        }
        if (InfinispanCache.get().exists(AccessType.NAMECACHE)) {
            InfinispanCache.get().<String, AccessType>getCache(AccessType.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, AccessType>getCache(AccessType.NAMECACHE)
                            .addListener(new CacheLogListener(AccessType.LOG));
        }
    }

    /**
     * Returns for given identifier in <i>_id</i> the cached instance of class
     * AccessType.
     *
     * @param _id id of the search access type
     * @return instance of class AccessType
     * @throws CacheReloadException on error
     */
    public static AccessType getAccessType(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, AccessType> cache = InfinispanCache.get().<Long, AccessType>getCache(AccessType.IDCACHE);
        if (!cache.containsKey(_id)) {
            AccessType.getAccessTypeFromDB(AccessType.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given name in <i>_name</i> the cached instance of class
     * AccessType.
     *
     * @param _name name of the access type
     * @return instance of class AccessType
     * @throws CacheReloadException on error
     */
    public static AccessType getAccessType(final String _name)
        throws CacheReloadException
    {
        final Cache<String, AccessType> cache = InfinispanCache.get()
                        .<String, AccessType>getCache(AccessType.NAMECACHE);
        if (!cache.containsKey(_name)) {
            AccessType.getAccessTypeFromDB(AccessType.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given universal unique identifier in <i>_uuid</i> the cached
     * instance of class AccessType.
     *
     * @param _uuid UUID of the access type
     * @return instance of class AccessType
     * @throws CacheReloadException on error
     */
    public static AccessType getAccessType(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, AccessType> cache = InfinispanCache.get().<UUID, AccessType>getCache(AccessType.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            AccessType.getAccessTypeFromDB(AccessType.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _accessType AccessType to be cached
     */
    private static void cacheAccessType(final AccessType _accessType)
    {
        final Cache<UUID, AccessType> cache4UUID = InfinispanCache.get().<UUID, AccessType>getCache(
                        AccessType.UUIDCACHE);
        if (!cache4UUID.containsKey(_accessType.getUUID())) {
            cache4UUID.put(_accessType.getUUID(), _accessType);
        }

        final Cache<String, AccessType> nameCache = InfinispanCache.get().<String, AccessType>getCache(
                        AccessType.NAMECACHE);
        if (!nameCache.containsKey(_accessType.getName())) {
            nameCache.put(_accessType.getName(), _accessType);
        }
        final Cache<Long, AccessType> idCache = InfinispanCache.get().<Long, AccessType>getCache(AccessType.IDCACHE);
        if (!idCache.containsKey(_accessType.getId())) {
            idCache.put(_accessType.getId(), _accessType);
        }
    }


    /**
     * @param _sql      sql Statement to be executed
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getAccessTypeFromDB(final String _sql,
                                               final Object _criteria)
        throws CacheReloadException
    {
        final boolean ret = false;
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
                    final String uuid = rs.getString(2);
                    final String name = rs.getString(3);
                    AccessType.LOG.debug("read AccessType  '{}' (id = {}))", name, id);
                    final AccessType accessType = new AccessType(id, uuid, name);
                    AccessType.cacheAccessType(accessType);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read access types", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read access types", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not abort transaction "
                                    + "while reading access types", e);
                }
            }
        }
        return ret;
    }
}

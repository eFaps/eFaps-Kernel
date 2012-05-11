/*
 * Copyright 2003 - 2012 The eFaps Team
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheReloadException;
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
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccessType.class);

    /**
     * This is the sql select statement to select all access types from the
     * database.
     */
    private static final SQLSelect SQL_SELECT = new SQLSelect()
                                                .column("ID")
                                                .column("UUID")
                                                .column("NAME")
                                                .from("T_ACCESSTYPE");

    /**
     * Stores all instances of class {@link AccessType}.
     *
     * @see #getAccessType(long)
     * @see #getAccessType(String)
     * @see #getAccessType(UUID)
     */
    private static final AccessTypeCache CACHE = new AccessTypeCache();

    /**
     * This is the constructor.
     *
     * @param _id     id of this access type
     * @param _uuid   universal unique identifier of this access type
     * @param _name   name of this access type
     */
    private AccessType(final long _id,
                       final String _uuid,
                       final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * The method checks, if the given object represents the same access type
     * as this instance. Equals means, that the object to compare is not null,
     * an instance of this class {@link AccessType} and both id's are the same.
     *
     * @param _toCompare  object used to compare
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
        AccessType.CACHE.initialize(AccessType.class);
    }

    /**
     * Returns for given identifier in  <i>_id</i> the cached instance of class
     * AccessType.
     *
     * @param _id   id of the search access type
     * @return instance of class AccessType
     */
    public static AccessType getAccessType(final long _id)
    {
        return AccessType.CACHE.get(_id);
    }

    /**
     * Returns for given name in <i>_name</i> the cached instance of class
     * AccessType.
     *
     * @param _name     name of the access type
     * @return instance of class AccessType
     */
    public static AccessType getAccessType(final String _name)
    {
        return AccessType.CACHE.get(_name);
    }

    /**
     * Returns for given universal unique identifier in <i>_uuid</i> the cached
     * instance of class AccessType.
     *
     * @param _uuid     UUID of the access type
     * @return instance of class AccessType
     */
    public static AccessType getAccessType(final UUID _uuid)
    {
        return AccessType.CACHE.get(_uuid);
    }

    /**
     * Cache for all access types.
     */
    private static class AccessTypeCache
        extends AbstractCache<AccessType>
    {
        /**
         * Reads all access types and stores them in the given mapping caches.
         *
         * @param _newCache4Id      cache for the mapping between id and access
         *                          type
         * @param _newCache4Name    cache for the mapping between name and
         *                          access type
         * @param _newCache4UUID    cache for the mapping between UUID and
         *                          access type
         * @throws CacheReloadException if cache could not be reloaded
         */
        @Override
        protected void readCache(final Map<Long, AccessType> _newCache4Id,
                                 final Map<String, AccessType> _newCache4Name,
                                 final Map<UUID, AccessType> _newCache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try  {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try  {

                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(AccessType.SQL_SELECT.getSQL());
                    while (rs.next())  {
                        final long id                       = rs.getLong(1);
                        final String uuid                   = rs.getString(2);
                        final String name                   = rs.getString(3);

                        if (AccessType.LOG.isDebugEnabled())  {
                            AccessType.LOG.debug("read access type '" + name + "' "
                                    + "(id = " + id + ", uuid = " + uuid + ")");
                        }

                        final AccessType accessType = new AccessType(id, uuid, name);
                        _newCache4Id.put(accessType.getId(), accessType);
                        _newCache4Name.put(accessType.getName(), accessType);
                        _newCache4UUID.put(accessType.getUUID(), accessType);
                    }
                    rs.close();

                } finally  {
                    if (stmt != null)  {
                        stmt.close();
                    }
                }

                con.commit();

            } catch (final SQLException e)  {
                throw new CacheReloadException("could not read access types", e);
            } catch (final EFapsException e)  {
                throw new CacheReloadException("could not read access types", e);
            } finally  {
                if ((con != null) && con.isOpened())  {
                    try  {
                        con.abort();
                    } catch (final EFapsException e)  {
                        throw new CacheReloadException("could not abort transaction "
                                               + "while reading access types", e);
                    }
                }
            }
        }
    }
}

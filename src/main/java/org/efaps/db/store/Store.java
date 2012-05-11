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

package org.efaps.db.store;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIDB;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Store
    extends AbstractAdminObject
{
    /**
     * Property to get the compress for this store.
     */
    public static final String PROPERTY_COMPRESS = "StoreCompress";

    /**
     * Property name to get the JDNI Name.
     */
    public static final String PROPERTY_JNDINAME = "StoreJNDIName";

    /**
     * Cache for Stores.
     */
    private static final StoreCache CACHE = new StoreCache();

    /**
     * Name of the Resource class of this store.
     */
    private String resource;

    /**
     * Properties for the StoreResource.
     */
    private final Map<String, String> resourceProperties = new HashMap<String, String>();

    /**
     * @param _id id of this store
     * @param _uuid uuid of this store
     * @param _name name of thi store
     */
    private Store(final long _id,
                  final String _uuid,
                  final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Sets the link properties for this object.
     *
     * @param _linkType type of the link property
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException error
     */
    @Override
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIDB.Store2Resource.getType())) {
            this.resource = _toName;
            loadResourceProperties(_toId);
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * Method to get the properties for the resource.
     *
     * @param _id id of the resource
     * @throws EFapsException on error
     */
    private void loadResourceProperties(final long _id)
        throws EFapsException
    {
        this.resourceProperties.clear();
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.Property);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.Property.Abstract, _id);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminCommon.Property.Name, CIAdminCommon.Property.Value);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            this.resourceProperties.put(multi.<String> getAttribute(CIAdminCommon.Property.Name),
                            multi.<String> getAttribute(CIAdminCommon.Property.Value));
        }
    }

    /**
     * @return value for instance variable {@link #resourceProperties}
     */
    protected Map<String, String> getResourceProperties()
    {
        return this.resourceProperties;
    }

    /**
     * Method to get a instance of the resource.
     *
     * @param _instance instance the resource is wanted for
     * @return Resource
     * @throws EFapsException on error
     */
    public Resource getResource(final Instance _instance)
        throws EFapsException
    {
        Resource ret = null;
        try {
            ret = (Resource) (Class.forName(this.resource).newInstance());
            ret.initialize(_instance, this);
        } catch (final InstantiationException e) {
            throw new EFapsException(Store.class, "getResource.InstantiationException", e, this.resource);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(Store.class, "getResource.IllegalAccessException", e, this.resource);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(Store.class, "getResource.ClassNotFoundException", e, this.resource);
        }
        return ret;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error during loading of cache
     */
    public static void initialize()
        throws CacheReloadException
    {
        // check is need to work on first installation
        if (CIDB.Store.getType() != null) {
            Store.CACHE.initialize(Store.class);
            for (final Store store : Store.CACHE.getCache4Id().values()) {
                store.readFromDB4Properties();
                store.readFromDB4Links();
            }
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Store}.
     *
     * @param _id id of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException
     */
    public static Store get(final long _id)
    {
        return Store.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Store}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException
     */
    public static Store get(final String _name)
    {
        return Store.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Store}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException
     */
    public static Store get(final UUID _uuid)
    {
        return Store.CACHE.get(_uuid);
    }

    /**
     * Cache for Stores.
     */
    public static class StoreCache
        extends AbstractCache<Store>
    {

        /**
         * Method to fill this cache with objects.
         *
         * @param _cache4Id Cache for id
         * @param _cache4Name Cache for name
         * @param _cache4UUID Cache for UUID
         * @throws CacheReloadException on error during reading
         */
        @Override
        protected void readCache(final Map<Long, Store> _cache4Id,
                                 final Map<String, Store> _cache4Name,
                                 final Map<UUID, Store> _cache4UUID)
            throws CacheReloadException
        {
            try {
                final QueryBuilder queryBldr = new QueryBuilder(CIDB.Store);
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute(CIDB.Store.Name, CIDB.Store.ID, CIDB.Store.UUID);
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final long id = multi.<Long> getAttribute(CIDB.Store.ID);
                    final String name = multi.<String> getAttribute(CIDB.Store.Name);
                    final String uuid = multi.<String> getAttribute(CIDB.Store.UUID);
                    final Store store = new Store(id, uuid, name);
                    _cache4Id.put(store.getId(), store);
                    _cache4Name.put(store.getName(), store);
                    _cache4UUID.put(store.getUUID(), store);
                }
            } catch (final EFapsException e) {
                throw new CacheReloadException("Could not read Store.", e);
            }
        }
    }
}

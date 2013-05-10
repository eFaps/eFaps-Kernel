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

package org.efaps.db.store;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIDB;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Store.class);

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = "Store4UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = "Store4ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = "Store4Name";

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
            this.resourceProperties.put(multi.<String>getAttribute(CIAdminCommon.Property.Name),
                            multi.<String>getAttribute(CIAdminCommon.Property.Value));
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
            Store.LOG.debug("Getting resource for: {} with properties: {}", this.resource, this.resourceProperties);
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
        if (InfinispanCache.get().exists(Store.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Store>getCache(Store.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Store>getCache(Store.UUIDCACHE).addListener(new CacheLogListener(Store.LOG));
        }
        if (InfinispanCache.get().exists(Store.IDCACHE)) {
            InfinispanCache.get().<Long, Store>getCache(Store.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Store>getCache(Store.IDCACHE).addListener(new CacheLogListener(Store.LOG));
        }
        if (InfinispanCache.get().exists(Store.NAMECACHE)) {
            InfinispanCache.get().<String, Store>getCache(Store.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Store>getCache(Store.NAMECACHE).addListener(new CacheLogListener(Store.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Store}.
     *
     * @param _id id of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException on error
     */
    public static Store get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Store> cache = InfinispanCache.get().<Long, Store>getCache(Store.IDCACHE);
        if (!cache.containsKey(_id)) {
            Store.getStoreFromDB(CIDB.Store.ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Store}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException  on error
     */
    public static Store get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Store> cache = InfinispanCache.get().<String, Store>getCache(Store.NAMECACHE);
        if (!cache.containsKey(_name)) {
            Store.getStoreFromDB(CIDB.Store.Name, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Store}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException  on error
     */
    public static Store get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, Store> cache = InfinispanCache.get().<UUID, Store>getCache(Store.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Store.getStoreFromDB(CIDB.Store.UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _store Store to be cached
     */
    private static void cacheStore(final Store _store)
    {
        final Cache<UUID, Store> cache4UUID = InfinispanCache.get().<UUID, Store>getCache(Store.UUIDCACHE);
        if (!cache4UUID.containsKey(_store.getUUID())) {
            cache4UUID.put(_store.getUUID(), _store);
        }
        final Cache<String, Store> nameCache = InfinispanCache.get().<String, Store>getCache(Store.NAMECACHE);
        if (!nameCache.containsKey(_store.getName())) {
            nameCache.put(_store.getName(), _store);
        }
        final Cache<Long, Store> idCache = InfinispanCache.get().<Long, Store>getCache(Store.IDCACHE);
        if (!idCache.containsKey(_store.getId())) {
            idCache.put(_store.getId(), _store);
        }
    }

    /**
     * @param _uUID      CIAttribute
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getStoreFromDB(final CIAttribute _uUID,
                                          final Object _criteria)
        throws CacheReloadException
    {
        final boolean ret = false;
        try {
            final QueryBuilder queryBldr = new QueryBuilder(CIDB.Store);
            queryBldr.addWhereAttrEqValue(CIDB.Store.ID, _criteria);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIDB.Store.Name, CIDB.Store.ID, CIDB.Store.UUID);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final long id = multi.<Long>getAttribute(CIDB.Store.ID);
                final String name = multi.<String>getAttribute(CIDB.Store.Name);
                final String uuid = multi.<String>getAttribute(CIDB.Store.UUID);
                final Store store = new Store(id, uuid, name);
                store.readFromDB4Properties();
                store.readFromDB4Links();
                Store.cacheStore(store);
            }
        } catch (final EFapsException e) {
            throw new CacheReloadException("Could not read Store.", e);
        }
        return ret;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Store) {
            ret = ((Store) _obj).getId() == getId();
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
}

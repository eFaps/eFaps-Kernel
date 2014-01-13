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

package org.efaps.db;

import java.util.HashSet;
import java.util.Set;

import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching for Queries.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class QueryCache
{

    /**
     * DefaultKey.
     */
    public static final String DEFAULTKEY = QueryCache.class.getName() + ".DefaultKey";

    /**
     * Name of the Cache for Instances.
     */
    public static final String SQLCACHE = QueryCache.class.getName() + ".Sql";

    /**
     * Name of the Cache for AccessKey.
     */
    public static final String KEYCACHE = QueryCache.class.getName() + ".Key";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(QueryCache.class);

    /**
     * Utility class therefore no public Constructor.
     */
    private QueryCache()
    {
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(QueryCache.KEYCACHE)) {
            InfinispanCache.get().<String, Set<QueryKey>>getCache(QueryCache.KEYCACHE).clear();
        } else {
            final Cache<String, Set<QueryKey>> cache = InfinispanCache.get().<String, Set<QueryKey>>getCache(
                            QueryCache.KEYCACHE);
            cache.addListener(new CacheLogListener(QueryCache.LOG));
            cache.addListener(new KeyCacheListener());
        }
        if (InfinispanCache.get().exists(QueryCache.SQLCACHE)) {
            InfinispanCache.get().<QueryKey, Object>getCache(QueryCache.SQLCACHE).clear();
        } else {
            final Cache<QueryKey, Object> cache = InfinispanCache.get().<QueryKey, Object>getCache(
                            QueryCache.SQLCACHE);
            cache.addListener(new CacheLogListener(QueryCache.LOG));
            cache.addListener(new SqlCacheListener());
        }
    }

    /**
     * @param _key key to be cleaned
     */
    public static void cleanByKey(final String _key)
    {
        final Cache<String, Set<QueryKey>> cache = InfinispanCache.get().<String, Set<QueryKey>>getIgnReCache(
                        QueryCache.KEYCACHE);
        cache.remove(_key);
    }

    /**
     * @param _cacheDef cacheDefinition
     * @param _querykey QueryKey
     * @param _object   object to store
     */
    public static void put(final ICacheDefinition _cacheDef,
                           final QueryKey _querykey,
                           final Object _object)
    {
        final Cache<QueryKey, Object> cache = QueryCache.getSqlCache();
        if (_cacheDef.getMaxIdleTime() != 0) {
            cache.put(_querykey, _object, _cacheDef.getLifespan(), _cacheDef.getLifespanUnit(),
                            _cacheDef.getMaxIdleTime(), _cacheDef.getMaxIdleTimeUnit());
        } else if (_cacheDef.getLifespan() != 0) {
            cache.put(_querykey, _object, _cacheDef.getLifespan(), _cacheDef.getLifespanUnit());
        } else {
            cache.put(_querykey, _object);
        }
    }

    /**
     * @return the QueryCache
     */
    public static Cache<QueryKey, Object> getSqlCache()
    {
        return InfinispanCache.get().<QueryKey, Object>getCache(QueryCache.SQLCACHE).getAdvancedCache()
                        .withFlags(Flag.IGNORE_RETURN_VALUES);
    }

    /**
     * Listener responsible to maintain the two Caches in sync. The SQLCache
     * will receive the new QueryKey in the moment that a new QueryKey is
     * inserted.
     */
    @Listener
    public static class SqlCacheListener
    {

        /**
         * If an QueryKey is inserted to SQLCache the QueryKey will also be
         * registered in the KeyCache.
         *
         * @param _event event to be loged
         */
        @CacheEntryCreated
        public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> _event)
        {
            if (!_event.isPre()) {
                final Cache<String, Set<QueryKey>> keyCache = InfinispanCache.get()
                                .<String, Set<QueryKey>>getIgnReCache(QueryCache.KEYCACHE);

                final QueryKey querykey = (QueryKey) _event.getKey();
                final String key = querykey.getKey();

                Set<QueryKey> queryKeys;
                if (keyCache.containsKey(key)) {
                    queryKeys = keyCache.get(key);
                } else {
                    queryKeys = new HashSet<QueryKey>();
                    keyCache.put(key, queryKeys);
                }
                queryKeys.add(querykey);
            }

        }
    }

    /**
     * Listener responsible to maintain the two Caches in sync. On removal of an
     * Key from the KeyCache the related QueryKey from the SQLCache will be
     * removed also.
     */
    @Listener
    public static class KeyCacheListener
    {

        /**
         * If an Key is removed the related QueryKey will be removed also.
         *
         * @param _event event
         */
        @SuppressWarnings("unchecked")
        @CacheEntryRemoved
        public void onCacheEntryRemoved(final CacheEntryRemovedEvent<?, ?> _event)
        {
            if (_event.isPre()) {
                final Cache<QueryKey, Object> keyCache = InfinispanCache.get().<QueryKey, Object>getIgnReCache(
                                QueryCache.SQLCACHE);
                final Set<QueryKey> queryKeys = (Set<QueryKey>) _event.getValue();
                if (queryKeys != null) {
                    for (final QueryKey queryKey : queryKeys) {
                        keyCache.remove(queryKey);
                    }
                }
            }
        }
    }
}

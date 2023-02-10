/*
 * Copyright 2003 - 2021 The eFaps Team
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

package org.efaps.db;

import java.util.List;

import org.efaps.admin.AppConfigHandler;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.efaps.util.cache.NoOpCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.query.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching for Queries.
 *
 * @author The eFaps Team
 *
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
    public static final String INDEXCACHE = QueryCache.class.getName() + ".Index";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(QueryCache.class);

    /**
     * NoOp cache in case the QueryCache mechanism is deactivated.
     */
    private static NoOpQueryCache NOOP;

    private static boolean INIT;

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
        if (AppConfigHandler.get().isQueryCacheDeactivated()) {
            QueryCache.NOOP = new NoOpQueryCache();
        } else {
            final Cache<String, QueryKey> indexCache = InfinispanCache.get().<String, QueryKey>getCache(
                            QueryCache.INDEXCACHE);
            indexCache.clear();
            final Cache<QueryKey, Object> sqlCache = InfinispanCache.get()
                            .<QueryKey, Object>getCache(QueryCache.SQLCACHE);
            sqlCache.clear();
            if (!INIT) {
                INIT = true;
                indexCache.addListener(new CacheLogListener(QueryCache.LOG));
                sqlCache.addListener(new CacheLogListener(QueryCache.LOG));
                sqlCache.addListener(new SqlCacheListener());
            }
        }
    }

    /**
     * @param _key key to be cleaned
     */
    public static void cleanByKey(final String _key)
    {
        if (!AppConfigHandler.get().isQueryCacheDeactivated()) {
            final Cache<String, QueryKey> indexCache = InfinispanCache.get().<String, QueryKey>getIgnReCache(
                            QueryCache.INDEXCACHE);
            if (!indexCache.isEmpty()) {
                final var queryFactory = Search.getQueryFactory(indexCache);
                final var query = queryFactory.create("FROM org.efaps.db.QueryKey q WHERE q.key = '" + _key + "'");
                final List<?> result = query.execute().list();
                if (result != null) {
                    for (final Object key : result) {
                        QueryCache.getSqlCache().remove(key);
                        indexCache.remove(((QueryKey) key).getIndexKey());
                    }
                }
            }
        }
    }

    /**
     * @param _cacheDef cacheDefinition
     * @param _querykey QueryKey
     * @param _object object to store
     */
    public static void put(final ICacheDefinition _cacheDef,
                           final QueryKey _querykey,
                           final Object _object)
    {
        if (!AppConfigHandler.get().isQueryCacheDeactivated()) {
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
    }

    /**
     * @return the QueryCache
     */
    public static Cache<QueryKey, Object> getSqlCache()
    {
        final Cache<QueryKey, Object> ret;
        if (AppConfigHandler.get().isQueryCacheDeactivated()) {
            ret = QueryCache.NOOP;
        } else {
            ret = InfinispanCache.get().<QueryKey, Object>getCache(QueryCache.SQLCACHE).getAdvancedCache()
                            .withFlags(Flag.IGNORE_RETURN_VALUES);
        }
        return ret;
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
         * registered in the IndexCache.
         *
         * @param _event event to be logged
         */
        @CacheEntryCreated
        public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> _event)
        {
            if (!_event.isPre()) {
                final Cache<String, QueryKey> indexCache = InfinispanCache.get()
                                .<String, QueryKey>getIgnReCache(QueryCache.INDEXCACHE);

                final QueryKey querykey = (QueryKey) _event.getKey();
                indexCache.put(querykey.getIndexKey(), querykey);
            }
        }
    }

    /**
     * Implementation of a Cache that actually does not store anything.
     */
    private static class NoOpQueryCache
        extends NoOpCache<QueryKey, Object>
    {

    }
}

/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db;

import org.efaps.admin.AppConfigHandler;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.efaps.util.cache.NoOpCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
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
    public static final String CACHE = QueryCache.class.getName();

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
            final Cache<QueryKey, Object> sqlCache = InfinispanCache.get()
                            .<QueryKey, Object>getCache(QueryCache.CACHE);
            sqlCache.clear();
            if (!INIT) {
                INIT = true;
                sqlCache.addListener(new CacheLogListener(QueryCache.LOG));
            }
        }
    }

    /**
     * @param _key key to be cleaned
     */
    public static void cleanByKey(final String _key)
    {
        if (!AppConfigHandler.get().isQueryCacheDeactivated()) {
            final var cache = get();
            if (!cache.isEmpty()) {
                final var queryFactory = Search.getQueryFactory(cache);
                final Query<QueryKey> query = queryFactory
                                .create("DELETE FROM org.efaps.db.QueryValue q WHERE q.key = :key");
                query.setParameter("key", _key);
                final var deleted = query.executeStatement();
                LOG.debug("Deleted {} entries for {}", deleted, _key);
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
                           final QueryValue _object)
    {
        if (!AppConfigHandler.get().isQueryCacheDeactivated()) {
            final Cache<QueryKey, QueryValue> cache = QueryCache.get();
            if (_cacheDef.getMaxIdleTime() != 0) {
                cache.put(_querykey, _object, _cacheDef.getLifespan(), _cacheDef.getLifespanUnit(),
                                _cacheDef.getMaxIdleTime(), _cacheDef.getMaxIdleTimeUnit());
            } else if (_cacheDef.getLifespan() != 0) {
                cache.put(_querykey, _object, _cacheDef.getLifespan(), _cacheDef.getLifespanUnit());
            } else {
                cache.put(_querykey, _object);
            }
            LOG.debug("Added entry for {}", _querykey);
        }
    }

    /**
     * @return the QueryCache
     */
    public static Cache<QueryKey, QueryValue> get()
    {
        final Cache<QueryKey, QueryValue> ret;
        if (AppConfigHandler.get().isQueryCacheDeactivated()) {
            ret = QueryCache.NOOP;
        } else {
            ret = InfinispanCache.get().<QueryKey, QueryValue>getCache(QueryCache.CACHE).getAdvancedCache()
                            .withFlags(Flag.IGNORE_RETURN_VALUES);
        }
        return ret;
    }

    /**
     * Implementation of a Cache that actually does not store anything.
     */
    private static class NoOpQueryCache
        extends NoOpCache<QueryKey, QueryValue>
    {

    }
}

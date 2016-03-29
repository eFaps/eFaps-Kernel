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

package org.efaps.admin.access;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TermQuery;
import org.efaps.admin.AppConfigHandler;
import org.efaps.db.Instance;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.infinispan.commands.read.GetKeyValueCommand;
import org.infinispan.commands.write.PutKeyValueCommand;
import org.infinispan.context.InvocationContext;
import org.infinispan.interceptors.base.BaseCustomInterceptor;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.ResultIterator;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class AccessCache
{

    /**
     * Name of the Cache for Instances.
     */
    public static final String INDEXCACHE = AccessCache.class.getName() + ".Index";

    /**
     * Name of the Cache for AccessKey.
     */
    public static final String KEYCACHE = AccessCache.class.getName() + ".AccessKey";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccessCache.class);

    /**
     * Utility class therefore no public Constructor.
     */
    private AccessCache()
    {
    }

    /**
     * @param _personId personid the access cache must be clean for
     */
    public static void clean4Person(final long _personId)
    {
        AccessCache.LOG.debug("Cleaning cache for Person: {}", _personId);
        final Cache<String, AccessKey> indexCache = InfinispanCache.get()
                        .<String, AccessKey>getIgnReCache(AccessCache.INDEXCACHE);
        if (!indexCache.isEmpty()) {
            final SearchManager searchManager = Search.getSearchManager(indexCache);

            final CacheQuery query = searchManager.getQuery(
                            NumericRangeQuery.newLongRange("personId", _personId, _personId, true,
                                            true), AccessKey.class);
            try (final ResultIterator iter = query.iterator()) {
                while (iter.hasNext()) {
                    final AccessKey key = (AccessKey) iter.next();
                    AccessCache.getKeyCache().remove(key);
                    indexCache.remove(key.getIndexKey());
                }
            }
        }
    }

    /**
     * @param _instance Instance the update will be registered for
     */
    public static void registerUpdate(final Instance _instance)
    {
        if (AppConfigHandler.get().isAccessCacheDeactivated()) {
            AccessCache.LOG.debug("AccessCache is deactivated.");
        } else {
            AccessCache.LOG.debug("registered Update for Instance: {}", _instance);
            final Cache<String, AccessKey> indexCache = InfinispanCache.get()
                            .<String, AccessKey>getIgnReCache(AccessCache.INDEXCACHE);
            if (!indexCache.isEmpty()) {
                final SearchManager searchManager = Search.getSearchManager(indexCache);
                final Term typeUUIDTerm = new Term("instanceTypeUUID", _instance.getTypeUUID().toString());

                final BooleanQuery andQuery = new BooleanQuery();
                andQuery.add(new TermQuery(typeUUIDTerm), Occur.MUST);
                andQuery.add(NumericRangeQuery.newLongRange("instanceId", _instance.getId(), _instance.getId(), true,
                                true), Occur.MUST);
                final CacheQuery query = searchManager.getQuery(andQuery, AccessKey.class);

                try (final ResultIterator iter = query.iterator()) {
                    while (iter.hasNext()) {
                        final AccessKey key = (AccessKey) iter.next();
                        AccessCache.getKeyCache().remove(key);
                        indexCache.remove(key.getIndexKey());
                    }
                }
            }
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(AccessCache.INDEXCACHE)) {
            InfinispanCache.get().<String, AccessKey>getCache(AccessCache.INDEXCACHE).clear();
        } else {
            final Cache<String, AccessKey> cache = InfinispanCache.get().<String, AccessKey>getCache(
                            AccessCache.INDEXCACHE);
            cache.addListener(new CacheLogListener(AccessCache.LOG));
        }
        if (InfinispanCache.get().exists(AccessCache.KEYCACHE)) {
            InfinispanCache.get().<AccessKey, Boolean>getCache(AccessCache.KEYCACHE).clear();
        } else {
            final Cache<AccessKey, Boolean> cache = InfinispanCache.get().<AccessKey, Boolean>getCache(
                            AccessCache.KEYCACHE);
            cache.addListener(new CacheLogListener(AccessCache.LOG));
            cache.addListener(new KeyCacheListener());
            cache.getAdvancedCache().addInterceptor(new Interceptor(), 0);
        }
    }

    /**
     * @return the Cache for AccessKey
     */
    public static Cache<AccessKey, Boolean> getKeyCache()
    {
        return InfinispanCache.get().<AccessKey, Boolean>getIgnReCache(AccessCache.KEYCACHE);
    }

    /**
     * Listener responsible to maintain the two Caches in sync.
     * The instanceCache will receive the new AccessKey in the moment that a new AccessKey is inserted.
     */
    @Listener
    public static class KeyCacheListener
    {
        /**
         * If an AccessKey is inserted to KeyCache the instance will also be
         * registered in the INDEXCACHE.
         *
         * @param _event event to be logged
         */
        @CacheEntryCreated
        public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> _event)
        {
            if (!_event.isPre()) {
                final Cache<String, AccessKey> indexCache = InfinispanCache.get()
                                .<String, AccessKey>getIgnReCache(AccessCache.INDEXCACHE);
                final AccessKey accessKey = (AccessKey) _event.getKey();
                indexCache.put(accessKey.getIndexKey(), accessKey);
            }
        }
    }

    /**
     * Interceptor.
     */
    private static class Interceptor
        extends BaseCustomInterceptor
    {

        @Override
        public Object visitPutKeyValueCommand(final InvocationContext _ctx,
                                              final PutKeyValueCommand _command)
            throws Throwable
        {
            final Object ret;
            if (AppConfigHandler.get().isAccessCacheDeactivated()) {
                // even if the access cache is deactivated a OneTime Key will be
                // stored for one access and then removed
                final AccessKey key = (AccessKey) _command.getKey();
                if (key.isOneTime()) {
                    ret = invokeNextInterceptor(_ctx, _command);
                } else {
                    ret = null;
                }
            } else {
                ret = invokeNextInterceptor(_ctx, _command);
            }
            return ret;
        }

        @Override
        public Object visitGetKeyValueCommand(final InvocationContext _ctx,
                                              final GetKeyValueCommand _command)
            throws Throwable
        {
            final Object tmp = invokeNextInterceptor(_ctx, _command);
            final Object ret;
            if (tmp == null) {
                ret = null;
            } else {
                final AccessKey key = (AccessKey) _command.getKey();
                if (key.isOneTime()) {
                    AccessCache.getKeyCache().remove(key);
                }
                ret = tmp;
            }
            return ret;
        }
    }
}

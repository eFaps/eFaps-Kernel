/*
AccessCache.LOG.debug("registered Update for Instance: {}", _instance); * Copyright 2003 - 2013 The eFaps Team
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

import java.util.List;
import java.util.Set;

import org.efaps.admin.AppConfigHandler;
import org.efaps.db.Instance;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.efaps.util.cache.NoOpCache;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
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
     * NoOp cache in case the AccessCache mechanism is deactivated.
     */
    private static NoOpAccessCache NOOP;

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
            final QueryFactory<?> qf = searchManager.getQueryFactory();
            final Query query = qf.from(AccessKey.class).having("personId").eq(_personId).toBuilder().build();
            final List<?> result = query.list();
            if (result != null) {
                for (final Object key : result) {
                    AccessCache.getKeyCache().remove(key);
                    indexCache.remove(((AccessKey) key).getIndexKey());
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
                final QueryFactory<?> qf = searchManager.getQueryFactory();
                final Query query = qf.from(AccessKey.class)
                                .having("instanceTypeUUID").like(_instance.getTypeUUID().toString())
                                .and()
                                .having("instanceId").eq(_instance.getId())
                                .toBuilder().build();
                final List<?> result = query.list();
                if (result != null) {
                    for (final Object key : result) {
                        AccessCache.getKeyCache().remove(key);
                        indexCache.remove(((AccessKey) key).getIndexKey());
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
        if (AppConfigHandler.get().isAccessCacheDeactivated()) {
            AccessCache.NOOP = new NoOpAccessCache();
        } else {
            if (InfinispanCache.get().exists(AccessCache.INDEXCACHE)) {
                InfinispanCache.get().<Instance, Set<AccessKey>>getCache(AccessCache.INDEXCACHE).clear();
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
            }
        }
    }

    /**
     * @return the Cache for AccessKey
     */
    public static Cache<AccessKey, Boolean> getKeyCache()
    {
        Cache<AccessKey, Boolean> ret;
        if (AppConfigHandler.get().isAccessCacheDeactivated()) {
            ret = AccessCache.NOOP;
        } else {
            ret = InfinispanCache.get().<AccessKey, Boolean>getIgnReCache(AccessCache.KEYCACHE);
        }
        return ret;
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
     * Implementation of a Cache that actually does not store anything.
     */
    private static class NoOpAccessCache
        extends NoOpCache<AccessKey, Boolean>
    {

    }
}

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

import java.util.HashSet;
import java.util.Set;

import org.efaps.db.Instance;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
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
    public static final String INSTANCECACHE = AccessCache.class.getName() + ".Instance";

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
     * @param _instance Instance the update will be registered for
     */
    public static void registerUpdate(final Instance _instance)
    {
        AccessCache.LOG.debug("registered Update for Instance: {}", _instance);
        final Cache<Instance, Set<AccessKey>> instanceCache = InfinispanCache.get()
                        .<Instance, Set<AccessKey>>getIgnReCache(AccessCache.INSTANCECACHE);
        instanceCache.remove(_instance);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(AccessCache.INSTANCECACHE)) {
            InfinispanCache.get().<Instance, Set<AccessKey>>getCache(AccessCache.INSTANCECACHE).clear();
        } else {
            final Cache<Instance, Set<AccessKey>> cache = InfinispanCache.get().<Instance, Set<AccessKey>>getCache(
                            AccessCache.INSTANCECACHE);
            cache.addListener(new CacheLogListener(AccessCache.LOG));
            cache.addListener(new InstanceCacheListener());
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

    /**
     * @return the Cache for AccessKey
     */
    public static Cache<AccessKey, Boolean> getKeyCache()
    {
        return  InfinispanCache.get().<AccessKey, Boolean>getCache(AccessCache.KEYCACHE);
    }

    /**
     * Listener responsible to maintain the two Caches in sync.
     * The instanceCache will receive the new AccessKey in the moment that a new AccessKey is inserted.
     */
    @Listener
    public static class KeyCacheListener
    {
        /**
         * If an AccessKey is inserted to KeyCache the instance will also be registered in the INSTANCECACHE.
         * @param _event event to be loged
         */
        @CacheEntryCreated
        public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> _event)
        {
            try {
                if (!_event.isPre()) {
                    final Cache<Instance, Set<AccessKey>> instanceCache = InfinispanCache.get()
                                    .<Instance, Set<AccessKey>>getIgnReCache(AccessCache.INSTANCECACHE);

                    final AccessKey accessKey = (AccessKey) _event.getKey();
                    final Instance instance = accessKey.getInstance();

                    Set<AccessKey> accessKeys;
                    if (instanceCache.containsKey(instance)) {
                        accessKeys = instanceCache.get(instance);
                    } else {
                        accessKeys = new HashSet<AccessKey>();
                        instanceCache.put(instance, accessKeys);
                    }
                    accessKeys.add(accessKey);
                }
            } catch (final CacheReloadException e) {
                AccessCache.LOG.error("Error on syncing the Caches for AccessCache", e);
            }
        }
    }

    /**
     * Listener responsible to maintain the two Caches in sync. On removal of an
     * instance from the instanceCache the related AccessKeys from the KeyCache
     * will be removed also.
     */
    @Listener
    public static class InstanceCacheListener
    {
        /**
         * If an Instance is removed the related AccessKeys will be removed
         * also..
         *
         * @param _event event to be loged
         */
        @SuppressWarnings("unchecked")
        @CacheEntryRemoved
        public void onCacheEntryRemoved(final CacheEntryRemovedEvent<?, ?> _event)
        {
            if (_event.isPre()) {
                final Cache<AccessKey, Boolean> keyCache = InfinispanCache.get().<AccessKey, Boolean>getIgnReCache(
                                AccessCache.KEYCACHE);
                final Set<AccessKey> accessKeys = (Set<AccessKey>) _event.getValue();
                if (accessKeys != null) {
                    for (final AccessKey accessKey : accessKeys) {
                        keyCache.remove(accessKey);
                    }
                }
            }
        }
    }
}

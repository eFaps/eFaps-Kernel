/*
 * Copyright 2003 - 2017 The eFaps Team
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


package org.efaps.admin.access.user;

import java.util.UUID;

import org.efaps.admin.access.AccessType;
import org.efaps.db.Instance;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Cache.
 *
 * @author The eFaps Team
 */
public final class AccessCache
{

    /**
     * Name of the Cache for AccessKey.
     */
    public static final String PERMISSIONCACHE = AccessCache.class.getName() + ".PermissionCache";

    /**
     * Name of the Cache for AccessKey.
     */
    public static final String STATUSCACHE = AccessCache.class.getName() + ".StatusCache";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccessCache.class);

    /**
     * Instantiates a new access cache.
     */
    private AccessCache()
    {
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(AccessCache.PERMISSIONCACHE)) {
            InfinispanCache.get().<UUID, AccessType>getCache(AccessCache.PERMISSIONCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, AccessType>getCache(AccessCache.PERMISSIONCACHE)
                            .addListener(new CacheLogListener(AccessCache.LOG));
        }
        if (InfinispanCache.get().exists(AccessCache.STATUSCACHE)) {
            InfinispanCache.get().<Long, AccessType>getCache(AccessCache.STATUSCACHE).clear();
        } else {
            InfinispanCache.get().<Long, AccessType>getCache(AccessCache.STATUSCACHE)
                            .addListener(new CacheLogListener(AccessCache.LOG));
        }
    }


    /**
     * @return the Cache for AccessKey
     */
    public static Cache<Key, PermissionSet> getPermissionCache()
    {
        return InfinispanCache.get().<Key, PermissionSet>getIgnReCache(AccessCache.PERMISSIONCACHE);
    }

    /**
     * @return the Cache for AccessKey
     */
    public static Cache<String, Long> getStatusCache()
    {
        return InfinispanCache.get().<String, Long>getIgnReCache(AccessCache.STATUSCACHE);
    }

    /**
     * @param _instance Instance the update will be registered for
     */
    public static void registerUpdate(final Instance _instance)
    {
        AccessCache.LOG.debug("Registered Update for: {}", _instance);
        AccessCache.getStatusCache().remove(_instance.getKey());
    }

    /**
     * @param _personId personid the access cache must be clean for
     */
    public static void clean4Person(final long _personId)
    {
        AccessCache.LOG.debug("Cleaning cache for Person: {}", _personId);
        final Cache<Key, PermissionSet> cache = AccessCache.getPermissionCache();
        final QueryFactory queryFactory = Search.getQueryFactory(cache);

        final Query query = queryFactory.from(PermissionSet.class)
                        .having("personId")
                        .eq(_personId)
                        .build();
        query.<PermissionSet>list().forEach(set -> {
            cache.remove(set.getKey());
        });
    }
}

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


package org.efaps.util.cache;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class InfinispanCache
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanCache.class);

    /**
     * The instance used for singelton.
     */
    private static InfinispanCache CACHEINSTANCE;

    /**
     * The manager for Infinspan.
     */
    private DefaultCacheManager manager;

    /**
     * Singelton is wanted.
     */
    private InfinispanCache()
    {
    }

    /**
     * init this instance.
     */
    private void init()
    {
        try {
            this.manager = new DefaultCacheManager(this.getClass().getResourceAsStream(
                            "/org/efaps/util/cache/infinispan-config.xml"));
            this.manager.addListener(new CacheLogListener(InfinispanCache.LOG));
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param _cacheName cache wanted
     * @param <K> Key
     * @param <V> Value
     * @return a cache from Infinspan
     */
    public <K, V> Cache<K, V> getCache(final String _cacheName)
    {
        return this.manager.getCache(_cacheName);
    }

    /**
     * @param _cacheName cache wanted
     * @return true if cache exists
     */
    public boolean exists(final String _cacheName)
    {
        return this.manager.cacheExists(_cacheName);
    }

    /**
     * @return the InfinispanCache
     */
    public static InfinispanCache get()
    {
        if (InfinispanCache.CACHEINSTANCE == null) {
            InfinispanCache.CACHEINSTANCE = new InfinispanCache();
            InfinispanCache.CACHEINSTANCE.init();
        }
        return InfinispanCache.CACHEINSTANCE;
    }

}

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
package org.efaps.util.cache;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.FactoryConfigurationError;

import org.efaps.init.INamingBinds;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class InfinispanCache
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanCache.class);

    /**
     * Sequence used to search for the CacheManager inside JNDI.
     */
    private static final String[] KNOWN_JNDI_KEYS = new String[] {
        "java:/" + INamingBinds.INFINISPANMANGER,
        "java:jboss/" + INamingBinds.INFINISPANMANGER,
        "java:comp/env/" + INamingBinds.INFINISPANMANGER };

    /**
     * Key to the Counter Cache.
     */
    private static String COUNTERCACHE = InfinispanCache.class.getName() + ".InternalCounter";

    /**
     * The instance used for singelton.
     */
    private static InfinispanCache CACHEINSTANCE;

    /**
     * The manager for Infinspan.
     */
    private EmbeddedCacheManager container;

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
        this.container = InfinispanCache.findCacheContainer();
        if (this.container == null) {
            try {
                this.container = new DefaultCacheManager(this.getClass().getResourceAsStream(
                                "/org/efaps/util/cache/infinispan-config.xml"));
                if (this.container instanceof EmbeddedCacheManager) {
                    this.container.addListener(new CacheLogListener(InfinispanCache.LOG));
                }
                InfinispanCache.bindCacheContainer(this.container);
                final Cache<String, Integer> cache = this.container
                                .<String, Integer>getCache(InfinispanCache.COUNTERCACHE);
                cache.put(InfinispanCache.COUNTERCACHE, 1);
            } catch (final FactoryConfigurationError e) {
                InfinispanCache.LOG.error("FactoryConfigurationError", e);
            } catch (final IOException e) {
                InfinispanCache.LOG.error("IOException", e);
            }
        } else {
            final Cache<String, Integer> cache = this.container
                            .<String, Integer>getCache(InfinispanCache.COUNTERCACHE);
            Integer count = cache.get(InfinispanCache.COUNTERCACHE);
            if (count == null) {
                count = 1;
            } else {
                count = count + 1;
            }
            cache.put(InfinispanCache.COUNTERCACHE, count);
        }
    }

    /**
     * Terminate the manager.
     */
    private void terminate()
    {
        if (this.container != null) {
            final Cache<String, Integer> cache = this.container
                            .<String, Integer>getCache(InfinispanCache.COUNTERCACHE);
            Integer count = cache.get(InfinispanCache.COUNTERCACHE);
            if (count == null || count < 2) {
                this.container.stop();
            } else {
                count = count - 1;
                cache.put(InfinispanCache.COUNTERCACHE, count);
            }
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
        return this.container.getCache(_cacheName, true);
    }

    /**
     * Gets the manager for Infinspan.
     *
     * @return the manager for Infinspan
     */
    public CacheContainer getContainer()
    {
        return this.container;
    }

    /**
     * An advanced cache that does not return the value of the previous value in
     * case of replacement.
     *
     * @param _cacheName cache wanted
     * @param <K> Key
     * @param <V> Value
     * @return an AdvancedCache from Infinispan with Ignore return values
     */
    public <K, V> AdvancedCache<K, V> getIgnReCache(final String _cacheName)
    {
        return this.container.<K, V>getCache(_cacheName, true).getAdvancedCache()
                        .withFlags(Flag.IGNORE_RETURN_VALUES, Flag.SKIP_REMOTE_LOOKUP, Flag.SKIP_CACHE_LOAD);
    }

    /**
     * Method to init a Cache using the default definitions.
     * @param _cacheName cache wanted
     * @param <K> Key
     * @param <V> Value
     * @return a cache from Infinspan
     */
    public <K, V> Cache<K, V> initCache(final String _cacheName)
    {
        if (!exists(_cacheName)
                        &&  ((EmbeddedCacheManager) getContainer()).getCacheConfiguration(_cacheName) == null) {
            ((EmbeddedCacheManager) getContainer()).defineConfiguration(_cacheName, "eFaps-Default",
                            new ConfigurationBuilder().build());
        }
        return this.container.getCache(_cacheName, true);
    }

    /**
     * @param _cacheName cache wanted
     * @return true if cache exists
     */
    public boolean exists(final String _cacheName)
    {
        final boolean ret;
        if (this.container instanceof EmbeddedCacheManager) {
            ret = this.container.cacheExists(_cacheName);
        } else {
            ret = this.container.getCache(_cacheName) != null;
        }
        return ret;
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
        if (InfinispanCache.CACHEINSTANCE.container instanceof DefaultCacheManager) {
            final ComponentStatus status = ((DefaultCacheManager) InfinispanCache.CACHEINSTANCE.container).getStatus();
            if (status.isStopping() || status.isTerminated()) {
                InfinispanCache.CACHEINSTANCE = new InfinispanCache();
                InfinispanCache.CACHEINSTANCE.init();
            }
        }
        return InfinispanCache.CACHEINSTANCE;
    }

    /**
     * Stop the manager.
     */
    public static void stop()
    {
        if (InfinispanCache.CACHEINSTANCE != null) {
            InfinispanCache.CACHEINSTANCE.terminate();
        }
    }

    /**
     * @param _container container to be binded in jndi
     */
    private static void bindCacheContainer(final CacheContainer _container)
    {
        InitialContext context = null;
        try {
            context = new InitialContext();
            context.bind(InfinispanCache.KNOWN_JNDI_KEYS[0], _container);
        } catch (final NamingException ex) {
            InfinispanCache.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
    }

    /**
     * @return the CacheContainer
     */
    private static EmbeddedCacheManager findCacheContainer()
    {
        EmbeddedCacheManager ret = null;
        InitialContext context = null;
        try {
            context = new InitialContext();
        } catch (final NamingException ex) {
            InfinispanCache.LOG.error("Could not initialise JNDI InitialContext", ex);
        }
        for (final String lookup : InfinispanCache.KNOWN_JNDI_KEYS) {
            if (lookup != null) {
                try {
                    ret = (EmbeddedCacheManager) context.lookup(lookup);
                    InfinispanCache.LOG.info("CacheContainer found in JNDI under '{}'", lookup);
                } catch (final NamingException e) {
                    InfinispanCache.LOG.debug("CacheContainer not found in JNDI under '{}'", lookup);
                }
            }
        }
        if (ret == null) {
            InfinispanCache.LOG.debug("No CacheContainer found under known names");
        }
        return ret;
    }
}

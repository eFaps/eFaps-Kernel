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


import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStarted;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStopped;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStartedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Listener
public class CacheLogListener
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CacheLogListener.class);


    private static CacheLogListener CACHELOG;

    @CacheStarted
    public void onCacheStarted(final CacheStartedEvent _event)
    {
        CacheLogListener.LOG.info("Cache '{}' started.", _event.getCacheName());
    }

    @CacheStopped
    public void onCacheStopped(final CacheStoppedEvent _event)
    {
        CacheLogListener.LOG.info("Cache {} started.", _event.getCacheName());
    }


    @CacheEntryCreated
    public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> event)
    {
        if (!event.isPre()) {
            CacheLogListener.LOG.info("Added key: '{}' to Cache '{}'. ", event.getKey(), event.getCache().getName());
        }
    }




}

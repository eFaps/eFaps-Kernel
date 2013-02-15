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
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryLoaded;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryVisited;
import org.infinispan.notifications.cachelistener.event.CacheEntriesEvictedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryActivatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryLoadedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryVisitedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStarted;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStopped;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStartedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStoppedEvent;
import org.slf4j.Logger;

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
    private final Logger log;


   public CacheLogListener(final Logger _log) {
       this.log = _log;
   }

    @CacheStarted
    public void onCacheStarted(final CacheStartedEvent _event)
    {
        this.log.info("Cache '{}' started.", _event.getCacheName());
    }

    @CacheStopped
    public void onCacheStopped(final CacheStoppedEvent _event)
    {
        this.log.info("Cache {} stopped.", _event.getCacheName());
    }


    @CacheEntryCreated
    public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> event)
    {
        if (!event.isPre()) {
            this.log.trace("Added key: '{}' to Cache '{}'. ", event.getKey(), event.getCache().getName());
        }
    }


    @CacheEntryLoaded
    public void onCacheEntryLoaded(final CacheEntryLoadedEvent<?, ?> event)
    {
        this.log.trace("loaded key: '{}' from Cache '{}'. ", event.getKey(), event.getCache().getName());
    }

    @CacheEntryVisited
    public void onCacheEntryVisited(final CacheEntryVisitedEvent<?, ?> event)
    {
        this.log.trace("visited key: '{}' from Cache '{}'. ", event.getKey(), event.getCache().getName());
    }

    @CacheEntryActivated
    public void onCacheEntryActivated(final CacheEntryActivatedEvent<?, ?> event)
    {
        this.log.trace("activated key: '{}' from Cache '{}'. ", event.getKey(), event.getCache().getName());
    }

    @CacheEntriesEvicted
    public void onCacheEntriesEvicted(final CacheEntriesEvictedEvent<?, ?> event)
    {
        this.log.trace("evicted entries: '{}' from Cache '{}'. ", event.getEntries(), event.getCache().getName());
    }


    @CacheEntryRemoved
    public void onCacheEntryRemoved(final CacheEntryRemovedEvent<?, ?> event)
    {
        this.log.trace("removed key: '{}' from Cache '{}'. ", event.getKey(), event.getCache().getName());
    }



}

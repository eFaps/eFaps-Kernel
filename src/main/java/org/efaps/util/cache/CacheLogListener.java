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
 * @version $Id: CacheLogListener.java 8807 2013-02-15 18:33:57Z jan@moxter.net
 *          $
 */
@Listener
public class CacheLogListener
{

    /**
     * Logger for this class.
     */
    private final Logger log;

    /**
     * @param _log Logger to be logged to
     */
    public CacheLogListener(final Logger _log)
    {
        this.log = _log;
    }

    /**
     * @param _event event to be loged
     */
    @CacheStarted
    public void onCacheStarted(final CacheStartedEvent _event)
    {
        this.log.info("Cache '{}' started.", _event.getCacheName());
    }

    /**
     * @param _event event to be loged
     */
    @CacheStopped
    public void onCacheStopped(final CacheStoppedEvent _event)
    {
        this.log.info("Cache {} stopped.", _event.getCacheName());
    }

    /**
     * @param _event event to be loged
     */
    @CacheEntryCreated
    public void onCacheEntryCreated(final CacheEntryCreatedEvent<?, ?> _event)
    {
        if (!_event.isPre()) {
            this.log.trace("Added key: '{}' to Cache '{}'. ", _event.getKey(), _event.getCache().getName());
        }
    }

    /**
     * @param _event event to be loged
     */
    @CacheEntryLoaded
    public void onCacheEntryLoaded(final CacheEntryLoadedEvent<?, ?> _event)
    {
        this.log.trace("loaded key: '{}' from Cache '{}'. ", _event.getKey(), _event.getCache().getName());
    }

    /**
     * @param _event event to be loged
     */
    @CacheEntryVisited
    public void onCacheEntryVisited(final CacheEntryVisitedEvent<?, ?> _event)
    {
        this.log.trace("visited key: '{}' from Cache '{}'. ", _event.getKey(), _event.getCache().getName());
    }

    /**
     * @param _event event to be loged
     */
    @CacheEntryActivated
    public void onCacheEntryActivated(final CacheEntryActivatedEvent<?, ?> _event)
    {
        this.log.trace("activated key: '{}' from Cache '{}'. ", _event.getKey(), _event.getCache().getName());
    }

    /**
     * @param _event event to be loged
     */
    @CacheEntriesEvicted
    public void onCacheEntriesEvicted(final CacheEntriesEvictedEvent<?, ?> _event)
    {
        this.log.trace("evicted entries: '{}' from Cache '{}'. ", _event.getEntries(), _event.getCache().getName());
    }

    /**
     * @param _event event to be loged
     */
    @CacheEntryRemoved
    public void onCacheEntryRemoved(final CacheEntryRemovedEvent<?, ?> _event)
    {
        this.log.trace("removed key: '{}' from Cache '{}'. ", _event.getKey(), _event.getCache().getName());
    }
}

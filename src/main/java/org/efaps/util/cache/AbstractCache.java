/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The class is used to cache three not independent information which are
 * completely defined once but needed many times within eFaps. An example
 * is to load all types at startup and access the cached
 * data instead of reading the cached information each time they are
 * needed.</p>
 * <p>If a reload of the cache is needed, the cached object could be accessed,
 * but returns the &quot;old&quot; values until the old cache is replaced by
 * the new read values.</p>
 * <p>The class is thread save, but a cached instance could be accessed much
 * faster than a synchronized hash map.</p>
 *
 * @author The eFaps Team
 * @param <T> class implementing CacheObjectInterface
 * @version $Id$
 */
public abstract class AbstractCache<T extends CacheObjectInterface>
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCache.class);

    /**
     * Set that stores all initialized Caches.
     */
    private static Set<AbstractCache<?>> CACHES = Collections.synchronizedSet(new HashSet<AbstractCache<?>>());

    /**
     * The map holds all cached data instances by Id. Because of the
     * double-checked locking idiom, the instance variable is defined
     * <i>volatile</i>.
     *
     * @see #get(Long)
     */
    private volatile Map<Long, T> cache4Id = null;

    /**
     * The map holds all cached data instances by Name. Because of the
     * double-checked locking idiom, the instance variable is defined
     * <i>volatile</i>.
     *
     * @see #get(String)
     */
    private volatile Map<String, T> cache4Name = null;

    /**
     * The map holds all cached data instances by UUID. Because of the
     * double-checked locking idiom, the instance variable is defined
     * <i>volatile</i>.
     *
     * @see #get(UUID)
     */
    private volatile Map<UUID, T> cache4UUID = null;

    /**
     * Stores the class name of the class that initialized this cache.
     */
    private String initializer;

    /**
     * Constructor adding this Cache to the set of Caches.
     */
    protected AbstractCache()
    {
        AbstractCache.CACHES.add(this);
    }

    /**
     * Returns for given key id the cached object from the cache4Id cache. If
     * the cache was not initialized yet, <code>null</code> is returned.
     *
     * @see #getCache4Id()
     * @param _id       id of the searched cached object
     * @return cached object
     *
     */
    public T get(final long _id)
    {
        return getCache4Id() == null ? null : getCache4Id().get(new Long(_id));
    }

    /**
     * Returns for given key id the cached object from the cache4Id cache. If
     * the cache was not initialized yet, <code>null</code> is returned.
     *
     * @see #getCache4Name()
     * @param _name     name the cached object
     * @return cached object
     */
    public T get(final String _name)
    {
        return getCache4Name() == null ? null : getCache4Name().get(_name);
    }

    /**
     * Returns for given key id the cached object from the cache4Id cache. If
     * the cache was not initialized yet, <code>null</code> is returned.
     *
     * @see #getCache4UUID()
     * @param _uuid     UUID of the cached object
     * @return cached object
     *
     */
    public T get(final UUID _uuid)
    {
        return getCache4UUID() == null ? null : getCache4UUID().get(_uuid);
    }

    /**
     * Add an object to this Cache. This method should only be used to add some
     * objects, due to the reason that it is very slow!!! Normally the values
     * should be added by using abstract method
     * {@link #readCache(Map, Map, Map)}.
     *
     * @param _object Object to be added
     */
    public void addObject(final T _object)
    {
        final Map<Long, T> newCache4Id =  getNewCache4Id();
        final Map<String, T> newCache4Name = getNewCache4Name();
        final Map<UUID, T>  newCache4UUID = getNewCache4UUID();

        if (getCache4Id() != null) {
            newCache4Id.putAll(getCache4Id());
        }

        if (getCache4Name() != null) {
            newCache4Name.putAll(getCache4Name());
        }

        if (getCache4UUID() != null) {
            newCache4UUID.putAll(getCache4UUID());
        }

        newCache4Id.put(_object.getId(), _object);
        newCache4Name.put(_object.getName(), _object);
        newCache4UUID.put(_object.getUUID(), _object);

        // replace old cache with new values
        // it is thread save because of volatile
        setCache4Id(newCache4Id);
        setCache4Name(newCache4Name);
        setCache4UUID(newCache4UUID);
    }

    /**
     * The method tests, if the cache has stored some entries.
     *
     * @return <i>true</i> if the cache has some entries, otherwise
     *         <i>false</i>
     */
    public boolean hasEntries()
    {
        return (getCache4Id() != null && !getCache4Id().isEmpty())
            || (getCache4Name() != null && !getCache4Name().isEmpty())
            || (getCache4UUID() != null && !getCache4UUID().isEmpty());
    }

    /**
     * The method initialize the cache.
     *
     * @param _initializer class name of the class initializing
     *
     */
    public void initialize(final Class<?> _initializer)
    {
        this.initializer = _initializer.getName();
        synchronized (this) {
            try {
                readCache();
            } catch (final CacheReloadException e) {
                AbstractCache.LOG.error("Unexpected error while initializing Cache for " + getClass(), e);
            }
        }
    }

    /**
     * The complete cache is read and then replaces the current stored values
     * in the cache. The cache must be read in this order:
     * <ul>
     * <li>create new cache map</li>
     * <li>read cache in the new cache map</li>
     * <li>replace old cache map with the new cache map</li>
     * </ul>
     * This order is imported, otherwise
     * <ul>
     * <li>if the cache initialized first time, the cache is not
     *     <code>null</code> and returns then wrong values</li>
     * <li>existing and read values could not be read while a reload is
     *     done</li>
     * </ul>
     *
     * @throws CacheReloadException if the cache could not be read (the
     *                              exception is also written into the error
     *                              log)
     */
    private void readCache()
        throws CacheReloadException
    {
        // if cache is not initialized, the correct order is required!
        // otherwise the cache is not null and returns wrong values!
        final Map<Long, T> newCache4Id = getNewCache4Id();
        final Map<String, T> newCache4Name = getNewCache4Name();
        final Map<UUID, T>  newCache4UUID = getNewCache4UUID();
        try {
            readCache(newCache4Id, newCache4Name, newCache4UUID);
        } catch (final CacheReloadException e) {
            AbstractCache.LOG.error("Read Cache for " + getClass() + " failed", e);
            throw e;
            //CHECKSTYLE:OFF
        } catch (final Exception e) {
            //CHECKSTYLE:ON
            AbstractCache.LOG.error("Unexpected error while reading Cache for " + getClass(), e);
            throw new CacheReloadException("Unexpected error while reading Cache " + "for " + getClass(), e);
        }
        // replace old cache with new values
        // it is thread save because of volatile
        this.cache4Id = newCache4Id;
        this.cache4Name = newCache4Name;
        this.cache4UUID = newCache4UUID;
    }

    /**
     * Method to fill this cache with objects.
     *
     * @param _newCache4Id      cache for id
     * @param _newCache4Name    cache for name
     * @param _newCache4UUID    cache for UUID
     * @throws CacheReloadException on error during reading
     */
    protected abstract void readCache(final Map<Long, T> _newCache4Id,
                                      final Map<String, T> _newCache4Name,
                                      final Map<UUID, T> _newCache4UUID)
        throws CacheReloadException;

    /**
     * Method to get a new empty Map for the id Cache.
     * The method is implemented to provide the possibility to use
     * different Map types in the extending classes.
     *
     * @return new empty Instance of a HashMap
     */
    protected Map<Long, T> getNewCache4Id()
    {
        return new HashMap<Long, T>();
    }

    /**
     * Method to get a new empty Map for the id Cache.
     * The method is implemented to provide the possibility to use
     * different Map types in the extending classes.
     *
     * @return new empty Instance of a HashMap
     */
    protected Map<String, T> getNewCache4Name()
    {
        return new HashMap<String, T>();
    }

    /**
     * Method to get a new empty Map for the id Cache.
     * The method is implemented to provide the possibility to use
     * different Map types in the extending classes.
     *
     * @return new empty Instance of a HashMap
     */
    protected Map<UUID, T> getNewCache4UUID()
    {
        return new HashMap<UUID, T>();
    }

    /**
     * Getter method for instance variable {@link #cache4Id}.
     *
     * @return value of instance variable {@link #cache4Id}
     */
    public Map<Long, T> getCache4Id()
    {
        return this.cache4Id;
    }

    /**
     * Setter method for instance variable {@link #cache4Id}.
     *
     * @param _cache4Id value for instance variable {@link #cache4Id}
     */
    protected void setCache4Id(final Map<Long, T> _cache4Id)
    {
        this.cache4Id = _cache4Id;
    }

    /**
     * Getter method for instance variable {@link #cache4Name}.
     *
     * @return value of instance variable {@link #cache4Name}
     */
    protected Map<String, T> getCache4Name()
    {
        return this.cache4Name;
    }

    /**
     * Setter method for instance variable {@link #cache4Name}.
     *
     * @param _cache4Name value for instance variable {@link #cache4Name}
     */
    protected void setCache4Name(final Map<String, T> _cache4Name)
    {
        this.cache4Name = _cache4Name;
    }

    /**
     * Setter method for instance variable {@link #cache4UUID}.
     *
     * @param _cache4uuid value for instance variable {@link #cache4UUID}
     */
    protected void setCache4UUID(final Map<UUID, T> _cache4uuid)
    {
        this.cache4UUID = _cache4uuid;
    }

    /**
     * Getter method for instance variable {@link #cache4UUID}.
     *
     * @return value of instance variable {@link #cache4UUID}
     */
    protected Map<UUID, T> getCache4UUID()
    {
        return this.cache4UUID;
    }

    /**
     * Getter method for instance variable {@link #initializer}.
     *
     * @return value of instance variable {@link #initializer}
     */
    public String getInitializer()
    {
        return this.initializer;
    }

    /**
     * Clear all values of this Cache.
     */
    public void clear()
    {
        if (getCache4Id() != null) {
            getCache4Id().clear();
        }
        if (getCache4Name() != null) {
            getCache4Name().clear();
        }
        if (getCache4UUID() != null) {
            getCache4UUID().clear();
        }
    }

    /**
     * The static method removes all values in all caches.
     */
    public static void clearCaches()
    {
        synchronized (AbstractCache.CACHES) {
            for (final AbstractCache<?> cache : AbstractCache.CACHES) {
                cache.getCache4Id().clear();
                cache.getCache4Name().clear();
                cache.getCache4UUID().clear();
            }
        }
    }

    /**
     * Getter method for variable {@link #CACHES}.
     *
     * @return value of variable {@link #CACHES}
     */
    public static Set<AbstractCache<?>> getCaches()
    {
        return AbstractCache.CACHES;
    }
}

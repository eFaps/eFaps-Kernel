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

package org.efaps.util.cache;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.CacheCollection;
import org.infinispan.CacheSet;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.filter.KeyFilter;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverter;
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter;

/**
 * A Cache that looks like a normal Cache but is not Operational at all.
 *
 * @author The eFaps Team
 * @param <K> Key
 * @param <V> Value
 */
public class NoOpCache<K, V>
    implements Cache<K, V>
{

    @Override
    public V put(final K _key,
                 final V _value)
    {
        return _value;
    }

    @Override
    public CompletableFuture<V> putAsync(final K _key,
                                       final V _value)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> putAsync(final K _key,
                                       final V _value,
                                       final long _lifespan,
                                       final TimeUnit _unit)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> putAsync(final K _key,
                                       final V _value,
                                       final long _lifespan,
                                       final TimeUnit _lifespanUnit,
                                       final long _maxIdle,
                                       final TimeUnit _maxIdleUnit)
    {
        return null;
    }

    @Override
    public CompletableFuture<Void> putAllAsync(final Map<? extends K, ? extends V> _data)
    {
        return null;
    }

    @Override
    public CompletableFuture<Void> putAllAsync(final Map<? extends K, ? extends V> _data,
                                             final long _lifespan,
                                             final TimeUnit _unit)
    {
        return null;
    }

    @Override
    public CompletableFuture<Void> putAllAsync(final Map<? extends K, ? extends V> _data,
                                             final long _lifespan,
                                             final TimeUnit _lifespanUnit,
                                             final long _maxIdle,
                                             final TimeUnit _maxIdleUnit)
    {
        return null;
    }

    @Override
    public CompletableFuture<Void> clearAsync()
    {
        return null;
    }

    @Override
    public CompletableFuture<V> putIfAbsentAsync(final K _key,
                                               final V _value)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> putIfAbsentAsync(final K _key,
                                               final V _value,
                                               final long _lifespan,
                                               final TimeUnit _unit)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> putIfAbsentAsync(final K _key,
                                               final V _value,
                                               final long _lifespan,
                                               final TimeUnit _lifespanUnit,
                                               final long _maxIdle,
                                               final TimeUnit _maxIdleUnit)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> removeAsync(final Object _key)
    {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> removeAsync(final Object _key,
                                                final Object _value)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> replaceAsync(final K _key,
                                           final V _value)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> replaceAsync(final K _key,
                                           final V _value,
                                           final long _lifespan,
                                           final TimeUnit _unit)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> replaceAsync(final K _key,
                                           final V _value,
                                           final long _lifespan,
                                           final TimeUnit _lifespanUnit,
                                           final long _maxIdle,
                                           final TimeUnit _maxIdleUnit)
    {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> replaceAsync(final K _key,
                                                 final V _oldValue,
                                                 final V _newValue)
    {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> replaceAsync(final K _key,
                                                 final V _oldValue,
                                                 final V _newValue,
                                                 final long _lifespan,
                                                 final TimeUnit _unit)
    {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> replaceAsync(final K _key,
                                                 final V _oldValue,
                                                 final V _newValue,
                                                 final long _lifespan,
                                                 final TimeUnit _lifespanUnit,
                                                 final long _maxIdle,
                                                 final TimeUnit _maxIdleUnit)
    {
        return null;
    }

    @Override
    public CompletableFuture<V> getAsync(final K _key)
    {
        return null;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public String getVersion()
    {
        return null;
    }

    @Override
    public V put(final K _key,
                 final V _value,
                 final long _lifespan,
                 final TimeUnit _unit)
    {
        return _value;
    }

    @Override
    public V putIfAbsent(final K _key,
                         final V _value,
                         final long _lifespan,
                         final TimeUnit _unit)
    {
        return _value;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> _map,
                       final long _lifespan,
                       final TimeUnit _unit)
    {

    }

    @Override
    public V replace(final K _key,
                     final V _value,
                     final long _lifespan,
                     final TimeUnit _unit)
    {
        return _value;
    }

    @Override
    public boolean replace(final K _key,
                           final V _oldValue,
                           final V _value,
                           final long _lifespan,
                           final TimeUnit _unit)
    {
        return false;
    }

    @Override
    public V put(final K _key,
                 final V _value,
                 final long _lifespan,
                 final TimeUnit _lifespanUnit,
                 final long _maxIdleTime,
                 final TimeUnit _maxIdleTimeUnit)
    {
        return _value;
    }

    @Override
    public V putIfAbsent(final K _key,
                         final V _value,
                         final long _lifespan,
                         final TimeUnit _lifespanUnit,
                         final long _maxIdleTime,
                         final TimeUnit _maxIdleTimeUnit)
    {
        return _value;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> _map,
                       final long _lifespan,
                       final TimeUnit _lifespanUnit,
                       final long _maxIdleTime,
                       final TimeUnit _maxIdleTimeUnit)
    {

    }

    @Override
    public V replace(final K _key,
                     final V _value,
                     final long _lifespan,
                     final TimeUnit _lifespanUnit,
                     final long _maxIdleTime,
                     final TimeUnit _maxIdleTimeUnit)
    {
        return _value;
    }

    @Override
    public boolean replace(final K _key,
                           final V _oldValue,
                           final V _value,
                           final long _lifespan,
                           final TimeUnit _lifespanUnit,
                           final long _maxIdleTime,
                           final TimeUnit _maxIdleTimeUnit)
    {
        return false;
    }

    @Override
    public V putIfAbsent(final K _key,
                         final V _value)
    {
        return _value;
    }

    @Override
    public boolean remove(final Object _key,
                          final Object _value)
    {
        return false;
    }

    @Override
    public boolean replace(final K _key,
                           final V _oldValue,
                           final V _newValue)
    {
        return false;
    }

    @Override
    public V replace(final K _key,
                     final V _value)
    {
        return _value;
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop()
    {

    }

    @Override
    public boolean startBatch()
    {
        return false;
    }

    @Override
    public void endBatch(final boolean _successful)
    {

    }

    @Override
    public void addListener(final Object _listener,
                            final KeyFilter<? super K> _filter)
    {

    }

    @Override
    public void addListener(final Object _listener)
    {

    }

    @Override
    public void removeListener(final Object _listener)
    {

    }

    @Override
    public Set<Object> getListeners()
    {
        return Collections.<Object>emptySet();
    }

    @Override
    public void putForExternalRead(final K _key,
                                   final V _value)
    {
    }

    @Override
    public void evict(final K _key)
    {

    }

    @Override
    public Configuration getCacheConfiguration()
    {
        return null;
    }

    @Override
    public EmbeddedCacheManager getCacheManager()
    {
        return null;
    }

    @Override
    public AdvancedCache<K, V> getAdvancedCache()
    {
        return null;
    }

    @Override
    public ComponentStatus getStatus()
    {
        return null;
    }

    @Override
    public <C> void addListener(final Object _listener,
                                final CacheEventFilter<? super K, ? super V> _filter,
                                final CacheEventConverter<? super K, ? super V, C> _converter)
    {
    }

    @Override
    public void putForExternalRead(final K _key,
                                   final V _value,
                                   final long _lifespan,
                                   final TimeUnit _unit)
    {
    }

    @Override
    public void putForExternalRead(final K _key,
                                   final V _value,
                                   final long _lifespan,
                                   final TimeUnit _lifespanUnit,
                                   final long _maxIdle,
                                   final TimeUnit _maxIdleUnit)
    {
    }

    @Override
    public V remove(final Object _key)
    {
        return null;
    }

    @Override
    public boolean containsKey(final Object _key)
    {
        return false;
    }

    @Override
    public boolean containsValue(final Object _value)
    {
        return false;
    }

    @Override
    public V get(final Object _key)
    {
        return null;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> _m)
    {
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public CacheSet<K> keySet()
    {
        return null;
    }

    @Override
    public CacheCollection<V> values()
    {
        return null;
    }

    @Override
    public CacheSet<java.util.Map.Entry<K, V>> entrySet()
    {
        return null;
    }

    @Override
    public void clear()
    {
    }

    @Override
    public <C> void addFilteredListener(final Object _listener,
                                        final CacheEventFilter<? super K, ? super V> _filter,
                                        final CacheEventConverter<? super K, ? super V, C> _converter,
                                        final Set<Class<? extends Annotation>> _filterAnnotations)
    {
    }
}

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

import java.util.UUID;

/**
 * Cache that is initialized automatically on the first access to it.
 *
 * @author The eFaps Team
 * @version $Id$
 * @param <K> CacheObjectInterface
 */
public abstract class AutomaticCache<K extends CacheObjectInterface>
    extends Cache<K>
{
    /**
     * Returns for given key id the cached object from the cache4Id cache. If
     * the cache is NOT initialized <code>null</code> is returned.
     *
     * @param _id   id of searched cached object
     * @return cached object
     */
    @Override()
    public K get(final long _id)
    {
        if (!hasEntries()) {
            initialize(AutomaticCache.class);
        }
        return getCache4Id().get(new Long(_id));
    }

    /**
     * Returns for given key id the cached object from the cache4Id cache. If
     * the cache is NOT initialized, the cache will be initialized.
     *
     * @param _name     name of searched cached object
     * @return cached object
     */
    @Override()
    public K get(final String _name)
    {
        if (!hasEntries()) {
            initialize(AutomaticCache.class);
        }
        return getCache4Name().get(_name);
    }

    /**
     * Returns for given key id the cached object from the cache4Id cache. If
     * the cache is NOT initialized, the cache will be initialized.
     *
     * @param _uuid     UUID of searched cached object
     * @return cached object
     */
    @Override()
    public K get(final UUID _uuid)
    {
        if (!hasEntries()) {
            initialize(AutomaticCache.class);
        }
        return getCache4UUID().get(_uuid);
    }
}

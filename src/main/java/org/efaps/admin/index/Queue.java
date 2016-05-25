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

package org.efaps.admin.index;

import org.apache.commons.lang3.RandomStringUtils;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.AdvancedCache;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class Queue
{

    /**
     * Name of the Cache for Instances.
     */
    public static final String CACHENAME = Queue.class.getName() + ".Cache";

    /**
     * Instantiates a new queue.
     */
    private Queue()
    {
    }

    /**
     * Register update.
     *
     * @param _instance the _instance
     * @throws EFapsException the e faps exception
     */
    public static void registerUpdate(final Instance _instance)
        throws EFapsException
    {
        // check if SystemConfiguration exists, necessary during install
        if (EFapsSystemConfiguration.get() != null
                        && EFapsSystemConfiguration.get().getAttributeValueAsBoolean(KernelSettings.INDEXACTIVATE)) {
            if (_instance != null && _instance.getType() != null
                            && IndexDefinition.get(_instance.getType().getUUID()) != null) {
                final AdvancedCache<String, String> cache = InfinispanCache.get()
                                .<String, String>getIgnReCache(CACHENAME);
                cache.put(RandomStringUtils.random(12), _instance.getOid());
            }
        }
    }
}

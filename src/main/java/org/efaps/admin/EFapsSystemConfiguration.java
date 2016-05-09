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

package org.efaps.admin;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.util.cache.CacheReloadException;

/**
 * {@link SystemConfiguration system configurations} used from eFaps kernel.
 *
 * @author The eFaps Team
 *
 */
public final class EFapsSystemConfiguration
{

    /** The uuid. */
    public static final UUID UUID = java.util.UUID.fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079");

    /**
     * Utility Class.
     */
    private EFapsSystemConfiguration()
    {

    }

    /**
     * Returns related {@link SystemConfiguration system configuration}
     * instance.
     *
     * @return related system configuration instance
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration get()
        throws CacheReloadException
    {
        return SystemConfiguration.get(UUID);
    }
}

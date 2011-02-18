/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.admin;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;

/**
 * Enumeration for {@link SystemConfiguration system configurations} used from
 * eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public enum EFapsSystemConfiguration
{
    /**
     * Kernel system configuration.
     */
    KERNEL("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079");

    /**
     * Stores the {@link UUID} of related system configuration.
     */
    private final UUID uuid;

    /**
     * Initializes the {@link #uuid} for the system configuration.
     *
     * @param _uuid     string representation of the UUID
     */
    private EFapsSystemConfiguration(final String _uuid)
    {
        this.uuid = UUID.fromString(_uuid);
    }

    /**
     * Returns related {@link SystemConfiguration system configuration}
     * instance.
     *
     * @return related system configuration instance
     */
    public SystemConfiguration get()
    {
        return SystemConfiguration.get(this.uuid);
    }
}

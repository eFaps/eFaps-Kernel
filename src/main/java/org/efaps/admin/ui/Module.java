/*
 * Copyright 2003 - 2023 The eFaps Team
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

package org.efaps.admin.ui;

import java.util.UUID;

import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Module
    extends AbstractUserInterfaceObject
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Module.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _id       id
     * @param _uuid     UUID
     * @param _name     name
     */
    public Module(final Long id,
                  final String uuid,
                  final String name)
    {
        super(id, uuid, name);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Module}
     * .
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Module}
     * @throws CacheReloadException on error
     */
    public static Module get(final long _id)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Module>get(_id, Module.class, CIAdminUserInterface.Module.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Module}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Module}
     * @throws CacheReloadException on error
     */
    public static Module get(final String _name)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Module>get(_name, Module.class, CIAdminUserInterface.Module.getType());
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Module}.
     *
     * @param _uuid UUID to search in the cache
     * @return instance of class {@link Module}
     * @throws CacheReloadException on error
     */
    public static Module get(final UUID _uuid)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Module>get(_uuid, Module.class, CIAdminUserInterface.Module.getType());
    }
}

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

package org.efaps.admin.ui;

import java.util.UUID;

import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 *
 * TODO: description
 */
public class Command
    extends AbstractCommand
{
    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Command.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to set the id and name of the command object.
     *
     * @param _id       id of the command to set
     * @param _uuid     UUID of the command to set
     * @param _name     name of the command to set
     */
    public Command(final Long _id,
                   final String _uuid,
                   final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Returns for given parameter <i>UUID</i> the instance of class
     * {@link Command}.
     *
     * @param _uuid         UUID to search in the cache
     * @return instance of class {@link Command}
     * @throws CacheReloadException on error
     */
    public static Command get(final UUID _uuid)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Command>get(_uuid, Command.class, CIAdminUserInterface.Command.getType());
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Command}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Command}
     * @throws CacheReloadException on error
     */
    public static Command get(final long _id)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Command>get(_id, Command.class, CIAdminUserInterface.Command.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Command}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Command}
     * @throws CacheReloadException on error
     */
    public static Command get(final String _name)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Command>get(_name, Command.class, CIAdminUserInterface.Command.getType());
    }
}

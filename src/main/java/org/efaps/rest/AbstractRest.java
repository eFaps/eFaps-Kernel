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


package org.efaps.rest;

import java.util.UUID;

import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for all Rest implementations inside eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractRest
{
    /**
     * Logging instance used to give logging information of this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractRest.class);

    /**
     * Check if the logged in users has access to rest.
     * User must be assigned to the Role "Admin_Rest".
     *
     * @return true if user is assigned to Roles "Admin_Rest", else false
     * @throws CacheReloadException on error
     * @throws EFapsException on error
     */
    protected boolean hasAccess()
        throws CacheReloadException, EFapsException
    {
        //Admin_REST
        return Context.getThreadContext().getPerson().isAssigned(Role.get(
                        UUID.fromString("2d142645-140d-46ad-af67-835161a8d732")));
    }
}

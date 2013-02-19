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


package org.efaps.ci;

import java.util.UUID;


/**
 * Each class that extends this abstract class represents a configuration
 * item from eFaps. It is used to have easy access during the
 * coding. The classes are created automatically
 * with a maven target.
 *
 * @author The eFaps Team
 * @version $Id$
 */
//CHECKSTYLE:OFF
public abstract class CIObject
{
    /**
     * UUID of this ci object.
     */
    public final UUID uuid;

    //CHECKSTYLE:ON
    /**
     * Constructor setting the uuid.
     * @param _uuid UUID of this type
     */
    protected CIObject(final String _uuid)
    {
        this.uuid = UUID.fromString(_uuid);
    }
}

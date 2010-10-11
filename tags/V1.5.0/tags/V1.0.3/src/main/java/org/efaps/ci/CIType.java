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


package org.efaps.ci;

import java.util.UUID;

/**
 * Each class that extends this abstract class represents a configuration
 * item for a type from eFaps. It is used to have easy access during the
 * coding of esjp etc. to the configuration items without having the problem
 * of using strings to access them. The classes are created automatically
 * with a maven target.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class CIType
{
    /**
     * ID attribute. Each type must have it.
     */
    public final CIAttribute ID = new CIAttribute(this, "ID");

    /**
     * OID attribute.Each type must have it.
     */
    public final CIAttribute OID = new CIAttribute(this, "OID");

    /**
     * Type attribute.Each type must have it.
     */
    public final CIAttribute Type = new CIAttribute(this, "Type");

    /**
     * UUID of this type.
     */
    public final UUID uuid;

    /**
     * Constructor setting the uuid.
     * @param _uuid UUID of this type
     */
    protected CIType(final String _uuid)
    {
        this.uuid = UUID.fromString(_uuid);
    }

    /**
     * Get the type this Configuration item represents.
     * @return Type
     */
    public org.efaps.admin.datamodel.Type getType()
    {
        return org.efaps.admin.datamodel.Type.get(this.uuid);
    }
}

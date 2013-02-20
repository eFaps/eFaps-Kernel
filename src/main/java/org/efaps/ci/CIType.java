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

import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
//CHECKSTYLE:OFF
public abstract class CIType
    extends CIObject
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CIType.class);

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
   //CHECKSTYLE:ON
    /**
     * Constructor setting the uuid.
     * @param _uuid UUID of this type
     */
    protected CIType(final String _uuid)
    {
        super(_uuid);
    }

    /**
     * Get the type this Configuration item represents.
     *
     * @return Type
     */
    public org.efaps.admin.datamodel.Type getType()
    {
        org.efaps.admin.datamodel.Type ret = null;
        try {
            ret =  org.efaps.admin.datamodel.Type.get(this.uuid);
        } catch (final CacheReloadException e) {
            CIType.LOG.error("Error on retrieving Type for CIType with uuid: {}", this.uuid);
        }
        return ret;
    }
}

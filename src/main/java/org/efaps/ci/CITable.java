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


package org.efaps.ci;

import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Each class that extends this abstract class represents a configuration
 * item for a table from eFaps. It is used to have easy access during the
 * coding of esjp etc. to the configuration items without having the problem
 * of using strings to access them. The classes are created automatically
 * with a maven target.
 *
 * @author The eFaps Team
 *
 */
//CHECKSTYLE:OFF
public abstract class CITable
    extends CICollection
{
//CHECKSTYLE:ON

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CIObject.class);

    /**
     * Constructor setting the uuid.
     * @param _uuid UUID of this type
     */
    protected CITable(final String _uuid)
    {
        super(_uuid);
    }

    /**
     * Get the type this Configuration item represents.
     * @return Table
     */
    public org.efaps.admin.ui.Table getType()
    {
        org.efaps.admin.ui.Table ret = null;
        try {
            ret =  org.efaps.admin.ui.Table.get(this.uuid);
        } catch (final CacheReloadException e) {
            CITable.LOG.error("Error on retrieving Type for CIType with uuid: {}", this.uuid);
        }
        return ret;
    }
}

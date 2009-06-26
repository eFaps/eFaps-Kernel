/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.admin.datamodel;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractDataModelObject extends AbstractAdminObject
{

    /**
     * Constructor to set the id and name of the data model object.
     *
     * @param _id id to set
     * @param _uuid universal unique identifier
     * @param _name name to set
     */
    protected AbstractDataModelObject(final long _id, final String _uuid, final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Initialize the cache of all data model objects.
     *
     * @throws CacheReloadException if cache could not be initialized
     */
    public static void initialize() throws CacheReloadException
    {
        AttributeType.initialize(AbstractDataModelObject.class);
        SQLTable.initialize(AbstractDataModelObject.class);
        Type.initialize(AbstractDataModelObject.class);
        Dimension.initialize(AbstractDataModelObject.class);
        Attribute.initialize(AbstractDataModelObject.class);
    }
}
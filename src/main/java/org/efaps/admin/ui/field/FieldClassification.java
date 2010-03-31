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

package org.efaps.admin.ui.field;

import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldClassification extends Field
{

    /**
     * Stores the classification for this field.
     */
    private String classificationName;

    /**
     * This is the constructor of the field class.
     *
     * @param _id       id of the field instance
     * @param _uuid     UUID of the field instance
     * @param _name     name of the field instance
     */
    public FieldClassification(final long _id, final String _uuid, final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Getter method for instance variable {@link #classificationName}.
     *
     * @return value of instance variable {@link #classificationName}
     */
    public String getClassificationName()
    {
        return this.classificationName;
    }

    /**
     * @see org.efaps.admin.ui.field.Field#setProperty(java.lang.String, java.lang.String)
     * @param _name     name of the property
     * @param _value    value for the property
     * @throws CacheReloadException on error
     */
    @Override
    protected void setProperty(final String _name, final String _value)
            throws CacheReloadException
    {
        if ("Classification".equals(_name)) {
            this.classificationName = _value;
        } else {
            super.setProperty(_name, _value);
        }
    }
}

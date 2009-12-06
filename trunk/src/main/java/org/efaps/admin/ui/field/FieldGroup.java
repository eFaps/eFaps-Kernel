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

package org.efaps.admin.ui.field;

import org.efaps.util.cache.CacheReloadException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldGroup
    extends Field
{
    /**
     * Group Count if in a row / columnd must be shown more than one value. The
     * default value is <code>-1</code> meaning no group count is set.
     *
     * @see #setGroupCount
     * @see #getGroupCount
     */
    private int groupCount = -1;

    /**
     *
     * @param _id       id of the field group
     * @param _uuid     UUID of the field group
     * @param _name     name of the field group
     */
    public FieldGroup(final long _id,
                      final String _uuid,
                      final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * This is the setter method for the instance variable {@link #groupCount}.
     *
     * @return value of instance variable {@link #groupCount}
     * @see #groupCount
     * @see #setGroupCount
     */
    public int getGroupCount()
    {
        return this.groupCount;
    }

    /**
     * Sets the property for this field group. This includes
     * <ul>
     * <li>{@link #groupCount}</li>
     * </ul>
     *
     * @param _name     name / key of the property
     * @param _value    value of the property
     * @throws CacheReloadException from called super property method
     */
    @Override()
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("GroupCount".equals(_name)) {
            this.groupCount = Integer.parseInt(_value);
        } else {
            super.setProperty(_name, _value);
        }
    }
}

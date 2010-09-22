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

import java.util.ArrayList;
import java.util.List;

import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldSet
    extends Field
{
    /**
     * Order of this FieldSet.
     */
    private final List<String> order = new ArrayList<String>();

    /**
     * @param _id       id of the field set
     * @param _uuid     UUID of the field set
     * @param _name     name of the field set
     */
    public FieldSet(final long _id,
                    final String _uuid,
                    final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Sets the property for this field set. This includes
     * <ul>
     * <li>{@link #order}</li>
     * </ul>
     *
     * @param _name     name / key of the property
     * @param _value    value of the property
     * @throws CacheReloadException from called super property method
     */
    @Override
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("Order".equals(_name)) {
            final String[] values = _value.split("\\|");
            for (final String value : values) {
                this.order.add(value);
            }
        } else {
            super.setProperty(_name, _value);
        }
    }

    /**
     * Getter method for the instance variable {@link #order}.
     *
     * @return value of instance variable {@link #order}
     */
    public List<String> getOrder()
    {
        return this.order;
    }
}

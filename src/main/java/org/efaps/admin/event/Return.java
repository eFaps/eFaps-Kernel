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

package org.efaps.admin.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Class witch is used for getting the Return of the Events.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Return
{
    /**
     * Enumeration for the kind of returned values.
     */
    public enum ReturnValues
    {
        /** Used to return the instance after creating a new one.*/
        INSTANCE,
        /** Used to return code sniplett that will be represented as is. */
        SNIPLETT,
        /** Used to return a Map of Values. */
        VALUES,
        /** Used to return <i>true</i>. */
        TRUE;
    }

    /**
     * Map with all returned values.
     */
    private final Map<Return.ReturnValues, Object> map = new HashMap<Return.ReturnValues, Object>();

    /**
     *
     * @param _key  searched key
     * @return found object or <code>null</code> if not found
     */
    public Object get(final Return.ReturnValues _key)
    {
        return this.map.get(_key);
    }

    /**
     *
     * @param _key      key to set
     * @param _value    value to set
     */
    public void put(final Return.ReturnValues _key,
                    final Object _value)
    {
        this.map.put(_key, _value);
    }

    /**
     *
     * @return set of all values depending on the {@link ReturnValues}
     */
    public Set<Map.Entry<Return.ReturnValues, Object>> entrySet()
    {
        return this.map.entrySet();
    }

    /**
     *
     * @return <i>true</i> if the {@link #map} of returned value is empty;
     *         otherwise <i>false</i>
     * @see #map
     */
    public boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    /**
     *
     * @param _key  searched key
     * @return <i>true</i> if the <code>_key</code> is defined in the
     *         {@link #map}; otherwise <i>false</i>
     * @see #map
     */
    public boolean contains(final Return.ReturnValues _key)
    {
        return this.map.containsKey(_key);
    }

    /**
     * Returns a string representation of this parameter instance.
     *
     * @return string representation of this parameter instance.
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("map", this.map.toString())
            .toString();
    }
}

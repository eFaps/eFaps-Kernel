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

package org.efaps.update;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.db.Instance;

/**
 * TODO description!
 *
 * @author The eFaps Team
 *
 */
public class LinkInstance
{
    /**
     * Mapping for the values.
     */
    private final Map<String, String> valuesMap = new HashMap<String, String>();

    /**
     * Map of Names of a attribute to the values of this attribute.
     */
    private final Map<String, String> keyAttr2Value = new HashMap<String, String>();

    /**
     * Instance of the object the link links to.
     */
    private Instance childInstance;

    /**
     * Instance of the object the link links to.
     */
    private Instance instance;

    /**
     * Constructor not setting nothing. Careful the keyAttrputs must be set!
     */
    public LinkInstance()
    {
    }

    /**
     * Constructor setting the only keyfield to "Name" and the value of this
     * keyfield to the given parameter.
     * @param _name key for the keyfield "Name"
     */
    public LinkInstance(final String _name)
    {
        this.keyAttr2Value.put("Name", _name);
    }

    /**
     * Constructor setting the only keyfield to "Name" and the value of this
     * keyfield to the given parameter.
     * @param _name key for the keyfield "Name"
     * @param _values valuue
     */
    public LinkInstance(final String _name,
                        final String... _values)
    {
        this.keyAttr2Value.put("Name", _name);
        for (int i = 0; i < _values.length; i += 2) {
            this.valuesMap.put(_values[i], _values[i + 1]);
        }
    }

    /**
     * Getter method for instance variable {@link #keyAttr2Value}.
     *
     * @return value of instance variable {@link #keyAttr2Value}
     */
    public Map<String, String> getKeyAttr2Value()
    {
        return this.keyAttr2Value;
    }

    /**
     * This is the getter method for the instance variable {@link #valuesMap}.
     *
     * @return value of instance variable {@link #valuesMap}
     */
    public Map<String, String> getValuesMap()
    {
        return this.valuesMap;
    }

    /**
     * @param _values values to set
     */
    public void setValues(final Map<String, String> _values)
    {
        this.valuesMap.clear();
        this.valuesMap.putAll(_values);
    }

    /**
     * Setter method for the instance variable {@link #childInstance}.
     *
     * @param _instance Instance to set
     */
    public void setChildInstance(final Instance _instance)
    {
        this.childInstance = _instance;
    }

    /**
     * Getter method for the instance variable {@link #childInstance}.
     *
     * @return value of instance variable {@link #childInstance}
     */
    public Instance getChildInstance()
    {
        return this.childInstance;
    }

    /**
     * Getter method for the instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Setter method for instance variable {@link #instance}.
     *
     * @param _instance value for instance variable {@link #instance}
     */
    public void setInstance(final Instance _instance)
    {
        this.instance = _instance;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}

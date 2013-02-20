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
import org.efaps.db.Instance;

/**
 * Class witch is used for parsing Parameters to the Events.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Parameter
{

    /**
     * This enum holds the definitions of Parameters, to be accessed.
     */
    public enum ParameterValues
    {
        /**
         * Holds an AccessType, used for AccessCheck-Programs.
         */
        ACCESSTYPE,
        /**
         * Holds the mode of the access for the ui.
         */
        ACCESSMODE,
        /**
         * Call instance, means
         * <ul>
         * <li>for a web table, the instance for which the table values are
         * evaluated</li>
         * <li>for a web form, on which the web form is executed (if exists);
         * e.g. in edit mode it is the instance of the called object</li>
         * <li>for a command the instance on which the command was executed</li>
         * </ul>
         */
        CALL_INSTANCE,
        /**
         * The cmd that initiated the call. (e.g. The cmd that opened the form)
         */
        CALL_CMD,
        /**
         * Contains the class that called the esjp.
         */
        CLASS,
        /**
         * Contains a list of classifcations.
         */
        CLASSIFICATIONS,
        /**
         * Holds an Instance.
         * */
        INSTANCE,
        /**
         * Holds the new Values for an Instance, used e.g. by Creation of a new
         * Object
         */
        NEW_VALUES,
        /**
         * Holds an Map used to obfuscate the oids for presentation in the UserInterface.
         */
        OIDMAP4UI,
        /**
         * Further Parameters as map (key is string, value is string array),
         * e.g. from called form, command etc.
         */
        PARAMETERS,
        /**
         * Holds the Properties of the trigger.
         */
        PROPERTIES,
        /**
         * Place mark for additional Informations.
         */
        OTHERS,
        /**
         * Instances that where retrieved in the same request as the instance.
         */
        REQUEST_INSTANCES,
        /**
         * Holds the UserInterfaceObject on which the event is called.
         */
        UIOBJECT;
    }

    /**
     * Map used as the store for this Parameter.
     */
    private final Map<Parameter.ParameterValues, Object> map = new HashMap<Parameter.ParameterValues, Object>();

    /**
     * Put an object into the underlying map.
     *
     * @param _key key to the object
     * @param _value object
     */
    public void put(final ParameterValues _key, final Object _value)
    {
        this.map.put(_key, _value);
    }

    /**
     * Method to get an object from the underlying map.
     *
     * @param _key key to the object
     * @return object from the underlying map
     */
    public Object get(final ParameterValues _key)
    {
        return this.map.get(_key);
    }

    /**
     * Returns the value of map with the key
     * {@link ParameterValues#CALL_INSTANCE}.
     *
     * @return call instance of this parameter; or if not defined
     *         <code>null</code>
     */
    public Instance getCallInstance()
    {
        return (Instance) this.map.get(Parameter.ParameterValues.CALL_INSTANCE);
    }

    /**
     * Returns the value of map with the key {@link ParameterValues#INSTANCE}.
     *
     * @return instance of this parameter; or if not defined <code>null</code>
     */
    public Instance getInstance()
    {
        return (Instance) this.map.get(Parameter.ParameterValues.INSTANCE);
    }

    /**
     * Returns the value of map with the key {@link ParameterValues#PARAMETERS}.
     *
     * @return further parameters of this parameter; or if not defined
     *         <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameters()
    {
        return (Map<String, String[]>) this.map.get(Parameter.ParameterValues.PARAMETERS);
    }

    /**
     * Evaluates with given key in the list of all parameters for given key and
     * returns them (if found) as string array. If not found a <code>null</code>
     * is returned.
     *
     * @param _key name of parameter values which should returned
     * @return array of parameter values for given key of <code>null</code> if
     *         not exists
     * @see #getParameters to get the map of parameters
     */
    public String[] getParameterValues(final String _key)
    {
        final Map<String, String[]> params = getParameters();
        return (params != null) ? params.get(_key) : null;
    }

    /**
     * Evaluates with given key in the list of all parameters for given key and
     * returns them (if found) as string with index 0 in the string array of the
     * parameter values. If not found a <code>null</code> is returned.
     *
     * @param _key name of parameter value which should returned
     * @return value for given key or <code>null</code> if not exists
     * @see #getParameterValues to get the string array for given key
     */
    public String getParameterValue(final String _key)
    {
        final String[] paramValues = getParameterValues(_key);
        return ((paramValues != null) && (paramValues.length > 0)) ? paramValues[0] : null;
    }

    /**
     * Method to get the entry set of the underlying map.
     *
     * @return entry set
     */
    public Set<?> entrySet()
    {
        return this.map.entrySet();
    }

    /**
     * Returns a string representation of this parameter instance.
     *
     * @return string representation of this parameter instance.
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("map", this.map.toString()).toString();
    }
}

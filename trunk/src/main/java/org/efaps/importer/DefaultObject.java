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

package org.efaps.importer;

import java.util.HashMap;
import java.util.Map;

/**
 * This Class represents the Possibility to define default Values, for the Case
 * a Foreign-Object returns a invalid Value. <br>
 * <br>
 * Example for the XML-Structure:<br/> &lt;definition&gt;<br/> &lt;default
 * type="Admin_User_Person" name="Creator"&gt;1&lt;/default&gt;<br/>
 * &lt;/definition&gt; <br>
 * <br>
 * The Value can also be d by a ForeignObject.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DefaultObject
{
    /**
     * Contains the defaults defined for this import.
     */
    private static final Map<String, DefaultObject> DEFAULTS = new HashMap<String, DefaultObject>();

    /**
     * Contains the link to the {@link ForeignObject foreign object} of this
     * default object.
     */
    private ForeignObject link;

    /**
     * Contains the value of the default.
     */
    private String value = null;

    /**
     * Defines the {@value} and adds a new default to the {@link #DEFAULTS}.
     *
     * @param _type     string containing the Type of the Object
     * @param _name     string containing the Name of the Attribute
     * @param _value    value to be Set if the Default will be inserted
     */
    public void addDefault(final String _type,
                           final String _name,
                           final String _value)
    {
        this.value = _value;
        DefaultObject.DEFAULTS.put(_type + "/" + _name, this);
    }

    /**
     * Returns the default value of the object.
     *
     * @param _type     type of the Object
     * @param _name     name of the Attribute
     * @return string containing the default value, <code>null</code> if not
     *         defined
     */
    public static String getDefault(final String _type,
                                    final String _name)
    {
        final DefaultObject def = DefaultObject.DEFAULTS.get(_type + "/" + _name);
        String ret = null;
        if (def != null) {
            ret = def.value;
            if (ret.equals("")) {
                ret = def.link.dbGetValue();
                def.value = ret;
            }
        }
        return ret;
    }

    /**
     * Adds a foreign object to this default object.
     *
     * @param _link     foreign object to add
     */
    public void addLink(final ForeignObject _link)
    {
        this.link = _link;
    }
}

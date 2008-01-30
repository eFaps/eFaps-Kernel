/*
 * Copyright 2003 - 2008 The eFaps Team
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
 * Thsi Class represents the Possibility to define default Values, for the Case
 * a Foreign-Object returns a invalid Value. <br>
 * <br>
 * Example for the XML-Structure:<br/> &lt;definition&gt;<br/> &lt;default
 * type="Admin_User_Person" name="Creator"&gt;1&lt;/default&gt;<br/>
 * &lt;/definition&gt; <br>
 * <br>
 * The Value can also be retrived by a ForeignObject.
 *
 * @author jmox
 * @version $Id$
 */
public class DefaultObject {
  /**
   * contains the Defaults defined for this Import
   */
  final static Map<String, DefaultObject> DEFAULTS = new HashMap<String, DefaultObject>();

  /**
   * contains the {@link ForeignObjects} of this InsertObject
   */
  private ForeignObject                   link;

  /**
   * contains the Value of the Default
   */
  private String                          value    = null;

  /**
   * adds a new Dafult to the DEFAULTS
   *
   * @param _type
   *          String containing the Type of the Object
   * @param _name
   *          String containing the Name of the Attribute
   * @param _value
   *          Value to be Set if the Default will be inserte
   */
  public void addDefault(final String _type, final String _name,
                         final String _value) {
    this.value = _value;
    DEFAULTS.put(_type + "/" + _name, this);

  }

  /**
   * get the DefaultValue of the Object
   *
   * @param _type
   *          String containing the Type of the Object
   * @param _name
   *          String containing the Name of the Attribute
   * @return String containing the DefaultValue, null if not defined
   */
  public static String getDefault(final String _type, final String _name) {
    final DefaultObject def = DEFAULTS.get(_type + "/" + _name);
    if (def == null) {
      return null;
    }
    String ret = def.value;
    if (ret.equals("")) {
      ret = def.link.dbGetID();
      def.value = ret;
    }

    return ret;
  }

  /**
   * Adds a ForeignObject to this DefaultObject
   *
   * @param _Object
   *          ForeignObject to add
   */
  public void addLink(ForeignObject _Object) {
    this.link = (_Object);

  }

}

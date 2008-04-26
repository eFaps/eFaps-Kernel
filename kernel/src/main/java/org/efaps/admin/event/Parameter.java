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

package org.efaps.admin.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class witch is used for parsing Parameters to the Events.
 *
 * @author jmo
 * @version $Id$
 *
 */
public class Parameter  {
  /**
   * This enum holds the definitions of Parameters, to be accessed
   */
  public enum ParameterValues {
    /** Holds an AccessType, used for AccessCheck-Programs */
    ACCESSTYPE,
    /**
     * Call instance, means
     * <ul>
     * <li>for a web table, the instance for which the table values are
     *     evaluated</li>
     * <li>for a web form, on which the web form is executed (if exists);
     *     e.g. in edit mode it is the instance of the called object</li>
     * </ul>
     */
    CALL_INSTANCE,
    /** Holds an Instance */
    INSTANCE,
    /**
     * Holds the new Values for an Instance, used e.g. by Creation of a new
     * Object
     */
    NEW_VALUES,
    /** Holds the Properties of the trigger */
    PROPERTIES,
    /** Placemark for aditional Informations */
    OTHERS,
    /** Holds an UserInterfaceObject */
    UIOBJECT;

  }
  private final Map<ParameterValues, Object> map = new HashMap<ParameterValues, Object>();

  public void put(ParameterValues _key, Object _value) {
    this.map.put(_key, _value);

  }

  public Object get(ParameterValues _key) {
    return this.map.get(_key);
  }

  public Set<?> entrySet() {
    return this.map.entrySet();
  }

  /**
   * Returns a string representation of this parameter instance.
   *
   * @return string representation of this parameter instance.
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("map", this.map.toString())
        .toString();
  }
}

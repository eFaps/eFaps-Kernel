/*
 * Copyright 2003 - 2007 The eFaps Team
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

import java.util.Set;

/**
 * This Interface is the Interface for the Paramters to be used with the
 * <code>EventExecution</code> on executing. It is bassically just a Map
 * Interface, but it provides the possibility to define how the Map is
 * constructed.
 * 
 * @author jmo
 * @version $Id$
 * @param <K>
 * @param <V>
 */
public interface ParameterInterface<K, V> {

  /**
   * This enum holds the Defenitions of Parameters, to be accessed
   */
  public enum ParameterValues {
    /** Holds an AccessType, used for AccessCheck-Programs */
    ACCESSTYPE,
    /** Holds an Instance */
    INSTANCE,
    /**
     * Holds the new Values for an Instance, used e.g. by Creation of a new
     * Object
     */
    NEW_VALUES,
    /** Holds the Properties of the trigger */
    PROPERTIES;
  }

  public void put(ParameterValues _key, Object _value);

  public Object get(ParameterValues _key);

  public Set entrySet();
}

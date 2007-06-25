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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class witch is used for getting the Return of the Events.
 * 
 * @author jmo
 * @version $Id$
 * 
 */
public class Return  {
  
  public enum ReturnValues {
    /** used to return a Map of Values */
    VALUES,
    /** used to return true */
    TRUE;
  }
  
  
  private Map<ReturnValues, Object> map = new HashMap<ReturnValues, Object>();

  public Object get(ReturnValues _key) {
    return this.map.get(_key);
  }

  public void put(ReturnValues _key, Object _value) {
    this.map.put(_key, _value);

  }

  public Set<Map.Entry<ReturnValues, Object>> entrySet() {
    return this.map.entrySet();

  }

  /**
   * Returns a string representation of this parameter instance.
   * 
   * @return string representation of this parameter instance.
   */
  public String toString() {
    return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("map", this.map.toString())
        .toString();
  }
}

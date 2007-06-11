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

import java.util.Map;
import java.util.Set;

/**
 * This Interface is the Interface for the Returned Values to be used with the
 * <code>EventExecution</code> on executing. It is bassically just a Map
 * Inertface, but it provides the possibility to define how the Map is
 * constructed.
 * 
 * @author jmo
 * @version $Id$
 * 
 * @param <K>
 * @param <V>
 */
public interface ReturnInterface<K, V> {

  public enum ReturnValues {
    VALUES
  }

  public void put(ReturnValues _key, Object _value);

  public Object get(ReturnValues _key);

  public Set<Map.Entry<ReturnValues, Object>> entrySet();
}

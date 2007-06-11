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

public class Return implements ReturnInterface {
  private Map<ReturnValues, Object> map = new HashMap<ReturnValues, Object>();

  public Object get(ReturnValues _key) {
    return this.map.get(_key);
  }

  public void put(ReturnValues _key, Object _value) {
    this.map.put(_key, _value);

  }

}

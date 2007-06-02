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

package org.efaps.webapp.programs;

import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.TriggerKeys4Values;
import org.efaps.admin.runlevel.RunLevel;

public class ReloadCache implements EventExecution {

  public void execute(Map<TriggerKeys4Values, Object> _map) {
    try {
      RunLevel.init("webapp");
      RunLevel.execute();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}

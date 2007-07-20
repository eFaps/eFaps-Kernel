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
 * Revision:        $Rev:851 $
 * Last Changed:    $Date:2007-06-02 12:36:03 -0500 (Sat, 02 Jun 2007) $
 * Last Changed By: $Author:jmo $
 */

package org.efaps.esjp.common.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.runlevel.RunLevel;

/**
 * Class to relaod the Cache.<br>
 * This Class is a Java eFaps Program wich is stored inside the eFaps-Database.
 * It is executed on Userinteraction through a trigger on a Command.
 * 
 * @author jmo
 * @version $Id$
 */
public class ReloadCache implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(ReloadCache.class);

  public Return execute(Parameter _parameter) {
    try {
      RunLevel.init("webapp");
      RunLevel.execute();
    } catch (Exception e) {
      LOG.error("execute(ParameterInterface)", e);
    }
    return null;
  }

}

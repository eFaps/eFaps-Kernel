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

package org.efaps.esjp.common.uitable;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.Command;
import org.efaps.db.Delete;
import org.efaps.util.EFapsException;

public class CommonDelete implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(CommonDelete.class);

  public Return execute(Parameter _parameter) {
    String[] oids = (String[]) _parameter.get(ParameterValues.OTHERS);
    Command command = (Command) _parameter.get(ParameterValues.UIOBJECT);

    if (oids != null) {
      for (int i = 0; i < oids.length; i++) {
        String oid = oids[i];
        if (command.getDeleteIndex() > 0) {
          StringTokenizer tokens = new StringTokenizer(oid, "|");
          int count = 0;
          while (tokens.hasMoreTokens() && count < command.getDeleteIndex()) {
            oid = tokens.nextToken();
            count++;

          }
        }
        Delete del = new Delete(oid);
        try {
          del.execute();
        } catch (EFapsException e) {
          LOG.error("execute(Parameter)", e);
        }

      }

    }
    return null;

  }
}

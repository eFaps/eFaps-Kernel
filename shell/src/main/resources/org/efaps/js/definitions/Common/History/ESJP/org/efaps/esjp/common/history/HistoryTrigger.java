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

package org.efaps.esjp.common.history;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

public class HistoryTrigger implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(HistoryTrigger.class);

  public Return execute(Parameter _parameter) {
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    Map values = (Map) _parameter.get(ParameterValues.NEW_VALUES);
    Map properties = (Map) _parameter.get(ParameterValues.PROPERTIES);

    try {
      Insert insert = new Insert((String) properties.get("Type"));
      insert.add("ForID", ((Long) instance.getId()).toString());
      insert.add("ForType", ((Long) instance.getType().getId()).toString());
      insert.execute();
      String ID = insert.getId();
      insert.close();

      if (values != null) {
        Iterator iter = values.entrySet().iterator();

        while (iter.hasNext()) {
          Map.Entry entry = (Map.Entry) iter.next();
          Attribute attr = (Attribute) entry.getKey();
          String value = (String) entry.getValue().toString();
          
          insert = new Insert("Common_HistoryAttributes");
          insert.add("HistoryID", ID);
          insert.add("Attribute", ((Long) attr.getId()).toString());
          insert.add("Value", value);
          insert.execute();
          insert.close();

        }

      }
    } catch (EFapsException e) {
      LOG.error("execute(Parameter)", e);
    } catch (Exception e) {
      LOG.error("execute(Parameter)", e);
    }

    return null;
  }
}

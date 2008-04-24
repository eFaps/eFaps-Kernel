/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.esjp.common.uisearch;

import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
@EFapsUUID("9993f975-855d-4d19-865a-c509bf410149")
public class Connect implements EventExecution
{

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException
  {
    Return ret = new Return();

    Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
    Instance parent = (Instance) _parameter.get(ParameterValues.INSTANCE);
    String childOids[] = (String[]) _parameter.get(ParameterValues.OTHERS);

    String type = (String) properties.get("ConnectType");
    String childAttr = (String) properties.get("ConnectChildAttribute");
    String parentAttr = (String) properties.get("ConnectParentAttribute");

    /*
     * if (childAttr == null) { throw new Exception("Could not found child
     * attribute '" + command.getConnectChildAttribute() + "' for type '" +
     * type.getName() + "'"); } if (parentAttr == null) { throw new
     * Exception("Could not found parent attribute '" +
     * command.getConnectParentAttribute() + "' for type '" + type.getName() +
     * "'"); }
     */
    for (String childOid : childOids) {
      Instance child = new Instance(childOid);
      Insert insert = new Insert(type);
      insert.add(parentAttr, "" + parent.getId());
      insert.add(childAttr, "" + child.getId());
      insert.execute();
    }

    return ret;
  }
}

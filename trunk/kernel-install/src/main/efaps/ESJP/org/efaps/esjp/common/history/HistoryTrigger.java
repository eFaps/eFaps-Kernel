/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 * @todo description
 */
@EFapsUUID("11b5668b-de34-4cb5-985c-b3f10686e72c")
@EFapsRevision("$Rev$")
public class HistoryTrigger implements EventExecution
{
  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException
  {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);
    final Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
    final String type = (String) properties.get("Type");

    Insert insert = new Insert(type);
    insert.add("ForID", ((Long) instance.getId()).toString());
    insert.add("ForType", ((Long) instance.getType().getId()).toString());
    if ("Common_History_AddChild".equals(type)
        || "Common_History_RemoveChild".equals(type)) {
      final Context context = Context.getThreadContext();
      final String oid = context.getParameter("oid");
      final String typeid = oid.substring(0, oid.indexOf("."));
      final String toid = oid.substring(oid.indexOf(".") + 1);
      insert.add("ToType", typeid);
      insert.add("ToID", toid);
    }

    insert.execute();
    final String ID = insert.getId();

    insert.close();

    if (values != null) {
      final Iterator<?> iter = values.entrySet().iterator();

      while (iter.hasNext()) {
        final Entry<?, ?> entry = (Entry<?, ?>) iter.next();
        final Attribute attr = (Attribute) entry.getKey();
        final String value = entry.getValue().toString();

        insert = new Insert("Common_History_Attributes");
        insert.add("HistoryID", ID);
        insert.add("Attribute", ((Long) attr.getId()).toString());
        insert.add("Value", value);
        insert.execute();
        insert.close();
      }
    }

    return null;
  }
}

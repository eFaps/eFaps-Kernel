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

package org.efaps.teamwork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.TriggerKeys4Values;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

public class Member implements EventExecution {

  public void execute(Context _context, Instance _instance,
                      Map<TriggerKeys4Values, Map> _map) {

    // nur wenn ein neuer Root, Folder, Dokument angelegt wurde
    if (_instance.getId() != 0) {
      String abstractlink = ((Long) _instance.getId()).toString();
      try {
        Insert insert = new Insert("TeamWork_Member");
        insert.add("AbstractLink", abstractlink);
        insert.add("AccessSetLink", "1");
        insert.add("UserAbstractLink", ((Long) _context.getPerson().getId())
            .toString());
        insert.executeWithoutAccessCheck();

      } catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      Iterator iter = _map.get(TriggerKeys4Values.NEW_VALUES).entrySet()
          .iterator();
      Map<String, String> newValues = new HashMap<String, String>();

      while (iter.hasNext()) {
        Map.Entry entry = (Map.Entry) iter.next();
        Attribute attr = (Attribute) entry.getKey();
        String attrName = attr.getName();
        String value = (String) entry.getValue().toString();
        newValues.put(attrName, value);
      }

      SearchQuery query = new SearchQuery();
      Update update;
      try {
        query.setQueryTypes("TeamWork_Member");
        query.addWhereExprEqValue("AccessSetLink", newValues
            .get("AccessSetLink"));
        query.addWhereExprEqValue("UserAbstractLink", newValues
            .get("UserAbstractLink"));
        query
            .addWhereExprEqValue("AbstractLink", newValues.get("AbstractLink"));
        query.addSelect("OID");
        query.executeWithoutAccessCheck();

        if (query.next()) {
          update = new Update(query.get("OID").toString());
        } else {
          update = new Insert("TeamWork_Member");

        }
        update.add("AccessSetLink", newValues.get("AccessSetLink"));
        update.add("AbstractLink", newValues.get("AbstractLink"));
        update.add("UserAbstractLink", newValues.get("UserAbstractLink"));
        update.executeWithoutAccessCheck();
      } catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
  }
}

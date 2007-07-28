/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.esjp.admin.datamodel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LinkedMap;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * This Class gets a Range from the Database.<br>
 * The Class makes a query against the Database, with the Type from the
 * Properties of the Parameters and returns a map sorted by the values. The key
 * returned is the ID, the Value returned can be specified by the Propertie
 * "Value".
 * 
 * @author jmo
 * @version $Id$
 */
public class RangesValue implements EventExecution {

  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();

    String type =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("Type");

    String value =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("Value");
    SearchQuery query = new SearchQuery();

    Map<String, String> map = new HashMap<String, String>();

    query.setQueryTypes(type);
    query.addSelect("ID");
    query.addSelect(value);
    query.execute();

    while (query.next()) {
      map.put(query.get("ID").toString(), query.get(value).toString());
    }
    // sort by Value
    List<Map.Entry<String, String>> list =
        new Vector<Map.Entry<String, String>>(map.entrySet());

    java.util.Collections.sort(list,
        new Comparator<Map.Entry<String, String>>() {
          public int compare(Map.Entry<String, String> entry,
              Map.Entry<String, String> entry1) {
            String r = entry.getValue().toString();
            String r1 = entry1.getValue().toString();

            return r.compareTo(r1);
          }

        });

    AbstractLinkedMap map2 = new LinkedMap();

    for (Map.Entry<String, String> entry : list) {
      map2.put(entry.getKey(), entry.getValue());
    }

    ret.put(ReturnValues.VALUES, map2);

    return ret;
  }
}

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

package org.efaps.esjp.teamwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class StructurBrowser implements EventExecution {

  public Return execute(Parameter _parameter) throws EFapsException {
    Return ret = new Return();
    Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

    String types = (String) properties.get("Types");

    boolean expandChildTypes =
        "true".equals(properties.get("ExpandChildTypes"));

    SearchQuery query = new SearchQuery();
    query.setQueryTypes(types);
    query.setExpandChildTypes(expandChildTypes);
    query.addSelect("OID");
    query.execute();

    List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      List<Instance> instances = new ArrayList<Instance>(1);
      instances.add(new Instance((String) query.get("OID")));
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }

}

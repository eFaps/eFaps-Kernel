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

package org.efaps.esjp.common.history;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 * @todo description
 */
public class HistoryQuery implements EventExecution {

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();

    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);

    SearchQuery query = new SearchQuery();
    query.setQueryTypes("Common_History_Update");
    query.setExpandChildTypes(true);
    query.addSelect("OID");
    query.addWhereExprEqValue("ForID", instance.getId());
    query.addWhereExprEqValue("ForType", instance.getType().getId());
    query.execute();

    List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      System.out.println((String) query.get("OID"));
      List<Instance> instances = new ArrayList<Instance>(1);
      instances.add(new Instance((String) query.get("OID")));
      list.add(instances);
    }

    query.close();
    query = new SearchQuery();
    query.setQueryTypes("Common_History_Reference");
    query.setExpandChildTypes(true);
    query.addSelect("OID");
    query.addWhereExprEqValue("ToType", instance.getType().getId());
    query.addWhereExprEqValue("ToID", instance.getId());
    query.execute();
    while (query.next()) {
      System.out.println((String) query.get("OID"));
      List<Instance> instances = new ArrayList<Instance>(1);
      instances.add(new Instance((String) query.get("OID")));
      list.add(instances);
    }
    query.close();
    ret.put(ReturnValues.VALUES, list);

    return ret;
  }
}

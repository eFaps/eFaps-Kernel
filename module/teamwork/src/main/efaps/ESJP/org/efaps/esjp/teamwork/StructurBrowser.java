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

package org.efaps.esjp.teamwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("42f5f290-0575-479b-b6fa-29ed1771e0cd")
@EFapsRevision("$Rev$")
public class StructurBrowser implements EventExecution {

  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = null;
    final String method = (String) _parameter.get(ParameterValues.OTHERS);
    if (method.equals("execute")) {
      ret = internalExecute(_parameter);
    } else if (method.equals("checkForChildren")) {
      ret = checkForChildren(_parameter);
    } else if (method.equals("addChildren")) {
      ret = addChildren(_parameter);
    }
    return ret;
  }

  private Return internalExecute(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

    final String types = (String) properties.get("Types");

    final boolean expandChildTypes =
        "true".equals(properties.get("ExpandChildTypes"));

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(types);
    query.setExpandChildTypes(expandChildTypes);
    query.addSelect("OID");
    query.execute();

    final List<List<Object[]>> list = new ArrayList<List<Object[]>>();
    while (query.next()) {
      final List<Object[]> instances = new ArrayList<Object[]>(1);
      instances.add(new Object[] { new Instance((String) query.get("OID")),
          null });
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);
    return ret;

  }

  private Return checkForChildren(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

    final String expand = (String) properties.get("checkForChildren");

    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final SearchQuery query = new SearchQuery();

    query.setExpand(instance, expand);
    query.execute();
    if (query.next()) {
      ret.put(ReturnValues.TRUE, true);
    }

    return ret;
  }

  private Return addChildren(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();

    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);

    final SearchQuery query = new SearchQuery();

    query.setQueryTypes("TeamWork_Abstract");
    query.setExpandChildTypes(true);
    query.addSelect("OID");
    query.addWhereExprEqValue("ParentCollectionLink", this.getId(instance));
    query.execute();

    final List<List<Object[]>> lists = new ArrayList<List<Object[]>>();

    while (query.next()) {
      final List<Object[]> instances = new ArrayList<Object[]>(1);
      instances.add(new Object[] { new Instance((String) query.get("OID")),
          true });
      lists.add(instances);
    }
    ret.put(ReturnValues.VALUES, lists);
    return ret;

  }

  private String getId(final Instance _instance) {
    final String ret = _instance.getOid();
    return ret.substring(ret.indexOf(".") + 1);
  }
}

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

package org.efaps.esjp.common.uisearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class QuerySearch implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(QuerySearch.class);

  public Return execute(final Parameter _parameter) {
    Return ret = new Return();
    try {
Context context = Context.getThreadContext();
      CommandAbstract command = (CommandAbstract) _parameter.get(ParameterValues.UIOBJECT);
      Map properties = (Map) _parameter.get(ParameterValues.PROPERTIES);

      String types = (String) properties.get("Types");

      boolean expandChildTypes =
          "true".equals((String) properties.get("ExpandChildTypes"));

      if (LOG.isDebugEnabled()) {
        LOG.debug("types=" + types);
      }

      SearchQuery query = new SearchQuery();
      query.setQueryTypes(types);
      query.setExpandChildTypes(expandChildTypes);
      for (Field field : command.getTargetForm().getFields())  {
        String value = context.getParameter(field.getName());
        if ((value != null) && (value.length() > 0) && (!value.equals("*"))) {
          query.addWhereExprMatchValue(field.getExpression(), value);
        }
      }
      
      query.addSelect("OID");
      query.execute();

      List<List<Instance>> list = new ArrayList<List<Instance>>();
      while (query.next()) {
        List<Instance> instances = new ArrayList<Instance>(1);
        instances.add(new Instance((String) query.get("OID")));
        list.add(instances);
      }

      ret.put(ReturnValues.VALUES, list);
    } catch (EFapsException e) {
      LOG.error("execute(Parameter)", e);
    }

    return ret;
  }
}

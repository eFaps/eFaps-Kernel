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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.common.uisearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id:QuerySearch.java 1563 2007-10-28 14:07:41Z tmo $
 * @todo description
 */
public class QuerySearch implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(QuerySearch.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();

    Context context = Context.getThreadContext();
    AbstractCommand command =
        (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
    Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

    String types = (String) properties.get("Types");

    boolean expandChildTypes =
        "true".equals(properties.get("ExpandChildTypes"));

    if (LOG.isDebugEnabled()) {
      LOG.debug("types=" + types);
    }

    SearchQuery query = new SearchQuery();
    query.setQueryTypes(types);
    query.setExpandChildTypes(expandChildTypes);
    for (Field field : command.getTargetForm().getFields()) {
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

    return ret;
  }
}

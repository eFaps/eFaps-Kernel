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
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id:QuerySearch.java 1563 2007-10-28 14:07:41Z tmo $
 * @todo description
 */
@EFapsUUID("d6988849-9412-4f83-980f-a596750f6340")
@EFapsRevision("$Rev$")
public class QuerySearch implements EventExecution
{
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(QuerySearch.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException
  {
    final Return ret = new Return();

    final Context context = Context.getThreadContext();
    final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
    final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

    final String types = (String) properties.get("Types");

    final boolean expandChildTypes = "true".equals(properties.get("ExpandChildTypes"));

    if (LOG.isDebugEnabled()) {
      LOG.debug("types=" + types);
    }

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(types);
    query.setExpandChildTypes(expandChildTypes);
    for (final Field field : command.getTargetForm().getFields()) {
      final String value = context.getParameter(field.getName());
      if ((value != null) && (value.length() > 0) && (!value.equals("*"))) {
        query.addWhereExprMatchValue(field.getExpression(), value);
      }
    }

    query.addSelect("OID");
    query.execute();

    final List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      final List<Instance> instances = new ArrayList<Instance>(1);
      instances.add(Instance.get((String) query.get("OID")));
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }
}

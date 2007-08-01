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

package org.efaps.esjp.common.uitable;

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
import org.efaps.util.EFapsException;

/**
 * The ESJP is used to launch an expand-query against the eFaps-Database, wich
 * is afterwards used to fill a webtable.<br/> <b>Example:</b><br/> <code>
 *   &lt;target&gt;<br/>&nbsp;&nbsp;&lt;evaluate program="org.efaps.esjp.common.uitable.QueryExpand"&gt;
 * <br/>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="Expand"&gt;Admin_User_Person2Group\UserFromLink.UserToLink&lt;/property&gt;
 * <br/>&nbsp;&nbsp;&lt;/evaluate&gt;<br/> &lt;/target&gt;
 * </code><br/>
 * 
 * @author tmo
 * @version $Id$
 */
public class QueryExpand implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(QueryEvaluate.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);

    Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

    String expand = (String) properties.get("Expand");

    if (LOG.isDebugEnabled()) {
      LOG.debug("Expand=" + expand);
    }

    SearchQuery query = new SearchQuery();
    query.setExpand(instance, expand);
    query.execute();

    List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      list.add(query.getExpandInstances());
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }
}

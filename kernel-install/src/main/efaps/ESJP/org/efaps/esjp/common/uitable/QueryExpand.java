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

package org.efaps.esjp.common.uitable;

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
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * The ESJP is used to launch an expand-query against the eFaps-Database, wich
 * is afterwards used to fill a webtable<br>
 * <br>
 * <b>Properties:</b><br>
 * <table>
 * <tr>
 * <td><u>Name</u></td>
 * <td><u>Value</u></td>
 * <td><u>Default</u></td>
 * <td><u>mandatory</u></td>
 * <td><u>Description</u></td>
 * </b></tr>
 * <tr>
 * <td>Expand</td>
 * <td>-</td>
 * <td>-</td>
 * <td>yes</td>
 * <td>Expand to be executed</td>
 * </tr>
 * </table><br>
 * <b>Example:</b><br>
 * <code>
 * &lt;target&gt;<br>
 * &nbsp;&nbsp;&lt;evaluate program="org.efaps.esjp.common.uitable.QueryExpand"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="Expand"&gt;Admin_User_Person2Group\UserFromLink.UserToLink&lt;/property&gt;<br>
 * &nbsp;&nbsp;&lt;/evaluate&gt;<br>
 * &lt;/target&gt;
 * </code>
 * 
 * @author tmo
 * @version $Id:QueryExpand.java 1563 2007-10-28 14:07:41Z tmo $
 */
public class QueryExpand implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(QueryEvaluate.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);

    Map<?, ?> properties =
        (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

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

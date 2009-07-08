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
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * The ESJP is used to launch a query against the eFaps-Database, which is
 * afterwards used to fill a webtable.<br>
 * <br>
 * <b>Properties:</b><br>
 * <table>
 * <tr>
 * <td><u>Name</u></td>
 * <td><u>Value</u></td>
 * <td><u>Default</u></td>
 * <td><u>mandatory</u></td>
 * <td><u>Description</u></td>
 * <tr>
 * <td>Types</td>
 * <td>-</td>
 * <td>-</td>
 * <td>yes</td>
 * <td>Name of the Type to Search for</td>
 * </tr>
 * <tr>
 * <td>ExpandChildTypes</td>
 * <td>true</td>
 * <td>false</td>
 * <td>no</td>
 * <td>should the ChildTypes be expanded</td>
 * </tr>
 * </table>
 * <br>
 * <b>Example:</b><br>
 * <code>
 * &lt;target&gt;<br>
 * &nbsp;&nbsp;&lt;evaluate program="org.efaps.esjp.common.uitable.QueryEvaluate"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="Types"&gt;Admin_User_Person&lt;/property&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="ExpandChildTypes"&gt;true&lt;/property&gt;<br>
 * &nbsp;&nbsp;&lt;/evaluate&gt;<br>
 * &lt;/target&gt;</code>
 *
 * @author The eFaps Team
 * @version $Id:QueryEvaluate.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("bbe633db-b098-46d0-8dd8-84af34e7ff8c")
@EFapsRevision("$Rev$")
public class QueryEvaluate implements EventExecution
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(QueryEvaluate.class);

    /**
     * @param _parameter
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final String types = (String) properties.get("Types");

        final boolean expandChildTypes = "true".equals(properties.get("ExpandChildTypes"));

        if (QueryEvaluate.LOG.isDebugEnabled()) {
            QueryEvaluate.LOG.debug("types=" + types);
        }

        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(types);
        query.setExpandChildTypes(expandChildTypes);
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

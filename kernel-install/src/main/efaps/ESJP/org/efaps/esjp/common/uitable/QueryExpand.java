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
 * The ESJP is used to launch an expand-query against the eFaps-Database, which
 * is afterwards used to fill a webtable.<br>
 * <br>
 * <b>Properties:</b><br>
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Value</th>
 * <th>Default</th>
 * <th>mandatory</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Expand</td>
 * <td>String</td>
 * <td>-</td>
 * <td>yes</td>
 * <td>Expand to be executed</td>
 * </tr>
 * <tr>
 * <td>AlternateAttribute</td>
 * <td>String</td>
 * <td>-</td>
 * <td>no</td>
 * <td>alternate attribute the expand will be executed with also.</td>
 * </tr>
 * </table>
 * <br>
 * <b>Example:</b><br>
 * <code>
 * &lt;target&gt;<br>
 * &nbsp;&nbsp;&lt;evaluate program="org.efaps.esjp.common.uitable.QueryExpand"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="Expand"&gt;Admin_User_Person2Group\UserFromLink.UserToLink&lt;/property&gt;<br>
 * &nbsp;&nbsp;&lt;/evaluate&gt;<br>
 * &lt;/target&gt;
 * </code>
 * @deprecated use MulitPrint esjp
 * @author The eFaps Team
 * @version $Id:QueryExpand.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("41945470-bcc3-4d91-b16b-6357932ead5e")
@EFapsRevision("$Rev$")
@Deprecated
public class QueryExpand implements EventExecution
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(QueryEvaluate.class);

    /**
     * {@inheritDoc}
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);

        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final String expand = (String) properties.get("Expand");
        final String alternateAttribute = (String) properties.get("AlternateAttribute");

        if (QueryExpand.LOG.isDebugEnabled()) {
            QueryExpand.LOG.debug("Expand=" + expand);
        }

        final SearchQuery query = new SearchQuery();
        query.setExpand(instance, expand);
        query.execute();

        final List<List<Instance>> list = new ArrayList<List<Instance>>();
        while (query.next()) {
            list.add(query.getExpandInstances());
        }

        if (alternateAttribute != null) {
            final SearchQuery query2 = new SearchQuery();
            query2.setExpand(instance, expand.substring(0, expand.indexOf("\\") + 1) +  alternateAttribute);
            query2.execute();

            while (query2.next()) {
                list.add(query2.getExpandInstances());
            }
        }
        ret.put(ReturnValues.VALUES, list);

        return ret;
    }
}

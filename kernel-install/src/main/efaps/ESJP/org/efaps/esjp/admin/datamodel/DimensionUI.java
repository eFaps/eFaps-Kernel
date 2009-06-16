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

package org.efaps.esjp.admin.datamodel;

import java.util.Map;
import java.util.TreeMap;

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
 * ESJP is used to get the value, and to render the fields for the BaseUoM.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("c3d4e45f-cb46-4abf-9aa3-67703140b74b")
@EFapsRevision("$Rev$")
public class DimensionUI implements EventExecution
{

    /**
     * Method is called from within the form Admin_Datamodel_DimensionForm to
     * retrieve the value for the BaseUoM.
     *
     * @param _parameter Parameters as passed from eFaps
     * @return Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return retVal = new Return();
        final Instance instance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
        Long actual = new Long(0);
        final TreeMap<String, Long> map = new TreeMap<String, Long>();
        if (instance != null) {
            final SearchQuery query = new SearchQuery();
            query.setObject(instance);
            query.addSelect("BaseUoM");
            query.execute();
            if (query.next()) {
                actual = (Long) query.get("BaseUoM");
            }
            query.next();
            final SearchQuery query2 = new SearchQuery();
            query2.setExpand(instance, "Admin_DataModel_UoM\\Dimension");
            query2.addSelect("ID");
            query2.addSelect("Name");
            query2.execute();
            while (query2.next()) {
                map.put((String) query2.get("Name"), (Long) query2.get("ID"));
            }
            query2.close();
        }

        final StringBuilder ret = new StringBuilder();

        ret.append("<select size=\"1\" name=\"baseOuM4Edit\">");
        for (final Map.Entry<String, Long> entry : map.entrySet()) {
            ret.append("<option");

            if (entry.getValue().equals(actual)) {
                ret.append(" selected=\"selected\" ");
            }
            ret.append(" value=\"").append(entry.getValue()).append("\">").append(entry.getKey())
                .append("</option>");
        }

        ret.append("</select>");
        retVal.put(ReturnValues.SNIPLETT, ret.toString());

        return retVal;
    }
}

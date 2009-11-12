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

package org.efaps.esjp.admin.user;

import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class contains some method need to create a Person and to connect the person
 * to a role.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1b777261-6da1-4003-87e4-2937e44ff269")
@EFapsRevision("$Rev$")
public class Person
{

    /**
     * Method called to connect a Person to a Role.
     *
     * @param _parameter Parameter as past from eFaps to an esjp
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return connectUser2UserUI(final Parameter _parameter) throws EFapsException
    {

        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        final Instance parent = _parameter.getInstance();

        final String[] childOids = _parameter.getParameterValues("selectedRow");

        if (childOids != null) {
            final String type = (String) properties.get("ConnectType");
            final String childAttr = (String) properties.get("ConnectChildAttribute");
            final String parentAttr = (String) properties.get("ConnectParentAttribute");

            for (final String childOid : childOids) {
                final Instance child = Instance.get(childOid);
                final Insert insert = new Insert(type);
                insert.add(parentAttr, "" + parent.getId());
                insert.add(childAttr, "" + child.getId());
                insert.add("UserJAASSystem", "" + getJAASSystemID());
                insert.execute();
            }
        }
        return new Return();
    }

    /**
     * This method inserts the JAASSystem for a User into the eFaps-Database.<br>
     * It is executed on a INSERT_POST Trigger on the Type User_Person.
     *
     * @param _parameter Parameter as past from eFaps to en esjp
     * @return null
     * @throws EFapsException on error
     */
    public Return insertJaaskeyTrg(final Parameter _parameter) throws EFapsException
    {
        final Instance instance = _parameter.getInstance();

        final Map<?, ?> values = (Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);

        final String jaassystemid = getJAASSystemID();
        if (jaassystemid != null) {
            final Object[] key  = (Object[]) values.get(instance.getType().getAttribute("Name"));
            final Insert insert = new Insert("Admin_User_JAASKey");
            insert.add("Key", key[0]);
            insert.add("JAASSystemLink", getJAASSystemID());
            insert.add("UserLink", ((Long) instance.getId()).toString());
            insert.execute();
        }
        return null;
    }

    /**
     * Get the ID of the JAASSYstem for eFaps.
     *
     * @return ID of the JAASSYSTEM, NULL if not found
     * @throws EFapsException on error
     */
    private String getJAASSystemID() throws EFapsException
    {
        String objId = null;

        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_User_JAASSystem");
        query.addWhereExprEqValue("Name", "eFaps");
        query.addSelect("ID");
        query.execute();
        if (query.next()) {
            objId = query.get("ID").toString();
        }
        query.close();

        return objId;
    }
}

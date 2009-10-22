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

package org.efaps.esjp.db;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("0f824311-3747-4cfc-85ce-a5268a6ba9c9")
@EFapsRevision("$Rev$")
public class Store implements EventExecution
{

    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();
        final Insert insert = new Insert(Type.get(EFapsClassNames.DB_STORE));
        insert.add("Name", _parameter.getParameterValue("name"));
        insert.add("UUID", _parameter.getParameterValue("uuid"));
        insert.add("Revision", _parameter.getParameterValue("revision"));
        insert.execute();
        final Instance instance = insert.getInstance();

        final Insert resourceInsert = new Insert(Type.get(EFapsClassNames.DB_RESOURCE));
        resourceInsert.add("Name", _parameter.getParameterValue("resource4create"));
        resourceInsert.execute();
        final Instance resource = resourceInsert.getInstance();

        final Insert connect = new Insert(Type.get(EFapsClassNames.DB_STORE2RESOURCE));
        connect.add("From", ((Long) instance.getId()).toString());
        connect.add("To", ((Long) resource.getId()).toString());
        connect.execute();

        return ret;
    }

    /**
     * This method is called first to render simple inputfields.
     *
     * @param _parameter Parameter as passed from eFaps to esjp
     * @return Return
     */
    public Return getResourceFieldValueUI(final Parameter _parameter)
    {
        final StringBuilder ret = new StringBuilder();
        final FieldValue fieldvalue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final TargetMode mode = fieldvalue.getTargetMode();

        final Return retVal = new Return();

        if (mode.equals(TargetMode.CREATE)) {
            ret.append("<input name=\"").append(fieldvalue.getField().getName()).append("\" type=\"text\" ").append(
                            " size=\"").append(fieldvalue.getField().getCols()).append("\">");
        }
        if (ret != null) {
            retVal.put(ReturnValues.SNIPLETT, ret);
        }
        return retVal;
    }
}

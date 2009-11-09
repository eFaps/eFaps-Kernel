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

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.Status.StatusGroup;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * This Class gets a Status from the Database.<br>
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("f83e4c40-db9d-41d0-bf34-eec674f04b6f")
@EFapsRevision("$Rev$")
public class StatusValue implements EventExecution
{

    /**
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter    parameter as defined by the efaps api
     * @return map with value and keys
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final Return ret = new Return();

        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);
        final Type type = fieldValue.getInstance().getType().getStatusAttribute().getLink();

        final Map<String, String> map = new TreeMap<String, String>();

        if (fieldValue.getTargetMode().equals(TargetMode.VIEW) || fieldValue.getTargetMode().equals(TargetMode.PRINT)
                         || fieldValue.getTargetMode().equals(TargetMode.UNKNOWN)) {
            final Status status = Status.get((Long) fieldValue.getValue());
            map.put(status.getLabel(), ((Long) status.getId()).toString());
        } else {
            final StatusGroup group = Status.get(type.getName());
            for (final Status status : group.values()) {
                map.put(status.getLabel(), ((Long) status.getId()).toString());
            }
        }
        ret.put(ReturnValues.VALUES, map);
        return ret;
    }



    /**
     * Method to set the Status for an Instance.
     * @param _parameter parameter as defined by the efaps api
     * @return empty Return
     * @throws EFapsException on error
     */
    public Return setStatus(final Parameter _parameter)
        throws EFapsException
    {
        final Map<?, ?> map = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final String statusName = (String) map.get("Status");
        final Instance instance = _parameter.getInstance();
        final Status status = Status.find(instance.getType().getStatusAttribute().getLink().getName(), statusName);

        if (status != null) {
            final Update update = new Update(instance);
            update.add(instance.getType().getStatusAttribute(), ((Long) status.getId()).toString());
            update.execute();
        }

        final Return ret = new Return();
        return ret;
    }

}

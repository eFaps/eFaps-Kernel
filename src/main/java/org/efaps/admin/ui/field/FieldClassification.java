/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.admin.ui.field;

import java.util.List;

import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: FieldClassification.java 9468 2013-05-19 02:21:22Z
 *          jan@moxter.net $
 */
public class FieldClassification
    extends Field
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is the constructor of the field class.
     *
     * @param _id id of the field instance
     * @param _uuid UUID of the field instance
     * @param _name name of the field instance
     */
    public FieldClassification(final long _id,
                               final String _uuid,
                               final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAccess(final TargetMode _targetMode,
                             final Instance _instance,
                             final AbstractCommand _callCmd)
        throws EFapsException
    {
        boolean ret = false;
        if (Classification.get(getClassificationName()) != null
                        && (Classification.get(getClassificationName()))
                                        .isAssigendTo(Context.getThreadContext().getCompany())
                        && !AppAccessHandler.excludeMode()) {
            final Classification clazz = Classification.get(getClassificationName());
            // check if any of the type ahs access
            ret = checkAccessOnChild(clazz, _instance, _targetMode == TargetMode.CREATE
                            || _targetMode == TargetMode.EDIT ? AccessTypeEnums.CREATE.getAccessType()
                            : AccessTypeEnums.SHOW.getAccessType());
        }
        if ((ret || AppAccessHandler.excludeMode()) && super.hasEvents(EventType.UI_ACCESSCHECK)) {
            ret = false;
            final List<EventDefinition> events = super.getEvents(EventType.UI_ACCESSCHECK);

            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.UIOBJECT, this);
            parameter.put(ParameterValues.ACCESSMODE, _targetMode);
            parameter.put(ParameterValues.INSTANCE, _instance);
            for (final EventDefinition event : events) {
                final Return retIn = event.execute(parameter);
                ret = retIn.get(ReturnValues.TRUE) != null;
            }
        }
        return ret;
    }

    /**
     * @param _parent parent to iterate down
     * @param _instance instance to check
     * @param _accessType   accesstype
     * @return true of access is granted
     * @throws EFapsException on error
     */
    private boolean checkAccessOnChild(final Classification _parent,
                                       final Instance _instance,
                                       final AccessType _accessType)
        throws EFapsException
    {
        boolean ret = false;
        if (!_parent.isAbstract()) {
            ret = _parent.hasAccess(_instance, _accessType);
        }
        if (!ret) {
            for (final Classification childClass : _parent.getChildClassifications()) {
                ret = childClass.hasAccess(_instance, _accessType);
                if (ret) {
                    break;
                }
            }
        }
        return ret;
    }
}

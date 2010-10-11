/*
 * Copyright 2003 - 2010 The eFaps Team
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

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldClassification
    extends Field
{

    /**
     * Stores the classification for this field.
     */
    private String classificationName;

    /**
     * This is the constructor of the field class.
     *
     * @param _id       id of the field instance
     * @param _uuid     UUID of the field instance
     * @param _name     name of the field instance
     */
    public FieldClassification(final long _id,
                               final String _uuid,
                               final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Getter method for instance variable {@link #classificationName}.
     *
     * @return value of instance variable {@link #classificationName}
     */
    public String getClassificationName()
    {
        return this.classificationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAccess(final TargetMode _targetMode,
                             final Instance _instance)
        throws EFapsException
    {
        boolean ret = false;
        if (Type.get(this.classificationName) != null
                        && ((Classification) Type.get(this.classificationName))
                            .isAssigendTo(Context.getThreadContext().getCompany())
                        && !AppAccessHandler.excludeMode()) {
            ret = true;
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
     * @see org.efaps.admin.ui.field.Field#setProperty(java.lang.String, java.lang.String)
     * @param _name     name of the property
     * @param _value    value for the property
     * @throws CacheReloadException on error
     */
    @Override
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("Classification".equals(_name)) {
            this.classificationName = _value;
        } else {
            super.setProperty(_name, _value);
        }
    }
}

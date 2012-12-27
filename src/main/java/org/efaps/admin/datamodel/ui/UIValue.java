/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.admin.datamodel.ui;

import java.io.Serializable;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * Wrapper Class to get a value for a UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class UIValue
    implements Serializable
{

    /**
     *Needed foer serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Value form the database.
     */
    private final Serializable dbValue;

    /**
     * Id of the field this Value belongs to.
     */
    private final long fieldId;

    /**
     * Id of the Attribute this value belongs to.
     */
    private final long attributeId;

    /**
     * Stores he display mode for this field.
     */
    private Display display;

    /**
     * The TargetMode the value is wanted for.
     */
    private TargetMode targetMode;

    /**
     * @param _field        Field
     * @param _attribute    attribute
     * @param _value        value
     */
    private UIValue(final Field _field,
                    final Attribute _attribute,
                    final Serializable _value)
    {
        this.fieldId = _field == null ? 0 : _field.getId();
        this.attributeId = _attribute == null ? 0 : _attribute.getId();
        this.dbValue = _value;
    }

    /**
     * @param _field        Field
     * @param _attribute    attribute
     * @param _value        value
     * @return  UIValue
     */
    public static UIValue get(final Field _field,
                              final Attribute _attribute,
                              final Object _value)
    {
        UIValue ret;
        if (_value instanceof Serializable) {
            ret = new UIValue(_field, _attribute, (Serializable) _value);
        } else if (_value == null) {
            ret = new UIValue(_field, _attribute, null);
        } else {
            // throw warning!
            ret = new UIValue(_field, _attribute, null);
        }
        return ret;
    }

    /**
     * @return the field belonging to this UIValue
     */
    public Field getField()
    {
        return Field.get(this.fieldId);
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.dbValue);
    }

    /**
     * Method to get a plain string for this FieldValue .
     *
     * @see #executeEvents
     * @param _mode target mode
     * @throws EFapsException on error
     * @return plain string
     * @throws EFapsException
     */
    public Object getEditValue(final TargetMode _mode)
        throws EFapsException
    {

        this.display = Display.EDITABLE;
        Object ret = executeEvents(EventType.UI_FIELD_VALUE, _mode);
        if (ret == null) {
            ret = executeEvents(EventType.UI_FIELD_FORMAT, _mode);
            if (ret == null && getUIProvider() != null) {
                ret = getUIProvider().getValue(this);
            }
        }
        return ret;
    }

    /**
     * Method to get a plain string for this FieldValue .
     *
     * @see #executeEvents
     * @param _mode target mode
     * @throws EFapsException on error
     * @return plain string
     * @throws EFapsException
     */
    public Object getReadOnlyValue(final TargetMode _mode)
        throws EFapsException
    {

        this.display = Display.READONLY;
        this.targetMode = _mode;
        Object ret = executeEvents(EventType.UI_FIELD_VALUE, _mode);
        if (ret == null) {
            ret = executeEvents(EventType.UI_FIELD_FORMAT, _mode);
            if (ret == null && getUIProvider() != null) {
                ret = getUIProvider().getValue(this);
            }
        }
        return ret;
    }

    /**
     * @return the UIProvider for this value
     * @throws CacheReloadException on eror
     */
    public IUIProvider getUIProvider()
        throws CacheReloadException
    {
        IUIProvider ret;
        if (this.attributeId > 0) {
            ret =  getAttribute().getAttributeType().getUIProvider();
        } else {
            ret = getField().getUIProvider();
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #display}.
     *
     * @return value of instance variable {@link #display}
     */
    public Display getDisplay()
    {
        return this.display;
    }

    /**
     * Getter method for the instance variable {@link #dbValue}.
     *
     * @return value of instance variable {@link #dbValue}
     */
    public Serializable getDbValue()
    {
        return this.dbValue;
    }

    /**
     * @return Attribute
     * @throws CacheReloadException on error
     */
    public Attribute getAttribute()
        throws CacheReloadException
    {
        return Attribute.get(this.attributeId);
    }

    /**
     * Getter method for the instance variable {@link #targetMode}.
     *
     * @return value of instance variable {@link #targetMode}
     */
    public TargetMode getTargetMode()
    {
        return this.targetMode;
    }

    /**
     * Executes the field value events for a field.
     *
     * @param _eventType    type of event to be executed
     * @param _targetMode   targetmode
     * @throws EFapsException on error
     * @return string from called field value events or <code>null</code> if no
     *         field value event is defined
     *
     */
    protected Object executeEvents(final EventType _eventType,
                                   final TargetMode _targetMode)
        throws EFapsException
    {
        Object ret = null;
        if ((this.fieldId > 0) && getField().hasEvents(_eventType)) {

            final List<EventDefinition> events = getField().getEvents(_eventType);

            final StringBuilder html = new StringBuilder();
            if (events != null) {
                final Parameter parameter = new Parameter();
                parameter.put(ParameterValues.ACCESSMODE, _targetMode);
                parameter.put(ParameterValues.UIOBJECT, this);
                parameter.put(ParameterValues.CALL_INSTANCE, null);
                parameter.put(ParameterValues.INSTANCE, null);
                parameter.put(ParameterValues.REQUEST_INSTANCES, null);
                if (parameter.get(ParameterValues.PARAMETERS) == null) {
                    parameter.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
                }
                for (final EventDefinition evenDef : events) {
                    final Return retu = evenDef.execute(parameter);
                    if (retu.get(ReturnValues.SNIPLETT) != null) {
                        html.append(retu.get(ReturnValues.SNIPLETT));
                    } else if (retu.get(ReturnValues.VALUES) != null) {
                        ret = retu.get(ReturnValues.VALUES);
                    }
                }
            }
            if (html.length() > 0) {
                ret = html.toString();
            }
        }
        return ret;
    }
}

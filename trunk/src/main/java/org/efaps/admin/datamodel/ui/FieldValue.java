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

package org.efaps.admin.datamodel.ui;

import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used as the value for a field. It can be used to get the for
 * the user interface necessary strings etc.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldValue implements Comparable<Object>
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FieldValue.class);

    /**
     * The instance variable stores the class to represent this form value.
     *
     * @see #getClassUI
     */
    private final Attribute attribute;

    /**
     * The variable stores the field instance for this value.
     *
     * @see #getInstance
     */
    private final Instance instance;

    /**
     * The instance variable stores the field for this value.
     *
     * @see #getFieldDef
     */
    private final Field field;

    /**
     * Stores the UIInterface belonging to this fieldvalue.
     */
    private final UIInterface ui;

    /**
     * The instance variable stores the value for this form value.
     *
     * @see #getValue
     */
    private Object value;

    /**
     * Targetmode for this fieldvalue.
     */
    private TargetMode targetMode;

    /**
     * Stores he display mode for this field.
     */
    private Display display;

     /**
      *  instance for which this field is called (not the same
      *  as the instance of the field...).
      */
    private Instance callInstance;


    /**
     * Construtor used to evaluate the value from the database by using one of
     * the getter methods for html.
     *
     * @param _field            field this value belongs to
     * @param _attr             attribute the value belongs to
     * @param _value            value of the FieldValue
     * @param _valueInstance     Instance the Value belongs to
     * @param _callInstance     Instance that called Value for edit, view etc.
     */
    public FieldValue(final Field _field,
                      final Attribute _attr,
                      final Object _value,
                      final Instance _valueInstance,
                      final Instance _callInstance)
    {
        this.field = _field;
        this.attribute = _attr;
        this.value = _value;
        this.instance = _valueInstance;
        this.callInstance = _callInstance;
        this.ui = (_attr == null)
            ? (this.field.getClassUI() == null ? new StringUI()
            : this.field.getClassUI()) : _attr.getAttributeType().getUI();
    }


    /**
     * Constructor used in case of comparison.
     *
     * @param _field field this value belongs to
     * @param _ui UIInterface this value belongs to
     * @param _compareValue value to be compared
     */
    public FieldValue(final Field _field,
                      final UIInterface _ui,
                      final Object _compareValue)
    {
        this.ui = _ui;
        this.field = _field;
        this.value = _compareValue;
        this.instance = null;
        this.attribute = null;
    }


    /**
     * Method to get html code for this FieldValue in case of edit.
     *
     * @see #executeEvents
     * @param _mode         target mode
     * @throws EFapsException on error
     * @return html code as a String
     */
    public String getEditHtml(final TargetMode _mode)
        throws EFapsException
    {
        this.targetMode = _mode;
        this.display = Display.EDITABLE;
        String ret = null;
        ret = executeEvents(EventType.UI_FIELD_VALUE);
        if (ret == null) {
            ret = executeEvents(EventType.UI_FIELD_FORMAT);
            if (ret == null && this.ui != null) {
                ret = this.ui.getEditHtml(this);
            }
        }
        return ret;
    }

    /**
     * Method to get html code for this FieldValue in case of view.
     *
     * @see #executeEvents
     * @param _mode         target mode
     * @throws EFapsException on error
     * @return html code as a String
     */
    public String getReadOnlyHtml(final TargetMode _mode)
        throws EFapsException
    {
        this.targetMode = _mode;
        this.display = Display.READONLY;
        String ret = null;
        ret = executeEvents(EventType.UI_FIELD_VALUE);
        if (ret == null) {
            ret = executeEvents(EventType.UI_FIELD_FORMAT);
            if (ret == null && this.ui != null) {
                ret = this.ui.getReadOnlyHtml(this);
            }
        }
        return ret;
    }

    /**
     * Method to get html code for this FieldValue in case of hidden.
     *
     * @see #executeEvents
     * @param _mode         target mode
     * @throws EFapsException on error
     * @return html code as a String
     */
    public String getHiddenHtml(final TargetMode _mode)
        throws EFapsException
    {
        this.targetMode = _mode;
        this.display = Display.HIDDEN;
        String ret = null;
        ret = executeEvents(EventType.UI_FIELD_VALUE);
        if (ret == null) {
            ret = executeEvents(EventType.UI_FIELD_FORMAT);
            if (ret == null && this.ui != null) {
                ret = this.ui.getHiddenHtml(this);
            }
        }
        return ret;
    }

    /**
     * Method to get a plain string for this FieldValue .
     *
     * @see #executeEvents
     * @param _mode         target mode
     * @throws EFapsException on error
     * @return plain string
     * @throws EFapsException
     */
    public String getStringValue(final TargetMode _mode)
        throws EFapsException
    {
        this.targetMode = _mode;
        this.display = Display.NONE;
        String ret = null;
        ret = executeEvents(EventType.UI_FIELD_VALUE);
        if (ret == null) {
            ret = executeEvents(EventType.UI_FIELD_FORMAT);
            if (ret == null && this.ui != null) {
                ret = this.ui.getStringValue(this);
            }
        }
        return ret;
    }

    /**
     * Executes the field value events for a field.
     * @param _eventType type of event to be executed
     * @throws EFapsException on error
     * @return string from called field value events or <code>null</code> if no
     *         field value event is defined
     *
     */
    protected String executeEvents(final EventType _eventType)
        throws EFapsException
    {
        String ret = null;
        if ((this.field != null) && this.field.hasEvents(_eventType)) {

            final List<EventDefinition> events = this.field.getEvents(_eventType);

            final StringBuilder html = new StringBuilder();
            if (events != null) {
                final Parameter parameter = new Parameter();
                parameter.put(ParameterValues.ACCESSMODE, this.targetMode);
                parameter.put(ParameterValues.UIOBJECT, this);
                parameter.put(ParameterValues.CALL_INSTANCE, this.callInstance);
                parameter.put(ParameterValues.INSTANCE, this.instance);
                if (parameter.get(ParameterValues.PARAMETERS) == null) {
                    parameter.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
                }
                for (final EventDefinition evenDef : events) {
                    final Return retu = evenDef.execute(parameter);
                    if (retu.get(ReturnValues.SNIPLETT) != null) {
                        html.append(retu.get(ReturnValues.SNIPLETT));
                    } else if (retu.get(ReturnValues.VALUES) != null) {
                        this.value = retu.get(ReturnValues.VALUES);
                    }
                }
            }
            if (html.length() > 0) {
                ret = html.toString();
            }
        }
        return ret;
    }

    /**
     * Method is used to retrieve the value that mut be used for comparison.
     *
     * @return Object
     * @throws EFapsException on error
     */
    public Object getObject4Compare() throws EFapsException
    {
        Object ret = null;
        if (this.ui != null) {
            ret = this.ui.getObject4Compare(this);
        }
        return ret;
    }

    /**
     * The user interface implementing this attribute is returned. If no
     * attribute for this field is defined, a <code>null</code> is returned.
     *
     * @return class implementing the user interface for given attribute
     *         instance or <code>null</code> if not attribute is defined
     * @see #attribute
     */
    public UIInterface getClassUI()
    {
        return this.ui;
    }

    /**
     * Method to compare this FieldValue to a target FieldValue.
     *
     * @param _target field value to compare to
     * @return 0 if smaller, else -1
     */
    public int compareTo(final Object _target)
    {
        final FieldValue target = (FieldValue) _target;

        int ret = 0;
        if (this.value == null || target.getValue() == null) {
            if (this.value == null && target.getValue() != null) {
                ret = -1;
            }
            if (this.value != null && target.getValue() == null) {
                ret = 1;
            }
        } else {
            if (getClassUI().equals(target.getClassUI())) {
                try {
                    ret = getClassUI().compare(this, target);
                } catch (final EFapsException e) {
                    FieldValue.LOG.error("Comparing FieldValue to another FieldValue threw an error", e);
                }
            } else {
                FieldValue.LOG.error("can't compare this Objects because they don't have the same ClassUI");
            }
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     * @see #instance
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Getter method for the instance variable {@link #callInstance}.
     *
     * @return value of instance variable {@link #callInstance}
     */
    public Instance getCallInstance()
    {
        return this.callInstance;
    }

    /**
     * This is the getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     * @see #value
     */
    public Object getValue()
    {
        return this.value;
    }

    /**
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     */
    public void setValue(final Object _value)
    {
        this.value = _value;
    }

    /**
     * This is the getter method for instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     * @see #attribute
     */
    public Attribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * This is the getter method for instance variable {@link #field}.
     *
     * @return value of instance variable {@link #field}
     */
    public Field getField()
    {
        return this.field;
    }

    /**
     * Getter method for instance variable {@link #targetMode}.
     *
     * @return value of instance variable {@link #targetMode}
     */
    public TargetMode getTargetMode()
    {
        return this.targetMode;
    }

    /**
     * Setter method for instance variable {@link #targetMode}.
     *
     * @param _targetMode value for instance variable {@link #targetMode}
     */

    public void setTargetMode(final TargetMode _targetMode)
    {
        this.targetMode = _targetMode;
    }

    /**
     * Getter method for instance variable {@link #display}.
     *
     * @return value of instance variable {@link #display}
     */
    public Display getDisplay()
    {
        return this.display;
    }
}

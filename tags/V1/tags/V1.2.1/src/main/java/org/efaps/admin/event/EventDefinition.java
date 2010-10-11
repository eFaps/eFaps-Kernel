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

package org.efaps.admin.event;

import static org.efaps.admin.EFapsClassNames.COMMAND;
import static org.efaps.admin.EFapsClassNames.DATAMODEL_ATTRIBUTE;
import static org.efaps.admin.EFapsClassNames.DATAMODEL_ATTRIBUTESETATTRIBUTE;
import static org.efaps.admin.EFapsClassNames.DATAMODEL_TYPE;
import static org.efaps.admin.EFapsClassNames.EVENT_DEFINITION;
import static org.efaps.admin.EFapsClassNames.FIELD;
import static org.efaps.admin.EFapsClassNames.FIELDCLASSIFICATION;
import static org.efaps.admin.EFapsClassNames.FIELDCOMMAND;
import static org.efaps.admin.EFapsClassNames.FIELDGROUP;
import static org.efaps.admin.EFapsClassNames.FIELDHEADING;
import static org.efaps.admin.EFapsClassNames.FIELDTABLE;
import static org.efaps.admin.EFapsClassNames.MENU;
import static org.efaps.admin.EFapsClassNames.PICKER;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Picker;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In this Class a Event can be defined. <br/>
 * On loading the Cache all EventDefenitions are initialized and assigned to the
 * specific administrational type or command. On initialization of a
 * EventDefinition, for faster access during runtime, the Class of the Program
 * is instantiated and the Method stored.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class EventDefinition
    extends AbstractAdminObject
    implements EventExecution
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EventDefinition.class);

    /**
     * Classloader used for EventDefinition.
     */
    private static final EFapsClassLoader CLASSLOADER =  new EFapsClassLoader(EventDefinition.class.getClassLoader());

    /**
     * The variable stores the position in a event pool (more than one event
     * definition for one thrown event.
     *
     * @see #getIndexPos
     */
    private final int indexPos;

    /**
     * The variable stores the Name of the JavaClass.
     */
    private final String resourceName;

    /**
     * The variable stores the Name of the method to be invoked.
     */
    private final String methodName;

    /**
     * The variable stores the Method to be invoked.
     */
    private Method method = null;

    /**
     * holds the instance of this.
     */
    private Object progInstance = null;

    /**
     * @param _instance     Instance of this EventDefinition
     * @param _name         name of this EventDefinition
     * @param _indexPos     index position of this EventDefinition
     * @param _resourceName name of the resource of this EventDefinition
     * @param _method       method of this EventDefinition
     * @throws EFapsException on error
     */
    private EventDefinition(final Instance _instance,
                            final String _name,
                            final int _indexPos,
                            final String _resourceName,
                            final String _method)
        throws EFapsException
    {
        super(_instance.getId(), null, _name);
        this.indexPos = _indexPos;
        this.resourceName = _resourceName;
        this.methodName = _method;
        setProgramInstance();
        setProperties(_instance);
    }

    /**
     * Set the properties in the superclass.
     *
     * @param _instance Instance of this EventDefinition
     * @throws EFapsException on error
     */
    private void setProperties(final Instance _instance)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(Type.get(EFapsClassNames.ADMIN_COMMON_PROPERTY));
        queryBldr.addWhereAttrEqValue("Abstract", _instance.getId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute("Name", "Value");
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            super.setProperty(multi.<String> getAttribute("Name"), multi.<String> getAttribute("Value"));
        }
    }

    /**
     * This is the getter method for instance variable {@link #indexPos}.
     *
     * @return value of instance variable {@link #indexPos}
     * @see #indexPos
     */
    public long getIndexPos()
    {
        return this.indexPos;
    }

    /**
     * This is the getter method for instance variable {@link #resourceName}.
     *
     * @return value of instance variable {@link #resourceName}
     * @see #resourceName
     */
    public String getResourceName()
    {
        return this.resourceName;
    }

    /**
     * Method to set the instance of the esjp.
     */
    private void setProgramInstance()
    {
        try {
            if (EventDefinition.LOG.isDebugEnabled()) {
                EventDefinition.LOG.debug("setting Instance: " + this.resourceName + " - " + this.methodName);
            }
            final Class<?> cls = Class.forName(this.resourceName, true, EventDefinition.CLASSLOADER);
            this.method = cls.getMethod(this.methodName, new Class[] { Parameter.class });
            this.progInstance = cls.newInstance();
        } catch (final ClassNotFoundException e) {
            EventDefinition.LOG.error("could not find Class: '" + this.resourceName + "'", e);
        } catch (final InstantiationException e) {
            EventDefinition.LOG.error("could not instantiat Class: '" + this.resourceName + "'", e);
        } catch (final IllegalAccessException e) {
            EventDefinition.LOG.error("could not access Class: '" + this.resourceName + "'", e);
        } catch (final SecurityException e) {
            EventDefinition.LOG.error("could not access Class: '" + this.resourceName + "'", e);
        } catch (final NoSuchMethodException e) {
            EventDefinition.LOG.error("could not find method: '"
                            + this.methodName + "' in class: '" + this.resourceName + "'", e);
        }
    }

    /**
     * Method to execute the esjp.
     *
     * @param _parameter Parameter
     * @return Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        Return ret = null;
        _parameter.put(ParameterValues.PROPERTIES, super.getProperties());
        try {
            ret = (Return) this.method.invoke(this.progInstance, _parameter);
        } catch (final SecurityException e) {
            EventDefinition.LOG.error("could not access class: '" + this.resourceName, e);
        } catch (final IllegalArgumentException e) {
            EventDefinition.LOG.error("execute(Context, Instance, Map<TriggerKeys4Values,Map>)", e);
        } catch (final IllegalAccessException e) {
            EventDefinition.LOG.error("could not access class: '" + this.resourceName, e);
        } catch (final InvocationTargetException e) {
            EventDefinition.LOG.error("could not invoke method: '" + this.methodName
                            + "' in class: '" + this.resourceName, e);
            throw (EFapsException) e.getCause();
        }
        return ret;

    }

    /**
     * Loads all events from the database and assigns them to the specific
     * administrational type or command.
     *
     * @throws EFapsException on error
     */
    public static void initialize()
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(Type.get(EVENT_DEFINITION));
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute("Type", "Name", "Abstract", "IndexPosition", "JavaProg", "Method");
        multi.executeWithoutAccessCheck();

        if (EventDefinition.LOG.isDebugEnabled()) {
            EventDefinition.LOG.debug("initialise Triggers ---------------------------------------");
        }
        while (multi.next()) {
            //define all variables here so that an error can be thrown containing the
            //values that where set correctly
            Instance inst = null;
            Type eventType = null;
            String eventName = null;
            int eventPos = 0;
            long abstractID = 0;
            long programId = 0;
            String method = null;
            String resName = null;
            try {
                inst = multi.getCurrentInstance();
                eventType = multi.<Type>getAttribute("Type");
                eventName = multi.<String>getAttribute("Name");
                eventPos = multi.<Integer>getAttribute("IndexPosition");
                abstractID = multi.<Long>getAttribute("Abstract");
                programId = multi.<Long>getAttribute("JavaProg");
                method = multi.<String>getAttribute("Method");

                resName = EventDefinition.getClassName(programId);

                if (EventDefinition.LOG.isDebugEnabled()) {
                    EventDefinition.LOG.debug("   Instance=" + inst);
                    EventDefinition.LOG.debug("   eventType=" + eventType);
                    EventDefinition.LOG.debug("   eventName=" + eventName);
                    EventDefinition.LOG.debug("   eventPos=" + eventPos);
                    EventDefinition.LOG.debug("   parentId=" + abstractID);
                    EventDefinition.LOG.debug("   programId=" + programId);
                    EventDefinition.LOG.debug("   Method=" + method);
                    EventDefinition.LOG.debug("   resName=" + resName);
                }

                final EFapsClassNames eFapsClass = EFapsClassNames.getEnum(EventDefinition.getTypeName(abstractID,
                                inst, eventName));

                EventType triggerEvent = null;
                for (final EventType trigger : EventType.values()) {
                    final Type triggerClass = Type.get(trigger.name);
                    if (eventType.isKindOf(triggerClass)) {
                        if (EventDefinition.LOG.isDebugEnabled()) {
                            EventDefinition.LOG.debug("     found trigger " + trigger + ":" + triggerClass);
                        }
                        triggerEvent = trigger;
                        break;
                    }
                }

                if (eFapsClass == DATAMODEL_TYPE) {
                    final Type type = Type.get(abstractID);
                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("    type=" + type);
                    }

                    type.addEvent(triggerEvent,
                                    new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (eFapsClass == COMMAND) {
                    final Command command = Command.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("    Command=" + command.getName());
                    }
                    command.addEvent(triggerEvent, new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (eFapsClass == FIELD || eFapsClass == FIELDCOMMAND || eFapsClass == FIELDGROUP
                                || eFapsClass == FIELDHEADING || eFapsClass == FIELDCLASSIFICATION) {
                    final Field field = Field.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("       Field=" + field.getName());
                    }

                    field.addEvent(triggerEvent, new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (eFapsClass == DATAMODEL_ATTRIBUTE || eFapsClass == DATAMODEL_ATTRIBUTESETATTRIBUTE) {
                    final Attribute attribute = Attribute.get(abstractID);
                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("      Attribute=" + attribute.getName());
                    }

                    attribute.addEvent(triggerEvent, new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (eFapsClass == MENU) {
                    final Menu menu = Menu.get(abstractID);
                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("      Menu=" + menu.getName());
                    }

                    menu.addEvent(triggerEvent, new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (eFapsClass == FIELDTABLE) {

                    final FieldTable fieldtable = FieldTable.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("       Field=" + fieldtable.getName());
                    }

                    fieldtable.addEvent(triggerEvent, new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (eFapsClass == PICKER) {

                    final Picker picker = Picker.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("       Picker=" + picker.getName());
                    }

                    picker.addEvent(triggerEvent, new EventDefinition(inst, eventName, eventPos, resName, method));

                } else if (EventDefinition.LOG.isDebugEnabled()) {
                    EventDefinition.LOG.debug("initialise() - unknown event trigger connection");
                }
            } catch (final Exception e) {
                if (e instanceof EFapsException) {
                    throw (EFapsException) e;
                } else {
                    throw new EFapsException(EventDefinition.class, "initialize", e, inst, eventName, eventPos,
                                    resName, method);
                }
            }
        }
    }

    /**
     * Get the ClassName from the Database.
     *
     * @param _programId ID of the Program the ClassName is searched for
     * @return ClassName
     * @throws EFapsException on error
     */
    private static String getClassName(final Long _programId)
        throws EFapsException
    {
        String ret = null;
        final PrintQuery print = new PrintQuery(Type.get(EFapsClassNames.ADMIN_PROGRAM_JAVA), _programId);
        print.addAttribute("Name");

        if (print.executeWithoutAccessCheck()) {
            ret = print.getAttribute("Name");
        } else {
            EventDefinition.LOG.error("Can't find the Name for the Program with ID: " + _programId);
        }
        return ret;
    }

    /**
     * Get the Name of the Type from the Database.
     *
     * @param _abstractID   ID the Typename must be resolved
     * @param _eventDefInst Instance of the event, only used for logging on error
     * @param _eventName    name of the event, only used for logging error
     * @return NAem of the Type
     * @throws EFapsException on error
     */
    private static UUID getTypeName(final long _abstractID,
                                    final Instance _eventDefInst,
                                    final String _eventName)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(Type.get("Admin_Abstract"));
        queryBldr.addWhereAttrEqValue("ID", _abstractID);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute("Type");
        multi.executeWithoutAccessCheck();
        Type type;
        if (multi.next()) {
            type = multi.<Type> getAttribute("Type");
        } else {
            // necessary, because for "Admin_Abstract" the Query does not
            // work
            type = Type.get(_abstractID);
        }
        UUID ret = null;
        if (type == null) {
            EventDefinition.LOG.error("Can't find the Type  with ID: " + _abstractID + " for event Name: " + _eventName
                            + " Instance: " + _eventDefInst);
        } else {
            ret = type.getUUID();
        }
        return ret;
    }
}

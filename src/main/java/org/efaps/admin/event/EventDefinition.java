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
import org.efaps.db.SearchQuery;
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
    private final long indexPos;

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
     * @param _id           id of this EventDefinition
     * @param _name         name of this EventDefinition
     * @param _indexPos     index position of this EventDefinition
     * @param _resourceName name of the resource of this EventDefinition
     * @param _method       method of this EventDefinition
     * @param _oid oid of this EventDefinition
     */
    private EventDefinition(final long _id,
                            final String _name,
                            final long _indexPos,
                            final String _resourceName,
                            final String _method,
                            final String _oid)
    {
        super(_id, null, _name);
        this.indexPos = _indexPos;
        this.resourceName = _resourceName;
        this.methodName = _method;
        setInstance();
        setProperties(_oid);
    }

    /**
     * Set the properties in the superclass.
     *
     * @param _oid OID of this EventDefinition
     */
    private void setProperties(final String _oid)
    {
        final SearchQuery query = new SearchQuery();
        try {
            query.setExpand(_oid, "Admin_Common_Property\\Abstract");
            query.addSelect("Name");
            query.addSelect("Value");
            query.executeWithoutAccessCheck();

            while (query.next()) {
                super.setProperty((String) query.get("Name"), (String) query.get("Value"));
            }
        } catch (final EFapsException e) {
            EventDefinition.LOG.error("setProperties(String)", e);
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
    private void setInstance()
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
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(Type.get(EVENT_DEFINITION).getName());
        query.setExpandChildTypes(true);
        query.addSelect("OID");
        query.addSelect("ID");
        query.addSelect("Type");
        query.addSelect("Name");
        query.addSelect("Abstract");
        query.addSelect("IndexPosition");
        query.addSelect("JavaProg");
        query.addSelect("Method");
        query.executeWithoutAccessCheck();

        if (EventDefinition.LOG.isDebugEnabled()) {
            EventDefinition.LOG.debug("initialise Triggers ---------------------------------------");
        }
        while (query.next()) {
            final String eventOID = (String) query.get("OID");
            final long eventId = ((Number) query.get("ID")).longValue();
            final Type eventType = (Type) query.get("Type");
            final String eventName = (String) query.get("Name");
            final long eventPos = (Long) query.get("IndexPosition");
            final long abstractID = ((Number) query.get("Abstract")).longValue();
            final Long programId = ((Number) query.get("JavaProg")).longValue();
            final String method = (String) query.get("Method");

            final String resName = EventDefinition.getClassName(programId.toString());

            if (EventDefinition.LOG.isDebugEnabled()) {
                EventDefinition.LOG.debug("   OID=" + eventOID);
                EventDefinition.LOG.debug("   eventId=" + eventId);
                EventDefinition.LOG.debug("   eventType=" + eventType);
                EventDefinition.LOG.debug("   eventName=" + eventName);
                EventDefinition.LOG.debug("   eventPos=" + eventPos);
                EventDefinition.LOG.debug("   parentId=" + abstractID);
                EventDefinition.LOG.debug("   programId=" + programId);
                EventDefinition.LOG.debug("   Method=" + method);
                EventDefinition.LOG.debug("   resName=" + resName);
            }

            final EFapsClassNames eFapsClass = EFapsClassNames.getEnum(EventDefinition.getTypeName(abstractID));

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
            try {
                if (eFapsClass == DATAMODEL_TYPE) {
                    final Type type = Type.get(abstractID);
                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("    type=" + type);
                    }

                    type.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (eFapsClass == COMMAND) {
                    final Command command = Command.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("    Command=" + command.getName());
                    }
                    command.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (eFapsClass == FIELD || eFapsClass == FIELDCOMMAND || eFapsClass == FIELDGROUP
                                || eFapsClass == FIELDHEADING || eFapsClass == FIELDCLASSIFICATION) {
                    final Field field = Field.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("       Field=" + field.getName());
                    }

                    field.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (eFapsClass == DATAMODEL_ATTRIBUTE || eFapsClass == DATAMODEL_ATTRIBUTESETATTRIBUTE) {
                    final Attribute attribute = Attribute.get(abstractID);
                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("      Attribute=" + attribute.getName());
                    }

                    attribute.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (eFapsClass == MENU) {
                    final Menu menu = Menu.get(abstractID);
                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("      Menu=" + menu.getName());
                    }

                    menu.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (eFapsClass == FIELDTABLE) {

                    final FieldTable fieldtable = FieldTable.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("       Field=" + fieldtable.getName());
                    }

                    fieldtable.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (eFapsClass == PICKER) {

                    final Picker picker = Picker.get(abstractID);

                    if (EventDefinition.LOG.isDebugEnabled()) {
                        EventDefinition.LOG.debug("       Picker=" + picker.getName());
                    }

                    picker.addEvent(triggerEvent,
                                    new EventDefinition(eventId, eventName, eventPos, resName, method, eventOID));

                } else if (EventDefinition.LOG.isDebugEnabled()) {
                    EventDefinition.LOG.debug("initialise() - unknown event trigger connection");
                }
            } catch (final Exception e) {
                if (e instanceof EFapsException) {
                    throw (EFapsException) e;
                } else {
                    throw new EFapsException(EventDefinition.class, "initialize", e, eventId, eventName, eventPos,
                                    resName, method, eventOID);
                }
            }
        }
    }

    /**
     * Get the ClassName from the Database.
     *
     * @param _id ID of the Program the ClassName is searched for
     * @return ClassName
     */
    private static String getClassName(final String _id)
    {
        final SearchQuery query = new SearchQuery();
        String name = null;
        try {
            query.setQueryTypes("Admin_Program_Java");
            query.addSelect("Name");
            query.addWhereExprEqValue("ID", _id);
            query.executeWithoutAccessCheck();
            if (query.next()) {
                name = (String) query.get("Name");
            } else {
                EventDefinition.LOG.error("Can't find the Name for the Program with ID: " + _id);
            }
        } catch (final EFapsException e) {
            EventDefinition.LOG.error("getClassName(String)", e);
        }
        return name;
    }

    /**
     * Get the Name of the Type from the Database.
     *
     * @param _abstractID ID the Typename must be resolved
     * @return NAem of the Type
     */
    private static UUID getTypeName(final long _abstractID)
    {
        final SearchQuery query = new SearchQuery();
        Type type = null;
        UUID ret = null;
        try {
            query.setQueryTypes("Admin_Abstract");
            query.addSelect("Type");
            query.addWhereExprEqValue("ID", _abstractID);
            query.setExpandChildTypes(true);
            query.executeWithoutAccessCheck();
            if (query.next()) {
                type = (Type) query.get("Type");
            } else {
                // necessary, because for "Admin_Abstract" the Query does not
                // work
                type = Type.get(_abstractID);
            }
        } catch (final EFapsException e) {
            EventDefinition.LOG.error("getClassName(String)", e);
        }
        if (type == null) {
            EventDefinition.LOG.error("Can't find the Type  with ID: " + _abstractID);
        } else {
            ret = type.getUUID();
        }
        return ret;
    }
}

/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.admin.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminEvent;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
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
 *
 */
public final class EventDefinition
    extends AbstractAdminObject
    implements EventExecution
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EventDefinition.class);

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
     * @param _instance Instance of this EventDefinition
     * @param _name name of this EventDefinition
     * @param _indexPos index position of this EventDefinition
     * @param _resourceName name of the resource of this EventDefinition
     * @param _method method of this EventDefinition
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
        checkProgramInstance();
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
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.Property);
        queryBldr.addWhereAttrEqValue("Abstract", _instance.getId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute("Name", "Value");
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            super.setProperty(multi.<String>getAttribute("Name"), multi.<String>getAttribute("Value"));
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
     * Method to check if the instance of the esjp is valid.
     */
    private void checkProgramInstance()
    {
        try {
            if (EventDefinition.LOG.isDebugEnabled()) {
                EventDefinition.LOG.debug("checking Instance: {} - {}", this.resourceName, this.methodName);
            }
            final Class<?> cls = Class.forName(this.resourceName, true, EFapsClassLoader.getInstance());
            final Method method = cls.getMethod(this.methodName, new Class[] { Parameter.class });
            final Object progInstance = cls.newInstance();
            if (EventDefinition.LOG.isDebugEnabled()) {
                EventDefinition.LOG.debug("found Class: {} and method {}", progInstance, method);
            }
        } catch (final ClassNotFoundException e) {
            EventDefinition.LOG.error("could not find Class: '{}'", this.resourceName, e);
        } catch (final InstantiationException e) {
            EventDefinition.LOG.error("could not instantiat Class: '{}'", this.resourceName, e);
        } catch (final IllegalAccessException e) {
            EventDefinition.LOG.error("could not access Class: '{}'", this.resourceName, e);
        } catch (final SecurityException e) {
            EventDefinition.LOG.error("could not access Class: '{}'", this.resourceName, e);
        } catch (final NoSuchMethodException e) {
            EventDefinition.LOG.error("could not find method: '{}' in class '{}'",
                            new Object[] { this.methodName, this.resourceName, e });
        }
    }

    /**
     * Method to execute the esjp.
     *
     * @param _parameter Parameter
     * @return Return
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        Return ret = null;
        _parameter.put(ParameterValues.PROPERTIES, new HashMap<>(super.evalProperties()));
        try {
            EventDefinition.LOG.debug("Invoking method '{}' for Resource '{}'", this.methodName, this.resourceName);
            final Class<?> cls = Class.forName(this.resourceName, true, EFapsClassLoader.getInstance());
            final Method method = cls.getMethod(this.methodName, new Class[] { Parameter.class });
            ret = (Return) method.invoke(cls.newInstance(), _parameter);
            EventDefinition.LOG.debug("Terminated invokation of method '{}' for Resource '{}'",
                            this.methodName, this.resourceName);
        } catch (final SecurityException e) {
            EventDefinition.LOG.error("security wrong: '{}'", this.resourceName, e);
        } catch (final IllegalArgumentException e) {
            EventDefinition.LOG.error("arguments invalid : '{}'- '{}'", this.resourceName, this.methodName, e);
        } catch (final IllegalAccessException e) {
            EventDefinition.LOG.error("could not access class: '{}'", this.resourceName, e);
        } catch (final InvocationTargetException e) {
            EventDefinition.LOG.error("could not invoke method: '{}' in class: '{}'", this.methodName,
                            this.resourceName, e);
            throw (EFapsException) e.getCause();
        } catch (final ClassNotFoundException e) {
            EventDefinition.LOG.error("class not found: '{}" + this.resourceName, e);
        } catch (final NoSuchMethodException e) {
            EventDefinition.LOG.error("could not find method: '{}' in class '{}'",
                            new Object[] { this.methodName, this.resourceName, e });
        } catch (final InstantiationException e) {
            EventDefinition.LOG.error("could not instantiat Class: '{}'", this.resourceName, e);
        }
        return ret;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof EventDefinition) {
            ret = ((EventDefinition) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(getId()).intValue();
    }


    /**
     * @param _adminObject Object the event is added to
     * @throws EFapsException on error
     */
    public static void addEvents(final AbstractAdminObject _adminObject)
        throws EFapsException
    {
        // check is necessary for the first time installation
        if (CIAdminEvent.Definition.getType() != null && CIAdminEvent.Definition.getType().getMainTable() != null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminEvent.Definition);
            queryBldr.addWhereAttrEqValue(CIAdminEvent.Definition.Abstract, _adminObject.getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            final SelectBuilder selClass = new SelectBuilder().linkto(CIAdminEvent.Definition.JavaProg).attribute(
                            CIAdminProgram.Java.Name);
            multi.addSelect(selClass);
            multi.addAttribute(CIAdminEvent.Definition.Type,
                            CIAdminEvent.Definition.Name,
                            CIAdminEvent.Definition.IndexPosition,
                            CIAdminEvent.Definition.Method);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                // define all variables here so that an error can be thrown
                // containing the
                // values that where set correctly
                Instance inst = null;
                Type eventType = null;
                String eventName = null;
                int eventPos = 0;
                final long abstractID = 0;
                String program = "";
                String method = null;
                try {
                    inst = multi.getCurrentInstance();
                    eventType = multi.<Type>getAttribute(CIAdminEvent.Definition.Type);
                    eventName = multi.<String>getAttribute(CIAdminEvent.Definition.Name);
                    eventPos = multi.<Integer>getAttribute(CIAdminEvent.Definition.IndexPosition);
                    program = multi.<String>getSelect(selClass);
                    method = multi.<String>getAttribute(CIAdminEvent.Definition.Method);

                    EventDefinition.LOG.debug("Reading EventDefinition for: ");
                    EventDefinition.LOG.debug("   object = {}", _adminObject);
                    EventDefinition.LOG.debug("   Instance = {}", inst);
                    EventDefinition.LOG.debug("   eventType = {}", eventType);
                    EventDefinition.LOG.debug("   eventName = {}", eventName);
                    EventDefinition.LOG.debug("   eventPos = {}", eventPos);
                    EventDefinition.LOG.debug("   parentId = {}", abstractID);
                    EventDefinition.LOG.debug("   program = {}", program);
                    EventDefinition.LOG.debug("   Method = {}", method);

                    EventType triggerEvent = null;
                    for (final EventType trigger : EventType.values()) {
                        final Type triggerClass = Type.get(trigger.getName());
                        if (eventType.isKindOf(triggerClass)) {
                            if (EventDefinition.LOG.isDebugEnabled()) {
                                EventDefinition.LOG.debug("     found trigger " + trigger + ":" + triggerClass);
                            }
                            triggerEvent = trigger;
                            break;
                        }
                    }

                    _adminObject.addEvent(triggerEvent,
                                    new EventDefinition(inst, eventName, eventPos, program, method));

                    // CHECKSTYLE:OFF
                } catch (final Exception e) {
                    // CHECKSTYLE:ON
                    EventDefinition.LOG.error(
                                    "Instance: {}, eventType: {}, eventName: {}, eventPos: {}, parentId: {}, "
                                                    + "programId: {}, MethodresName: {}, , arguments: {}",
                                    inst, eventType, eventName, eventPos, abstractID, program, method, program);
                    if (e instanceof EFapsException) {
                        throw (EFapsException) e;
                    } else {
                        throw new EFapsException(EventDefinition.class, "initialize", e, inst, eventName, eventPos,
                                        program, method);
                    }
                }
            }
        }
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

    }
}

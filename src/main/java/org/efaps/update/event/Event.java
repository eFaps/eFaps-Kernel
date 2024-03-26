/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.update.event;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.api.datamodel.Overwrite;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines a Event to be connected with an update.
 *
 * @author The eFaps Team
 *
 */
public class Event
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Event.class);

    /**
     * Property value depending on the property name for this trigger.
     *
     * @see #addProperty(String, String)
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * Event as defined in {@link EventType}.
     */
    private final EventType event;

    /**
     * Name of the program invoked in this trigger.
     */
    private final String program;

    /**
     * Name of the method to be invoked by this trigger.
     */
    private final String method;

    /**
     * Index of the trigger.
     */
    private final long index;

    /**
     * Name of the trigger.
     */
    private final String name;

    /**
     * Constructor of event for a trigger setting all instance variables.
     *
     * @param _name     name of the event (if <code>null</code>, the event
     *                  itself is used as name)
     * @param _event    event as defined in {@link EventType}
     * @param _program  name of the program invoked in this trigger
     * @param _method   name of the method to be invoked by this trigger (if
     *                  <code>null</code>, method name <code>execute</code> is
     *                  used)
     * @param _index    index of the trigger
     */
    public Event(final String _name,
                 final EventType _event,
                 final String _program,
                 final String _method,
                 final String _index)
    {
        this.name = _name == null ? _event.getName() : _name;
        this.event = _event;
        this.program = _program;
        this.method = _method == null ? "execute" : _method;
        if (_index == null)  {
            this.index = 0;
        } else  {
            this.index = Long.parseLong(_index);
        }
    }

    /**
     * For given type defined with the instance parameter, this trigger is
     * searched by typeID and index position. If the trigger exists, the
     * trigger is updated. Otherwise the trigger is created.
     *
     * @param _instance   type instance to update with this attribute
     * @param _typeName   name of the type to update
     * @return Instance of the updated or inserted Trigger, null in case of
     *         error
     */
    public Instance updateInDB(final Instance _instance,
                               final String _typeName)
    {
        Instance ret = null;
        try {
            final long typeID = _instance.getId();
            final long progID = getProgID(_typeName);

            final QueryBuilder queryBldr = new QueryBuilder(Type.get(this.event.getName()));
            queryBldr.addWhereAttrEqValue("Abstract", typeID);
            queryBldr.addWhereAttrEqValue("Name", this.name);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();

            final Update update;
            if (query.next()) {
                update = new Update(query.getCurrentValue());
            } else {
                update = new Insert(this.event.getName());
                update.add("Abstract", typeID);
                update.add("IndexPosition", this.index);
                update.add("Name", this.name);
            }
            update.add("JavaProg", progID);
            update.add("Method", this.method);
            update.executeWithoutAccessCheck();

            ret = update.getInstance();
            update.close();
        } catch (final EFapsException e) {
            Event.LOG.error("updateInDB(Instance, String)", e);
            //CHECKSTYLE:OFF
        } catch (final Exception e) {
          //CHECKSTYLE:ON
            Event.LOG.error("updateInDB(Instance, String)", e);
        }
        return ret;
    }

    /**
     * Get the id of the program.
     *
     * @param _typeName     name of the type
     * @return id of the program, 0 if not found
     * @throws EFapsException if id of the program could not be fetched
     */
    private long getProgID(final String _typeName)
        throws EFapsException
    {
        long id = 0;
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.Java);
        queryBldr.addWhereAttrEqValue(CIAdminProgram.Java.Name, this.program);
        final InstanceQuery query = queryBldr.getQuery();
        query.executeWithoutAccessCheck();
        if (query.next()) {
            id = query.getCurrentValue().getId();
        } else {
            Event.LOG.error("type[" + _typeName + "]." + "Program [" + this.program + "]: " + "' not found");
        }
        return id;
    }

    /**
     * Adds a property to this event.
     *
     * @param _name     name of the property
     * @param _value    value of the property
     * @see #properties
     */
    public void addProperty(final String _name,
                            final String _value)
    {
        this.properties.put(_name, _value);
    }

    /**
     * Returns all properties of this event.
     *
     * @return Map containing the properties
     * @see #properties
     */
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    /**
     * Adds the properties overwrite.
     *
     * @param _sytemConfig the sytem config
     * @param _attribute the attribute
     */
    public void addPropertiesOverwrite(final String _sytemConfig,
                                       final String _attribute)
    {
        this.properties.put(Overwrite.SYSTEMCONFIG.value(), _sytemConfig);
        this.properties.put(Overwrite.ATTRIBUTE.value(), _attribute);
    }
}

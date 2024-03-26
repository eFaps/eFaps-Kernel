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
package org.efaps.admin;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.api.IEnumValue;
import org.efaps.api.datamodel.Overwrite;
import org.efaps.db.Context;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 *
 */
public abstract class AbstractAdminObject
    implements CacheObjectInterface, Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdminObject.class);

    /**
     * Select statement to get the properties.
     */
    private static final String SELECT = new SQLSelect()
                    .column("NAME")
                    .column("VALUE")
                    .from("T_CMPROPERTY")
                    .addPart(SQLPart.WHERE)
                    .addColumnPart(null, "ABSTRACT")
                    .addPart(SQLPart.EQUAL)
                    .addValuePart("?").toString();

    /**
     * The instance variable stores the id of the collections object.
     *
     * @see #getId
     */
    private final long id;

    /**
     * This is the instance variable for the universal unique identifier of this
     * admin object.
     *
     * @see #getUUID
     */
    private final UUID uuid;

    /**
     * This is the instance variable for the name of this admin object.
     *
     * @see #setName
     * @see #getName
     */
    private final String name;

    /**
     * This is the instance variable for the properties.
     *
     * @getProperties
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * All events for this AdminObject are stored in this map.
     */
    private final Map<EventType, List<EventDefinition>> events = new HashMap<>();

    /**
     * checked for events?
     */
    private boolean eventChecked = false;

    /**
     * Is this Admin Object dirty, meaning was it altered after first initialization
     * and it might be necessary to cache it again.
     */
    private boolean dirty = false;


    /**
     * Constructor to set instance variables {@link #id}, {@link #uuid} and
     * {@link #name} of this administrational object.
     *
     * @param _id id to set
     * @param _uuid universal unique identifier
     * @param _name name to set
     * @see #id
     * @see #uuid
     * @see #name
     */
    protected AbstractAdminObject(final long _id,
                                  final String _uuid,
                                  final String _name)
    {
        id = _id;
        uuid = _uuid == null || _uuid.trim().isEmpty() ? null : UUID.fromString(_uuid.trim());
        name = _name == null ? null : _name.trim();
    }

    /**
     * Sets the link properties for this object.
     *
     * @param _linkTypeUUID UUID of the type of the link property
     * @param _toId to id
     * @param _toTypeUUID UUDI of the to type
     * @param _toName to name
     * @throws EFapsException on error
     */
    protected void setLinkProperty(final UUID _linkTypeUUID,
                                   final long _toId,
                                   final UUID _toTypeUUID,
                                   final String _toName)
        throws EFapsException
    {
        setDirty();
    }

    /**
     * The instance method sets all properties of this administrational object.
     * All properties are stores in instance variable {@link #properties}.
     *
     * @param _name name of the property (key)
     * @param _value value of the property
     * @see #properties
     * @throws CacheReloadException on error
     */
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        getProperties().put(_name, _value);
        setDirty();
    }

    /**
     * The value of the given property is returned.
     *
     * @param _name name of the property (key)
     * @return value of the property with the given name / key.
     * @see #properties
     */
    public String getProperty(final String _name)
    {
        return evalProperties().get(_name);
    }

    /**
     * The value of the given property is returned.
     *
     * @param _enum the enum
     * @return value of the property with the given name / key.
     * @see #properties
     */
    public String getProperty(final IEnumValue _enum)
    {
        return getProperty(_enum.value());
    }

    /**
     * The value of the given property is returned.
     *
     * @param _name name of the property (key)
     * @return value of the property with the given name / key.
     * @see #properties
     */
    public boolean containsProperty(final String _name)
    {
        return evalProperties().containsKey(_name);
    }

    /**
     * The value of the given property is returned.
     *
     * @param _enum the enum
     * @return value of the property with the given name / key.
     * @see #properties
     */
    public boolean containsProperty(final IEnumValue _enum)
    {
        return containsProperty(_enum.value());
    }

    /**
     * @return unmodifiable map of properties
     */
    public Map<String, String> getPropertyMap()
    {
        return MapUtils.unmodifiableMap(evalProperties());
    }

    /**
     * Adds a new event to this AdminObject.
     *
     * @param _eventtype Eventtype class name to add
     * @param _eventdef EventDefinition to add
     * @see #events
     * @throws CacheReloadException on error
     */
    public void addEvent(final EventType _eventtype,
                         final EventDefinition _eventdef)
        throws CacheReloadException
    {
        List<EventDefinition> evenList = events.get(_eventtype);
        if (evenList == null) {
            evenList = new ArrayList<>();
            events.put(_eventtype, evenList);
        }
        if (!evenList.contains(_eventdef)) {
            evenList.add(_eventdef);
        }
        // if there are more than one event they must be sorted by their index
        // position
        if (evenList.size() > 1) {
            Collections.sort(evenList, (_eventDef0, _eventDef1) -> Long.compare(_eventDef0.getIndexPos(), _eventDef1.getIndexPos()));
        }
        setDirty();
    }

    /**
     * Returns the list of events defined for given event type.
     *
     * @param _eventType event type
     * @return list of events for the given event type
     */
    public List<EventDefinition> getEvents(final EventType _eventType)
    {
        if (!eventChecked) {
            eventChecked = true;
            try {
                EventDefinition.addEvents(this);
            } catch (final EFapsException e) {
                AbstractAdminObject.LOG.error("Could not read events for Name:; {}', UUID: {}",  name, uuid);
            }
        }
        return events.get(_eventType);
    }

    /**
     * Does this instance have Event, for the specified EventType ?
     *
     * @param _eventtype type of event to check for
     * @return <i>true</i>, if this instance has a trigger, otherwise
     *         <i>false</i>.
     */
    public boolean hasEvents(final EventType _eventtype)
    {
        if (!eventChecked) {
            eventChecked = true;
            try {
                EventDefinition.addEvents(this);
            } catch (final EFapsException e) {
                AbstractAdminObject.LOG.error("Could not read events for Name:; {}', UUID: {}",  name, uuid);
            }
        }
        return events.get(_eventtype) != null;
    }

    /**
     * The method gets all events for the given event type and executes them in
     * the given order. If no events are defined, nothing is done.
     *
     * @param _eventtype type of event to execute
     * @param _args arguments used as parameter (doubles with first parameters
     *            defining the key, second parameter the value itself)
     * @return List with Returns
     * @throws EFapsException on error
     */
    public List<Return> executeEvents(final EventType _eventtype,
                                      final Object... _args)
        throws EFapsException
    {
        final List<Return> ret = new ArrayList<>();
        if (hasEvents(_eventtype)) {
            final Parameter param = new Parameter();
            if (_args != null) {
                // add all parameters
                for (int i = 0; i < _args.length; i += 2) {
                    if (i + 1 < _args.length && _args[i] instanceof ParameterValues) {
                        param.put((ParameterValues) _args[i], _args[i + 1]);
                    }
                }
            }
            ret.addAll(executeEvents(_eventtype, param));
        }
        return ret;
    }

    /**
     * The method gets all events for the given event type and executes them in
     * the given order. If no events are defined, nothing is done.
     *
     * @param _eventtype type of event to execute
     * @param _param Parameter to be passed to the esjp
     * @return List with Returns
     * @throws EFapsException on error
     */
    public List<Return> executeEvents(final EventType _eventtype,
                                      final Parameter _param)
        throws EFapsException
    {
        final List<Return> ret = new ArrayList<>();
        if (hasEvents(_eventtype)) {
            if (this instanceof AbstractUserInterfaceObject && _param.get(ParameterValues.UIOBJECT) == null) {
                // add ui object to parameter
                _param.put(ParameterValues.UIOBJECT, this);
            }
            // execute all triggers
            for (final EventDefinition evenDef : events.get(_eventtype)) {
                ret.add(evenDef.execute(_param));
            }
        }
        return ret;
    }

    /**
     * The instance method reads the properties for this administration object.
     * Each found property is set with instance method {@link #setProperty}.
     *
     * @throws CacheReloadException on error
     *
     * @see #setProperty
     */
    protected void readFromDB4Properties()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            con = Context.getConnection();
            final PreparedStatement stmt = con.prepareStatement(AbstractAdminObject.SELECT);
            stmt.setObject(1, getId());
            final ResultSet rs = stmt.executeQuery();
            AbstractAdminObject.LOG.debug("Reading Properties for '{}'", getName());
            while (rs.next()) {
                final String nameStr = rs.getString(1).trim();
                final String value = rs.getString(2).trim();
                setProperty(nameStr, value);
                AbstractAdminObject.LOG.debug("    Name: '{}' - Value: '{}'", new Object[] { nameStr, value });
            }
            rs.close();
            stmt.close();
            con.commit();
            con.close();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read properties for " + "'" + getName() + "'", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read properties for " + "'" + getName() + "'", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Reads all links for this administration object. Each found link property
     * is set with instance method {@link setLinkProperty}.
     *
     * @throws CacheReloadException on error
     * @see #setLinkProperty
     */
    protected void readFromDB4Links()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            con = Context.getConnection();
            final SQLSelect select = new SQLSelect()
                            .column(0, "TYPEID")
                            .column(0, "TOID")
                            .column(1, "TYPEID")
                            .column(1, "NAME")
                            .from("T_CMABSTRACT2ABSTRACT", 0)
                            .leftJoin("T_CMABSTRACT", 1, "ID", 0, "TOID")
                            .addPart(SQLPart.WHERE)
                            .addColumnPart(0, "FROMID")
                            .addPart(SQLPart.EQUAL)
                            .addValuePart(getId());

            final Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery(select.getSQL());

            AbstractAdminObject.LOG.debug("Reading Links for '{}'", getName());

            final List<Object[]> values = new ArrayList<>();
            while (rs.next()) {
                final long conTypeId = rs.getLong(1);
                final long toId = rs.getLong(2);
                final long toTypeId = rs.getLong(3);
                final String toName = rs.getString(4);
                values.add(new Object[] { conTypeId, toId, toTypeId, toName.trim() });
            }
            rs.close();
            stmt.close();
            con.commit();
            con.close();
            for (final Object[] row : values) {
                final UUID conTypeUUID = Type.getUUID4Id((Long) row[0]);
                AbstractAdminObject.LOG.debug("     Connection Type UUID: {}", conTypeUUID);
                final UUID toTypeUUID = Type.getUUID4Id((Long) row[2]);
                AbstractAdminObject.LOG.debug("     To Type UUID: {}", toTypeUUID);
                if (conTypeUUID != null && toTypeUUID != null) {
                    setLinkProperty(conTypeUUID, (Long) row[1], toTypeUUID, String.valueOf(row[3]));
                    AbstractAdminObject.LOG.debug("     ID: {}, name: {}", row[1], row[3]);
                }
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read db links for " + "'" + getName() + "'", e);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException e) {
            // CHECKSTYLE:ON
            throw new CacheReloadException("could not read db links for " + "'" + getName() + "'", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read properties for " + "'" + getName() + "'", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * This is the getter method for instance variable {@link #id}.
     *
     * @return value of instance variable {@id}
     * @see #id
     */
    @Override
    public long getId()
    {
        return id;
    }

    /**
     * This is the getter method for instance variable {@link #uuid}.
     *
     * @return value of instance variable {@uuid}
     * @see #uuid
     */
    @Override
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     * This is the getter method for instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     * @see #name
     * @see #setName
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * This is the getter method for instance variable {@link #properties}.
     *
     * @return value of instance variable {@link #properties}
     * @see #properties
     */
    protected Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * This is the getter method for instance variable {@link #properties}.
     *
     * @return value of instance variable {@link #properties}
     *
     */
    protected Map<String, String> evalProperties()
    {
        final Map<String, String> ret = getProperties();
        try {
            if (ret.containsKey(Overwrite.SYSTEMCONFIG.value())) {
                final String sysConfStr = ret.get(Overwrite.SYSTEMCONFIG.value());
                final SystemConfiguration sysConfig;
                if (UUIDUtil.isUUID(sysConfStr)) {
                    sysConfig = SystemConfiguration.get(UUID.fromString(sysConfStr));
                } else {
                    sysConfig = SystemConfiguration.get(sysConfStr);
                }
                if (sysConfig != null) {
                    final Properties props = sysConfig.getAttributeValueAsProperties(ret.get(Overwrite.ATTRIBUTE
                                    .value()));
                    ret.putAll(props.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e
                                    .getValue().toString())));
                }
            }
        } catch (final EFapsException e) {
            AbstractAdminObject.LOG.error("Catched error on evaluation of Properties.", e);
        }
        return ret;
    }

    /**
     * This is the getter method for instance variable {@link #events}.
     *
     * @return value of instance variable {@link #events}
     * @see #events
     */
    protected Map<EventType, List<EventDefinition>> getEvents()
    {
        return events;
    }


    /**
     * Getter method for the instance variable {@link #dirty}.
     *
     * @return value of instance variable {@link #dirty}
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Declare Object as dirty.
     */
    protected void setDirty()
    {
        dirty = true;
    }

    /**
     * Declare Object as undirty.
     */
    protected void setUndirty()
    {
        dirty = false;
    }
    /**
     * The method overrides the original method 'toString' and returns the name
     * of the user interface object.
     *
     * @return name of the user interface object
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("name", getName()).append("uuid", getUUID()).append("id", getId())
                        .append("properties", getProperties()).append("events", events).toString();
    }
}

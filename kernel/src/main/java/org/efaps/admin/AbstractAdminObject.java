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

package org.efaps.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractAdminObject implements CacheObjectInterface
{

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

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
  private final Map<String, String> properties = new HashMap<String, String>();

  /**
   * All events for this AdminObject are stored in this map.
   */
  private final Map<EventType, List<EventDefinition>> events =
      new HashMap<EventType, List<EventDefinition>>();

  /**
   * this instance variable is used to determine if a Type is abstract or not
   */
  private boolean abstractType = false;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to set instance variables {@link #id}, {@link #uuid} and
   * {@link #name} of this administrational object.
   *
   * @param _id     id to set
   * @param _uuid   universal unique identifier
   * @param _name   name to set
   * @see #id
   * @see #uuid
   * @see #name
   */
  protected AbstractAdminObject(final long _id,
                                final String _uuid,
                                final String _name)
  {
    this.id = _id;
    this.uuid = (_uuid == null)
                ? null
                : UUID.fromString(_uuid.trim());
    this.name = (_name == null)
                ? null
                : _name.trim();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Sets the link properties for this object.
   *
   * @param _linkType   type of the link property
   * @param _toId       to id
   * @param _toType     to type
   * @param _toName     to name
   */
  protected void setLinkProperty(final EFapsClassNames _linkType,
                                 final long _toId,
                                 final EFapsClassNames _toType,
                                 final String _toName)
      throws Exception
  {
  }

  /**
   * The instance method sets all properties of this administrational object.
   * All properties are stores in instance variable {@link #properties}.
   *
   * @param _name   name of the property (key)
   * @param _value  value of the property
   * @see #properties
   */
  protected void setProperty(final String _name,
                             final String _value)
      throws CacheReloadException
  {
    getProperties().put(_name, _value);
  }

  /**
   * The value of the given property is returned.
   *
   * @param _name   name of the property (key)
   * @return value of the property with the given name / key.
   * @see #properties
   */
  public String getProperty(final String _name)
  {
    return getProperties().get(_name);
  }

  /**
   * Adds a new event to this AdminObject.
   *
   * @param _eventtype  Eventtype class name to add
   * @param _eventdef   EventDefinition to add
   * @see #events
   */
  public void addEvent(final EventType _eventtype,
                       final EventDefinition _eventdef)
  {
    List<EventDefinition> events = this.events.get(_eventtype);
    if (events == null) {
      events = new ArrayList<EventDefinition>();
      this.events.put(_eventtype, events);
    }
    int pos = 0;
    for (EventDefinition cur : events) {
      if (_eventdef.getIndexPos() > cur.getIndexPos()) {
        break;
      }
      pos++;
    }
    events.add(pos, _eventdef);

  }

  /**
   * Returns the list of events defined for given event type.
   *
   * @param _eventType    event type
   * @return list of events for the given event type
   */
  public List<EventDefinition> getEvents(final EventType _eventType)
  {
    return this.events.get(_eventType);
  }

  /**
   * Does this instance have Event, for the specified EventType ?
   *
   * @return <i>true</i>, if this instance has a trigger, otherwise
   *         <i>false</i>.
   */
  public boolean hasEvents(final EventType _eventtype)
  {
    return (this.events.get(_eventtype) != null);
  }

  /**
   * The method gets all events for the given event type and executes them in
   * the given order. If no events are defined, nothing is done.
   *
   * @param _eventtype  type of event to execute
   * @param _args       arguments used as parameter (doubles with first
   *                    parameters defining the key, second parameter the value
   *                    itself)
   * @return List with Returns
   * @throws EFapsException
   */
  public List<Return> executeEvents(final EventType _eventtype,
                                    final Object... _args)
      throws EFapsException
  {
    List<Return> ret = new ArrayList<Return>();
    if (hasEvents(_eventtype)) {
      Parameter param = new Parameter();

      if (_args != null) {
        // add all parameters
        for (int i = 0; i < _args.length; i += 2) {
          if (((i + 1) < _args.length) && (_args[i] instanceof ParameterValues)) {
            param.put((ParameterValues) _args[i], _args[i + 1]);
          }
        }
      }
      if (this instanceof AbstractUserInterfaceObject) {
        // add ui object to parameter
        param.put(ParameterValues.UIOBJECT, this);
      }
      // execute all triggers
      for (EventDefinition evenDef : this.events.get(_eventtype)) {
        ret.add(evenDef.execute(param));
      }
    }
    return ret;
  }

  /**
   * The instance method reads the properties for this administration object.
   * Each found property is set with instance method {@link #setProperty}.
   *
   * @see #setProperty
   */
  protected void readFromDB4Properties()
      throws CacheReloadException
  {
    Statement stmt = null;
    try  {
      stmt = Context.getThreadContext().getConnection().createStatement();
      final ResultSet rs = stmt.executeQuery(
          "select "+
            "T_CMPROPERTY.NAME,"+
            "T_CMPROPERTY.VALUE "+
          "from T_CMPROPERTY "+
          "where T_CMPROPERTY.ABSTRACT=" + getId() + ""
      );
      while (rs.next())  {
        final String name =   rs.getString(1).trim();
        final String value =  rs.getString(2).trim();
        setProperty(name, value);
      }
      rs.close();
    } catch (EFapsException e)  {
      throw new CacheReloadException("could not read properties for "
          + "'" + getName() + "'", e);
    } catch (SQLException e)  {
      throw new CacheReloadException("could not read properties for "
          + "'" + getName() + "'", e);
    } finally  {
      if (stmt != null)  {
        try  {
          stmt.close();
        } catch (SQLException e)  {
        }
      }
    }
  }

  /**
   * Reads all links for this administration object. Each found link property
   * is set with instance method {@link setLinkProperty}.
   *
   * @see #setLinkProperty
   */
  protected void readFromDB4Links()
      throws CacheReloadException
  {
    Statement stmt = null;
    try {
      stmt = Context.getThreadContext().getConnection().createStatement();
      final ResultSet resultset = stmt.executeQuery(
          "select "
              + "T_CMABSTRACT2ABSTRACT.TYPEID,"
              + "T_CMABSTRACT2ABSTRACT.TOID,"
              + "T_CMABSTRACT.TYPEID,"
              + "T_CMABSTRACT.NAME "
          + "from T_CMABSTRACT2ABSTRACT, T_CMABSTRACT "
          + "where T_CMABSTRACT2ABSTRACT.FROMID=" + getId()
              + " and T_CMABSTRACT2ABSTRACT.TOID=T_CMABSTRACT.ID");
      while (resultset.next()) {
        final long conTypeId = resultset.getLong(1);
        final long toId = resultset.getLong(2);
        final long toTypeId = resultset.getLong(3);
        final String toName = resultset.getString(4);
        final Type conType = Type.get(conTypeId);
        final Type toType = Type.get(toTypeId);
        if (EFapsClassNames.getEnum(conType.getUUID()) != null)  {
          setLinkProperty(EFapsClassNames.getEnum(conType.getUUID()),
                          toId,
                          EFapsClassNames.getEnum(toType.getUUID()),
                          toName.trim());
        }
      }
      resultset.close();
    } catch (Exception e) {
      throw new CacheReloadException("could not read db links for "
          + "'" + getName() + "'", e);
    }
    finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods

  /**
   * This is the getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@id}
   * @see #id
   */
  public long getId()
  {
    return this.id;
  }

  /**
   * This is the getter method for instance variable {@link #uuid}.
   *
   * @return value of instance variable {@uuid}
   * @see #uuid
   */
  public UUID getUUID()
  {
    return this.uuid;
  }

  /**
   * This is the getter method for instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   * @see #name
   * @see #setName
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * This is the getter method for the instance variable {@link #abstractType}.
   *
   * @return value of instance variable {@link #abstractType}
   */
  public boolean isAbstractType()
  {
    return this.abstractType;
  }

  /**
   * This is the setter method for the instance variable {@link #abstractType}.
   *
   * @param abstractType
   *                the abstractType to set
   */
  protected void setAbstractType(final boolean abstractType)
  {
    this.abstractType = abstractType;
  }

  /**
   * This is the getter method for instance variable {@link #properties}.
   *
   * @return value of instance variable {@link #properties}
   * @see #properties
   */
  protected Map<String, String> getProperties()
  {
    return this.properties;
  }

  /**
   * This is the getter method for instance variable {@link #events}.
   *
   * @return value of instance variable {@link #events}
   * @see #events
   */
  protected Map<EventType, List<EventDefinition>> getEvents()
  {
    return this.events;
  }

  /**
   * The method overrides the original method 'toString' and returns the name of
   * the user interface object.
   *
   * @return name of the user interface object
   */
  @Override
  public String toString()
  {
    return new ToStringBuilder(this)
                .append("name", getName())
                .append("uuid", getUUID())
                .append("id", getId())
                .append("properties", getProperties())
                .append("events", this.events)
                .toString();
  }

}

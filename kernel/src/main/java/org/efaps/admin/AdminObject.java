/*
 * Copyright 2003-2007 The eFaps Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.UserInterfaceObject;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class AdminObject implements CacheObjectInterface {

  /**
   *
   */
  public enum EFapsClassName {

    ATTRTYPE_LINK("Link", "440f472f-7be2-41d3-baec-4a2f0e4e5b31"),
    ATTRTYPE_LINK_WITH_RANGES(
                              "LinkWithRanges",
                              "9d6b2e3e-68ce-4509-a5f0-eae42323a696"),
    ATTRTYPE_CREATOR_LINK("CreatorLink", "76122fe9-8fde-4dd4-a229-e48af0fb4083"),
    ATTRTYPE_MODIFIER_LINK(
                           "ModifierLink",
                           "447a7c87-8395-48c4-b2ed-d4e96d46332c"),

    DATAMODEL_TYPE("Admin_DataModel_Type", null),
    DATAMODEL_ATTRIBUTE("Admin_DataModel_Attribute", null),

    USER_ABSTRACT("Admin_User_Abstract", null),
    USER_PERSON("Admin_User_Person", null),
    USER_ROLE("Admin_User_Role", null),
    USER_GROUP("Admin_User_Group", null),
    USER_JAASKEY("Admin_User_JAASKey", null),
    USER_JAASSYSTEM("Admin_User_JAASSystem", null),

    USER_ABSTRACT2ABSTRACT("Admin_User_Abstract2Abstract", null),
    USER_PERSON2ROLE("Admin_User_Person2Role", null),
    USER_PERSON2GROUP("Admin_User_Person2Group", null),

    EVENT_DEFINITION("Admin_Event_Definition", null),

    COLLECTION("Admin_UI_Collection", null),
    FIELD("Admin_UI_Field", null),
    FORM("Admin_UI_Form", null),
    TABLE("Admin_UI_Table", null),
    COMMAND("Admin_UI_Command", null),
    MENU("Admin_UI_Menu", null),
    SEARCH("Admin_UI_Search", null),
    IMAGE("Admin_UI_Image", null),

    LINK_ICON("Admin_UI_LinkIcon", null),
    LINK_TARGET_FORM("Admin_UI_LinkTargetForm", null),
    LINK_TARGET_MENU("Admin_UI_LinkTargetMenu", null),
    LINK_TARGET_SEARCH("Admin_UI_LinkTargetSearch", null),
    LINK_TARGET_TABLE("Admin_UI_LinkTargetTable", null);

    public final String name;

    public final UUID uuid;

    private EFapsClassName(final String _name, final String _uuid) {
      this.name = _name;
      this.uuid = (_uuid == null) ? null : UUID.fromString(_uuid);
      mapper.put(this.name, this);
    }

    static public EFapsClassName getEnum(final String _name) {
      return mapper.get(_name);
    }
  }

  static private Map<String, EFapsClassName> mapper =
      new HashMap<String, EFapsClassName>();

  // ///////////////////////////////////////////////////////////////////////////
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
  private String name = null;

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

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to set instance variables {@link #id}, {@link #uuid} and
   * {@link #name} of this administrational object.
   * 
   * @param _id
   *          id to set
   * @param _uuid
   *          universal unique identifier
   * @param _name
   *          name to set
   * @see #id
   * @see #uuid
   * @see #name
   */
  protected AdminObject(final long _id, final String _uuid, final String _name) {
    this.id = _id;
    this.uuid = (_uuid == null) ? null : UUID.fromString(_uuid.trim());
    setName(_name);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Sets the link properties for this object.
   * 
   * @param _linkType
   *          type of the link property
   * @param _toId
   *          to id
   * @param _toType
   *          to type
   * @param _toName
   *          to name
   */
  protected void setLinkProperty(final EFapsClassName _linkType,
      final long _toId, final EFapsClassName _toType, final String _toName)
      throws Exception {
  }

  /**
   * The instance method sets all properties of this administrational object.
   * All properties are stores in instance variable {@link #properties}.
   * 
   * @param _name
   *          name of the property (key)
   * @param _value
   *          value of the property
   * @see #properties
   */
  protected void setProperty(final String _name, final String _value)
      throws CacheReloadException {
    getProperties().put(_name, _value);
  }

  /**
   * The value of the given property is returned.
   * 
   * @see #properties
   * @param _name
   *          name of the property (key)
   * @return value of the property with the given name / key.
   */
  public String getProperty(final String _name) {
    return getProperties().get(_name);
  }

  /**
   * The method overrides the original method 'toString' and returns the name of
   * the user interface object.
   * 
   * @return name of the user interface object
   */
  public String toString() {
    return new ToStringBuilder(this).append("name", getName()).append("uuid",
        getUUID()).append("id", getId()).append("properties", getProperties())
        .toString();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods

  /**
   * This is the getter method for instance variable {@link #id}.
   * 
   * @return value of instance variable {@id}
   * @see #id
   */
  public long getId() {
    return this.id;
  }

  /**
   * This is the getter method for instance variable {@link #uuid}.
   * 
   * @return value of instance variable {@uuid}
   * @see #uuid
   */
  public UUID getUUID() {
    return this.uuid;
  }

  /**
   * This is the setter method for instance variable {@link #name}.
   * 
   * @param _name
   *          new value for instance variable {@link #name}
   * @see #name
   * @see #getName
   */
  protected void setName(final String _name) {
    this.name = (_name == null) ? null : _name.trim();
  }

  /**
   * This is the getter method for instance variable {@link #name}.
   * 
   * @return value of instance variable {@name}
   * @see #name
   * @see #setName
   */
  public String getName() {
    return this.name;
  }

  /**
   * This is the getter method for instance variable {@link #properties}.
   * 
   * @return value of instance variable {@properties}
   * @see #properties
   */
  protected Map<String, String> getProperties() {
    return this.properties;
  }

  /**
   * Adds a new event to this AdminObject.
   * 
   * @see #events
   * @param _eventtype
   *          Eventtype class name to add
   * @param _eventdef
   *          EventDefinition to add
   */
  public void addEvent(final EventType _eventtype,
      final EventDefinition _eventdef) {
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
   * returns the Map with all events for this instance
   * 
   * @see #events
   * @return
   */
  protected Map<EventType, List<EventDefinition>> getEvents() {
    return this.events;
  }

  /**
   * Returns the list of events defined for given event type.
   * 
   * @param _eventType
   *          event type
   * @return list of events for the given event type
   */
  public List<EventDefinition> getEvents(final EventType _eventType) {
    return this.events.get(_eventType);
  }

  /**
   * does this instance have Events?
   * 
   * @see #events
   * @return true, if this instance has a event, otherwise false
   */
  public boolean hasEvents() {
    return !this.events.isEmpty();
  }

  /**
   * does this instance have Event, for the specified EventType ?
   * 
   * @return true, if this instance has a trigger, otherwise false
   */
  public boolean hasEvents(final EventType _eventtype) {
    boolean has = false;
    if (!this.events.isEmpty()) {
      has = this.events.get(_eventtype) != null;
    }
    return has;
  }

  /**
   * The method gets all events for the given event type and executes them in
   * the given order. If no events are defined, nothing is done.
   * 
   * @param _eventtype
   *          type of event to execute
   * @param _args
   *          arguments used as parameter (doubles with first parameters
   *          defining the key, second parameter the value itself)
   * @return List with Returns
   */
  public List<Return> executeEvents(final EventType _eventtype,
      final Object... _args) {

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
      if (this instanceof UserInterfaceObject) {
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
}

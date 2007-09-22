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

package org.efaps.update.event;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.event.EventType;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * This class defines a Event to be connected with a Update
 * 
 * @author jmo
 * @version $Id$
 * 
 */
public class Event {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(Event.class);

  /**
   * Property value depending on the property name for this Trigger
   * 
   * @see #addProperty.
   */
  private final Map<String, String> properties = new HashMap<String, String>();

  /**
   * event as defined in {@link org.efaps.admin.event.EventType}
   */
  private final String              event;

  /**
   * name of the programm invoked in this trigger
   */
  private final String              program;

  /**
   * name of the method to be invoked by tihs trigger
   */
  private final String              method;

  /**
   * index of the trigger
   */
  private final String              index;

  /**
   * name of the Trigger
   */
  private final String              name;

  private boolean                   isTrigger  = false;

  public void setTrigger(final boolean _isTrigger) {
    isTrigger = _isTrigger;
  }

  /**
   * Constructor of Event for a Trigger setting all instancevariables
   * 
   * @param _name
   *          name of the Event
   * @param _event
   *          event as defined in {@link org.efaps.admin.event.EventType}
   * @param _program
   *          name of the programm invoked in this trigger
   * @param _method
   *          name of the method to be invoked by tihs trigger
   * @param _index
   *          index of the trigger
   */
  public Event(final String _name, final String _event, final String _program,
      final String _method, final String _index) {
    if (_name == null)  {
      this.name = _event;
    } else  {
      this.name = _name;
    }
    this.event = _event;
    this.program = _program;
    this.method = _method;
    this.index = _index;
  }

  /**
   * For given type defined with the instance parameter, this trigger is
   * searched by typeID and indexposition. If the trigger exists, the trigger is
   * updated. Otherwise the trigger is created.
   * 
   * @param _instance
   *          type instance to update with this attribute
   * @param _typeName
   *          name of the type to update
   * @return Instance of the updateted or inserted Trigger, null incase of error
   * 
   * 
   */
  public Instance updateInDB(final Instance _instance, final String _typeName) {

    try {

      String eventtype;
      if (isTrigger) {
        eventtype = EventType.valueOf(this.event).name;
      } else {
        eventtype = this.event;
      }

      long typeID = _instance.getId();
      long progID = getProgID(_typeName);

      SearchQuery query = new SearchQuery();
      query.setQueryTypes(eventtype);
      query.addWhereExprEqValue("Abstract", typeID);
      query.addWhereExprEqValue("Name", this.name);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();

      Update update;

      if (query.next()) {
        update = new Update((String) query.get("OID"));
      } else {

        update = new Insert(eventtype);
        update.add("Abstract", "" + typeID);
        update.add("IndexPosition", this.index);
        update.add("Name", this.name);
      }
      query.close();
      update.add("JavaProg", "" + progID);
      update.add("Method", this.method);
      update.executeWithoutAccessCheck();

      Instance instance = update.getInstance();
      update.close();
      return instance;
    } catch (EFapsException e) {
      LOG.error("updateInDB(Instance, String)", e);
    } catch (Exception e) {
      LOG.error("updateInDB(Instance, String)", e);
    }
    return null;

  }

  /**
   * get the ID of the Program
   * 
   * @param _typeName
   *          Name of teh Type
   * @return id of the Program, 0 if not found
   * @throws EFapsException
   */
  private long getProgID(String _typeName) throws EFapsException {
    long id = 0;

    SearchQuery query = new SearchQuery();
    query.setQueryTypes("Admin_Program_Java");
    query.addSelect("ID");
    query.addWhereExprEqValue("Name", this.program);

    query.executeWithoutAccessCheck();
    if (query.next()) {
      id = (Long) query.get("ID");
    } else {
      LOG.error("type[" + _typeName + "]." + "Program [" + this.program + "]: "
          + "' not found");
    }
    return id;
  }

  /**
   * add a Property to this Trigger
   * 
   * @param _name
   *          Name of the Property
   * @param _value
   *          value of the Property
   */
  public void addProperty(final String _name, final String _value) {
    this.properties.put(_name, _value);
  }

  /**
   * get the properties of this Trigger
   * 
   * @return Map containing the Properties
   */
  public Map<String, String> getProperties() {
    return this.properties;
  }
}

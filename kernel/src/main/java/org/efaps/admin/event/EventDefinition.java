/*
 * Copyright 2003 - 2007 The eFaps Team
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.admin.AdminObject;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.java.EFapsClassLoader;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * In this Class a Event can be defined. <br/> On loading the Cache all
 * EventDefenitions are initialised and assigned to the specific
 * administrational type or command. On initialisation of a EventDefinition, for
 * faster access during runtime, the Class of the Program is instanciated and
 * the Method stored.
 * 
 * @author tmo
 * @author jmo
 * @version $Id$
 */
public class EventDefinition extends AdminObject implements EventExecution {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(EventDefinition.class);

  /**
   * The variable stores the position in a event pool (more than one event
   * definition for one thrown event.
   * 
   * @see #getIndexPos
   */
  private final long indexPos;

  /**
   * The variable stores the Name of the JavaClass
   */
  private final String resourceName;

  /**
   * The variable stores the Name of the method to be invoked
   */
  private final String methodName;

  /**
   * The variable stores the Method to be invoked
   */
  private Method method = null;

  /**
   * holds the instance of this
   */
  private EventExecution progInstance = null;

  /**
   * constructor
   */
  private EventDefinition(final long _id, final String _name,
                          final long _indexPos, final String _resourceName,
                          final String _method, final String _oid) {
    super(_id, null, _name);
    this.indexPos = _indexPos;
    this.resourceName = _resourceName;
    this.methodName = _method;
    setInstance();
    setProperties(_oid);
  }

  /**
   * set the properties in the superclass
   * 
   * @param _oid
   *          OID of thie EventDefenitoin
   */
  private void setProperties(final String _oid) {
    SearchQuery query = new SearchQuery();
    try {
      query.setExpand(_oid, "Admin_Property\\Abstract");
      query.addSelect("Name");
      query.addSelect("Value");
      query.executeWithoutAccessCheck();

      while (query.next()) {
        super.setProperty((String) query.get("Name"), (String) query
            .get("Value"));
      }
    } catch (EFapsException e) {
      LOG.error("setProperties(String)", e);
    } catch (CacheReloadException e) {
      LOG.error("setProperties(String)", e);
    }

  }

  public String getViewableName(Context _context) {
    return null;
  }

  /**
   * This is the getter method for instance variable {@link #indexPos}.
   * 
   * @return value of instance variable {@link #indexPos}
   * @see #indexPos
   */
  public long getIndexPos() {
    return this.indexPos;
  }

  /**
   * This is the getter method for instance variable {@link #resourceName}.
   * 
   * @return value of instance variable {@link #resourceName}
   * @see #resourceName
   */
  public String getResourceName() {
    return this.resourceName;
  }

  private void setInstance() {
    try {
      Class cls =
          Class.forName(this.resourceName, true, new EFapsClassLoader(this
              .getClass().getClassLoader()));
      this.method =
          cls.getMethod(this.methodName, new Class[] { Parameter.class });
      this.progInstance = ((EventExecution) cls.newInstance());
    } catch (ClassNotFoundException e) {
      LOG.error("could not find Class: '" + this.resourceName + "'", e);
    } catch (InstantiationException e) {
      LOG.error("could not instantiat Class: '" + this.resourceName + "'", e);
    } catch (IllegalAccessException e) {
      LOG.error("could not access Class: '" + this.resourceName + "'", e);
    } catch (SecurityException e) {
      LOG.error("could not access Class: '" + this.resourceName + "'", e);
    } catch (NoSuchMethodException e) {
      LOG.error("could not find method: '" + this.methodName + "' in class: '"
          + this.resourceName + "'", e);

    }

  }

  public Return execute(final Parameter _parameter) {
    Return ret = null;
    _parameter.put(ParameterValues.PROPERTIES, super.getProperties());
    try {

      ret = (Return) this.method.invoke(this.progInstance, _parameter);

    } catch (SecurityException e) {
      LOG.error("could not access class: '" + this.resourceName, e);
    } catch (IllegalArgumentException e) {
      LOG.error("execute(Context, Instance, Map<TriggerKeys4Values,Map>)", e);
    } catch (IllegalAccessException e) {
      LOG.error("could not access class: '" + this.resourceName, e);
    } catch (InvocationTargetException e) {
      LOG.error("could not invoke method: '" + this.methodName
          + "' in class: '" + this.resourceName, e);
    }
    return ret;

  }

  /**
   * Loads all events from the database and assigns them to the specific
   * administrational type or command
   * 
   * @param _context
   *          eFaps context for this request
   */
  public static void initialise() throws Exception {
    SearchQuery query = new SearchQuery();
    query.setQueryTypes(EFapsClassName.EVENT_DEFINITION.name);
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

    if (LOG.isDebugEnabled()) {
      LOG.debug("initialise Triggers ---------------------------------------");
    }
    while (query.next()) {
      String eventOID = (String) query.get("OID");
      long eventId = (Long) query.get("ID");
      Type eventType = (Type) query.get("Type");
      String eventName = (String) query.get("Name");
      long eventPos = (Long) query.get("IndexPosition");
      long abstractID = (Long) query.get("Abstract");
      Long programId = (Long) query.get("JavaProg");
      String method = (String) query.get("Method");

      String resName = getClassName(programId.toString());

      if (LOG.isDebugEnabled()) {
        LOG.debug("   OID=" + eventOID);
        LOG.debug("   eventId=" + eventId);
        LOG.debug("   eventType=" + eventType);
        LOG.debug("   eventName=" + eventName);
        LOG.debug("   eventPos=" + eventPos);
        LOG.debug("   parentId=" + abstractID);
        LOG.debug("   programId=" + programId);
        LOG.debug("   Method=" + method);
        LOG.debug("   resName=" + resName);
      }

      EFapsClassName eFapsClass =
          EFapsClassName.getEnum(getTypeName(abstractID));

      EventType triggerEvent = null;
      for (EventType trigger : EventType.values()) {
        Type triggerClass = Type.get(trigger.name);
        if (eventType.isKindOf(triggerClass)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("     found trigger " + trigger + ":" + triggerClass);
          }
          triggerEvent = trigger;
          break;
        }
      }

      if (eFapsClass == EFapsClassName.DATAMODEL_TYPE) {
        Type type = Type.get(abstractID);
        if (LOG.isDebugEnabled()) {
          LOG.debug("    type=" + type);
        }

        type.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == EFapsClassName.COMMAND) {
        Command command = Command.get(abstractID);

        if (LOG.isDebugEnabled()) {
          LOG.debug("    Command=" + command.getName());
        }
        command.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == EFapsClassName.FIELD) {

        Field field = Field.get(abstractID);

        if (LOG.isDebugEnabled()) {
          LOG.debug("       Field=" + field.getName());
        }

        field.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == EFapsClassName.DATAMODEL_ATTRIBUTE) {
        Attribute attribute = Attribute.get(abstractID);
        if (LOG.isDebugEnabled()) {
          LOG.debug("      Attribute=" + attribute.getName());
        }

        attribute.addEvent(triggerEvent, new EventDefinition(eventId,
            eventName, eventPos, resName, method, eventOID));

      } else if (LOG.isDebugEnabled()) {
        LOG.debug("initialise() - unknown event trigger connection");
      }
    }

  }

  /**
   * get the ClassName from the Database
   * 
   * @param _id
   *          ID of the Program the ClassName is searched for
   * @return ClassName
   */
  private static String getClassName(final String _id) {
    SearchQuery query = new SearchQuery();
    String Name = null;
    try {
      query.setQueryTypes("Admin_Program_Java");
      query.addSelect("Name");
      query.addWhereExprEqValue("ID", _id);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        Name = (String) query.get("Name");
      } else {
        LOG.error("Can't find the Name for the Program with ID: " + _id);
      }
    } catch (EFapsException e) {
      LOG.error("getClassName(String)", e);
    }
    return Name;
  }

  /**
   * get the Name of the Type from the Database
   * 
   * @param abstractID
   *          ID the Typename must be resolved
   * @return NAem of the Type
   */
  private static String getTypeName(final long abstractID) {
    SearchQuery query = new SearchQuery();
    Type type = null;
    try {
      query.setQueryTypes("Admin_Abstract");
      query.addSelect("Type");
      query.addWhereExprEqValue("ID", abstractID);
      query.setExpandChildTypes(true);
      query.executeWithoutAccessCheck();
      if (query.next()) {
        type = (Type) query.get("Type");
      } else {
        // wird gebraucht, da fuer Admin_Abstract die Query nicht funktioniert
        type = Type.get(abstractID);
      }
    } catch (EFapsException e) {
      LOG.error("getClassName(String)", e);
    }
    if (type == null) {
      LOG.error("Can't find the Type  with ID: " + abstractID);
    } else {
      return type.getName();
    }
    return null;
  }

}

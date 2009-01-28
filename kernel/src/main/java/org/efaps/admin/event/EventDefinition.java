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
import static org.efaps.admin.EFapsClassNames.FIELDTABLE;
import static org.efaps.admin.EFapsClassNames.MENU;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldTable;
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
 * @author jmox
 * @version $Id$
 */
public class EventDefinition extends AbstractAdminObject implements
    EventExecution {

  /**
   * Logger for this class
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(EventDefinition.class);

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
  private Object progInstance = null;

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
   *                OID of thie EventDefenitoin
   */
  private void setProperties(final String _oid) {
    final SearchQuery query = new SearchQuery();
    try {
      query.setExpand(_oid, "Admin_Common_Property\\Abstract");
      query.addSelect("Name");
      query.addSelect("Value");
      query.executeWithoutAccessCheck();

      while (query.next()) {
        super.setProperty((String) query.get("Name"), (String) query
            .get("Value"));
      }
    } catch (final EFapsException e) {
      LOG.error("setProperties(String)", e);
    } catch (final CacheReloadException e) {
      LOG.error("setProperties(String)", e);
    }

  }

  public String getViewableName(final Context _context) {
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
      final Class<?> cls =
          Class.forName(this.resourceName, true, new EFapsClassLoader(this
              .getClass().getClassLoader()));
      this.method =
          cls.getMethod(this.methodName, new Class[] { Parameter.class });
      this.progInstance = cls.newInstance();
    } catch (final ClassNotFoundException e) {
      LOG.error("could not find Class: '" + this.resourceName + "'", e);
    } catch (final InstantiationException e) {
      LOG.error("could not instantiat Class: '" + this.resourceName + "'", e);
    } catch (final IllegalAccessException e) {
      LOG.error("could not access Class: '" + this.resourceName + "'", e);
    } catch (final SecurityException e) {
      LOG.error("could not access Class: '" + this.resourceName + "'", e);
    } catch (final NoSuchMethodException e) {
      LOG.error("could not find method: '"
          + this.methodName
          + "' in class: '"
          + this.resourceName
          + "'", e);

    }

  }

  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = null;
    _parameter.put(ParameterValues.PROPERTIES, super.getProperties());
    try {

      ret = (Return) this.method.invoke(this.progInstance, _parameter);

    } catch (final SecurityException e) {
      LOG.error("could not access class: '" + this.resourceName, e);
    } catch (final IllegalArgumentException e) {
      LOG.error("execute(Context, Instance, Map<TriggerKeys4Values,Map>)", e);
    } catch (final IllegalAccessException e) {
      LOG.error("could not access class: '" + this.resourceName, e);
    } catch (final InvocationTargetException e) {
      LOG.error("could not invoke method: '"
          + this.methodName
          + "' in class: '"
          + this.resourceName, e);
      throw (EFapsException) e.getCause();
    }
    return ret;

  }

  /**
   * Loads all events from the database and assigns them to the specific
   * administrational type or command
   *
   * @param _context
   *                eFaps context for this request
   */
  public static void initialise() throws Exception {
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

    if (LOG.isDebugEnabled()) {
      LOG.debug("initialise Triggers ---------------------------------------");
    }
    while (query.next()) {
      final String eventOID = (String) query.get("OID");
      final long eventId = ((Number) query.get("ID")).longValue();
      final Type eventType = (Type) query.get("Type");
      final String eventName = (String) query.get("Name");
      final long eventPos = (Long) query.get("IndexPosition");
      final long abstractID = ((Number) query.get("Abstract")).longValue();
      final Long programId = ((Number) query.get("JavaProg")).longValue();;
      final String method = (String) query.get("Method");

      final String resName = getClassName(programId.toString());

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

      final EFapsClassNames eFapsClass = EFapsClassNames.getEnum(getTypeName(abstractID));

      EventType triggerEvent = null;
      for (final EventType trigger : EventType.values()) {
        final Type triggerClass = Type.get(trigger.name);
        if (eventType.isKindOf(triggerClass)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("     found trigger " + trigger + ":" + triggerClass);
          }
          triggerEvent = trigger;
          break;
        }
      }

      if (eFapsClass == DATAMODEL_TYPE) {
        final Type type = Type.get(abstractID);
        if (LOG.isDebugEnabled()) {
          LOG.debug("    type=" + type);
        }

        type.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == COMMAND) {
        final Command command = Command.get(abstractID);

        if (LOG.isDebugEnabled()) {
          LOG.debug("    Command=" + command.getName());
        }
        command.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == FIELD) {

        final Field field = Field.get(abstractID);

        if (LOG.isDebugEnabled()) {
          LOG.debug("       Field=" + field.getName());
        }

        field.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == DATAMODEL_ATTRIBUTE
                      || eFapsClass == DATAMODEL_ATTRIBUTESETATTRIBUTE) {
        final Attribute attribute = Attribute.get(abstractID);
        if (LOG.isDebugEnabled()) {
          LOG.debug("      Attribute=" + attribute.getName());
        }

        attribute.addEvent(triggerEvent, new EventDefinition(eventId,
            eventName, eventPos, resName, method, eventOID));

      } else if (eFapsClass == MENU) {
        final Menu menu = Menu.get(abstractID);
        if (LOG.isDebugEnabled()) {
          LOG.debug("      Menu=" + menu.getName());
        }

        menu.addEvent(triggerEvent, new EventDefinition(eventId, eventName,
            eventPos, resName, method, eventOID));

      } else if (eFapsClass == FIELDTABLE) {

        final FieldTable fieldtable = FieldTable.get(abstractID);

        if (LOG.isDebugEnabled()) {
          LOG.debug("       Field=" + fieldtable.getName());
        }

        fieldtable.addEvent(triggerEvent, new EventDefinition(eventId,
            eventName, eventPos, resName, method, eventOID));

      }

      else if (LOG.isDebugEnabled()) {
        LOG.debug("initialise() - unknown event trigger connection");
      }
    }

  }

  /**
   * get the ClassName from the Database
   *
   * @param _id
   *                ID of the Program the ClassName is searched for
   * @return ClassName
   */
  private static String getClassName(final String _id) {
    final SearchQuery query = new SearchQuery();
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
    } catch (final EFapsException e) {
      LOG.error("getClassName(String)", e);
    }
    return Name;
  }

  /**
   * get the Name of the Type from the Database
   *
   * @param abstractID    ID the Typename must be resolved
   * @return NAem of the Type
   */
  private static UUID getTypeName(final long abstractID)
  {
    final SearchQuery query = new SearchQuery();
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
    } catch (final EFapsException e) {
      LOG.error("getClassName(String)", e);
    }
    if (type == null) {
      LOG.error("Can't find the Type  with ID: " + abstractID);
    } else {
      return type.getUUID();
    }
    return null;
  }

}

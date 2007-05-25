/*
 * Copyright 2006 The eFaps Team
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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.admin.AdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.java.EFapsClassLoader;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;

/**
 * @author tmo
 * @version $Id: EventDefinition.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb
 *          2007) jmo $
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
  private final long       indexPos;

  /**
   * The variable stores the event type.
   */
  private final Type       eventType;

  /**
   * The variable stores the ID of the Program
   */
  private final long       progId;

  /**
   * The variable stores the Name of the JavaClass
   */
  private final String     resourceName;

  /**
   * 
   */
  private EventDefinition(final long _id, final String _name,
      final long _indexPos, final Type _eventType, final long _progID,
      final String _resourceName) {
    super(_id, null, _name);
    this.indexPos = _indexPos;
    this.eventType = _eventType;
    this.progId = _progID;
    this.resourceName = _resourceName;
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

  /**
   * This is the getter method for instance variable {@link #progId}.
   * 
   * @return value of instance variable {@link #progId}
   * @see #progId
   */
  public long getProgId() {
    return this.progId;
  }

  /**
   * This is the getter method for instance variable {@link #eventType}.
   * 
   * @return value of instance variable {@link #eventType}
   * @see #eventType
   */
  public Type geteventType() {
    return this.eventType;
  }

  public void execute(final Context _context, final Instance _instance,
                      final Map<TriggerKeys4Values, Map> _map) {

    try {

      Class cls = Class.forName(this.resourceName, true, new EFapsClassLoader(
          this.getClass().getClassLoader()));

      Method m = cls.getMethod("execute", new Class[] { Context.class,
          Instance.class, Map.class });

      m.invoke(cls.newInstance(), _context, _instance, _map);

    } catch (ClassNotFoundException e) {
      LOG.error("could not find class: '" + this.resourceName, e);
    } catch (SecurityException e) {
      LOG.error("could not access class: '" + this.resourceName, e);
    } catch (IllegalArgumentException e) {
      LOG.error("execute(Context, Instance, Map<TriggerKeys4Values,Map>)", e);
    } catch (NoSuchMethodException e) {
      LOG.error("could not find method: 'execute' in class: '" + this.resourceName + "'", e);
    } catch (IllegalAccessException e) {
      LOG.error("could not access class: '" + this.resourceName, e);
    } catch (InvocationTargetException e) {
      LOG.error("could not invoke method: 'execute' in class: '" + this.resourceName , e);
    } catch (InstantiationException e) {
      LOG.error("could not instantiat class: '" + this.resourceName, e);
    }

  }

  /**
   * Loads all events from the database and assigns them to the specific
   * administrational type.
   * 
   * @param _context
   *          eFaps context for this request
   */
  public static void initialise() throws Exception {
    SearchQuery query = new SearchQuery();
    query.setQueryTypes(EFapsClassName.EVENT_DEFINITION.name);
    query.setExpandChildTypes(true);
    query.addSelect("ID");
    query.addSelect("Type");
    query.addSelect("Name");
    query.addSelect("IndexPosition");
    query.addSelect("Abstract");
    query.addSelect("Abstract.Type");
    query.addSelect("JavaProg");
    query.addSelect("JavaProg.Name");
    query.executeWithoutAccessCheck();

    if (LOG.isDebugEnabled()) {
      LOG.debug("initialise Triggers ---------------------------------------");
    }
    while (query.next()) {
      long eventId = (Long) query.get("ID");
      Type eventType = (Type) query.get("Type");
      String eventName = (String) query.get("Name");
      long eventPos = (Long) query.get("IndexPosition");
      long parentId = (Long) query.get("Abstract");
      Type parentType = (Type) query.get("Abstract.Type");
      long programId = (Long) query.get("JavaProg");
      String resName = (String) query.get("JavaProg.Name");

      if (LOG.isDebugEnabled()) {
        LOG.debug("   eventId=" + eventId);
        LOG.debug("   eventType=" + eventType);
        LOG.debug("   eventName=" + eventName);
        LOG.debug("   eventPos=" + eventPos);
        LOG.debug("   parentId=" + parentId);
        LOG.debug("   parentType=" + parentType);
        LOG.debug("   programId=" + programId);
        LOG.debug("   JaveProgName=" + resName);
      }

      EFapsClassName eFapsClass = EFapsClassName.getEnum(parentType.getName());
      if (eFapsClass == EFapsClassName.DATAMODEL_TYPE) {
        Type type = Type.get(parentId);
        if (LOG.isDebugEnabled()) {
          LOG.debug("    type=" + type);
        }
        for (TriggerEvent triggerEvent : TriggerEvent.values()) {
          Type triggerClass = Type.get(triggerEvent.name);
          if (eventType.isKindOf(triggerClass)) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("     found trigger " + triggerEvent + ":"
                  + triggerClass);
            }
            type.addTrigger(triggerEvent, new EventDefinition(eventId,
                eventName, eventPos, eventType, programId, resName));
          }
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("initialise() - unknown event trigger connection");
        }
      }

    }
  }

}
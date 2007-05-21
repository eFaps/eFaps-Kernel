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

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Map;

import org.efaps.admin.AdminObject;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.transaction.ConnectionResource;

/**
 * @author tmo
 * @version $Id: EventDefinition.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb
 *          2007) jmo $
 */
public class EventDefinition extends AdminObject {

  /**
   * The variable stores the position in a event pool (more than one event
   * definition for one thrown event.
   * 
   * @see #getIndexPos
   */
  private final long indexPos;

  /**
   * The variable stores the event type id. The type itself could
   */
  private final Type eventType;

  /**
   * 
   */
  private EventDefinition(final long _id, final String _name,
      final long _indexPos, final Type _eventType) {
    super(_id, null, _name);
    this.indexPos = _indexPos;
    this.eventType = _eventType;
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
//muss ein Interfaxe sein
  public void execute(final Context _context, final Instance _instance,
                      final Map<TriggerKeys4Values, Map> _map)
                                                              throws org.efaps.util.EFapsException {

    ConnectionResource con = null;
    try {
      con = _context.getConnectionResource();

      StringBuilder cmd = new StringBuilder();

      cmd
          .append(
              "insert into HISTORY(EVENTTYPEID,FORTYPEID,FORID,MODIFIER,MODIFIED,ATTRID,ATTRVALUE) ")
          .append("values (").append(this.eventType.getId()).append(",")
          .append(_instance.getType().getId()).append(",").append(
              _instance.getId()).append(",").append(
              _context.getPerson().getId()).append(",").append(
              _context.getDbType().getCurrentTimeStamp()).append(",").append(
              "?,").append("?)");

      boolean executed = false;
      Map<Attribute, String> map = (Map<Attribute, String>) _map
          .get(TriggerKeys4Values.NEW_VALUES);
      for (Map.Entry<Attribute, String> entry : map.entrySet()) {
        PreparedStatement stmt = con.getConnection().prepareStatement(
            cmd.toString());
        stmt.setLong(1, entry.getKey().getId());
        stmt.setString(2, entry.getValue());
        stmt.executeUpdate();
        stmt.close();
        executed = true;
      }

      if (!executed) {
        PreparedStatement stmt = con.getConnection().prepareStatement(
            cmd.toString());
        stmt.setNull(1, Types.NULL);
        stmt.setNull(2, Types.NULL);
        stmt.executeUpdate();
        stmt.close();
      }

      /*
       * String [] keyColumn = {"ID"};
       * 
       * stmt.execute(cmd.toString(), keyColumn);
       * 
       * java.sql.ResultSet rs = stmt.getGeneratedKeys(); if (rs.next()) { long
       * newId = rs.getLong(1);
       * 
       * System.out.println("----------------->new id="+newId);
       *  } else { System.out.println("There are no generated keys."); }
       */

      con.commit();
    } catch (org.efaps.util.EFapsException e) {
      e.printStackTrace();
      if (con != null) {
        con.abort();
      }
      throw e;
    } catch (Throwable e) {
      e.printStackTrace();
      if (con != null) {
        con.abort();
      }
      throw new org.efaps.util.EFapsException(getClass(), "execute.Throwable");
    }

  }

  /*
   * EVENT_TRIGGER_HISTORY_CHECKIN ("Admin_Event_Trigger_History_Checkin"),
   * EVENT_TRIGGER_HISTORY_CHECKOUT ("Admin_Event_Trigger_History_Checkout"),
   * EVENT_TRIGGER_HISTORY_DELETE ("Admin_Event_Trigger_History_Delete"),
   * EVENT_TRIGGER_HISTORY_INSERT ("Admin_Event_Trigger_History_Insert"),
   * EVENT_TRIGGER_HISTORY_UPDATE ("Admin_Event_Trigger_History_Update"),
   */

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
    query.executeWithoutAccessCheck();
    System.out.println("--------------------------------------------");
    while (query.next()) {
      long eventId = (Long) query.get( "ID");
      Type eventType = (Type) query.get( "Type");
      String eventName = (String) query.get( "Name");
      long eventPos = (Long) query.get( "IndexPosition");
      long parentId = (Long) query.get( "Abstract");
      Type parentType = (Type) query.get( "Abstract.Type");
      System.out.println("eventId=" + eventId);
      System.out.println("eventType=" + eventType);
      System.out.println("eventName=" + eventName);
      System.out.println("eventPos=" + eventPos);
      System.out.println("parentId=" + parentId);
      System.out.println("parentType=" + parentType);

      EFapsClassName eFapsClass = EFapsClassName.getEnum(parentType.getName());
      if (eFapsClass == EFapsClassName.DATAMODEL_TYPE) {
        Type type = Type.get(parentId);
        System.out.println("type=" + type);
        for (TriggerEvent triggerEvent : TriggerEvent.values()) {
          Type triggerClass = Type.get(triggerEvent.name);
          if (eventType.isKindOf(triggerClass)) {
            System.out.println("found trigger " + triggerEvent + ":"
                + triggerClass);
            type.addTrigger(triggerEvent, new EventDefinition(eventId,
                eventName, eventPos, eventType));
          }
        }
      } else {
        System.out.println("unknown event trigger connection");
      }

    }
  }
}
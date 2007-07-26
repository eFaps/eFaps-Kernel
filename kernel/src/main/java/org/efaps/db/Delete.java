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

package org.efaps.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.transaction.StoreResource;
import org.efaps.util.EFapsException;

/**
 * The class is used as interface to the eFaps kernel to delete one object.
 * 
 * @author tmo
 * @version $Rev$
 */
public class Delete {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Delete.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the instance for which this update is made.
   * 
   * @see #getInstance
   * @see #setInstance
   */
  private final Instance instance;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @param _instance
   * @todo description
   */
  public Delete(final Instance _instance) {
    this.instance = _instance;
  }

  /**
   * @param _type
   * @param _id
   * @todo description
   */
  public Delete(final Type _type, final String _id) {
    this.instance = new Instance(_type, _id);
  }

  /**
   * @param _type
   * @param _id
   * @todo description
   */
  public Delete(final Type _type, final long _id) {
    this.instance = new Instance(_type, _id);
  }

  /**
   * @param _oid
   * @todo description
   */
  public Delete(final String _oid) {
    this.instance = new Instance(_oid);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * First it is checked if the user has access to delete the eFaps object
   * defined in {@link #instance}. If no access, an exception is thrown. If the
   * context user has access. the delete is made with
   * {@link #executeWithoutAccessCheck}.
   * 
   * @throws EFapsException
   *           if the current context user has no delete access on given eFaps
   *           object.
   * @see #executeWithoutAccessCheck
   */
  public void execute() throws EFapsException {
    boolean hasAccess =
        this.instance.getType().hasAccess(this.instance,
            AccessTypeEnums.DELETE.getAccessType());
    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess", this.instance);
    }
    executeWithoutAccessCheck();
  }

  /**
   * The method executes the delete. For the object, a delete is made in all SQL
   * tables from the type (if the SQL table is not read only!). If a store is
   * defined for the type, the checked in file is also deleted (with the help of
   * the store resource implementation; if the store resource implementation has
   * not implemented the delete, the file is not deleted!).<br/> It is not
   * checked if the current context user has access to delete the eFaps object
   * defined in {@link #instance}.
   * 
   * @see SQLTable#readOnly
   */
  public void executeWithoutAccessCheck() throws EFapsException {
    Context context = Context.getThreadContext();
    ConnectionResource con = null;
    executeEvents(EventType.DELETE_PRE);
    if (!executeEvents(EventType.DELETE_OVERRIDE)) {
      try {

        con = context.getConnectionResource();

        Statement stmt = null;
        try {
          stmt = con.getConnection().createStatement();

          SQLTable mainTable = getInstance().getType().getMainTable();
          for (SQLTable curTable : getInstance().getType().getTables()) {
            if ((curTable != mainTable) && !curTable.isReadOnly()) {
              StringBuilder buf = new StringBuilder();
              buf.append("delete from ").append(curTable.getSqlTable()).append(
                  " ");
              buf.append("where ").append(curTable.getSqlColId()).append("=")
                  .append(getInstance().getId()).append("");
              if (LOG.isTraceEnabled()) {
                LOG.trace(buf.toString());
              }
              stmt.addBatch(buf.toString());
            }
          }
          StringBuilder buf = new StringBuilder();
          buf.append("delete from ").append(mainTable.getSqlTable())
              .append(" ");
          buf.append("where ").append(mainTable.getSqlColId()).append("=")
              .append(getInstance().getId()).append("");
          if (LOG.isTraceEnabled()) {
            LOG.trace(buf.toString());
          }
          stmt.addBatch(buf.toString());

          stmt.executeBatch();
        } catch (SQLException e) {
          throw new EFapsException(getClass(),
              "executeWithoutAccessCheck.SQLException", e, this.instance);
        }
        finally {
          try {
            if (stmt != null) {
              stmt.close();
            }
          } catch (java.sql.SQLException e) {
          }
        }

        con.commit();
      }
      finally {
        if ((con != null) && con.isOpened()) {
          con.abort();
        }
      }

      StoreResource store = null;
      try {
        if (getInstance().getType().hasStoreResource()) {
          store = context.getStoreResource(getInstance());
          store.delete();
          store.commit();
        }
      }
      finally {
        if ((store != null) && store.isOpened()) {
          store.abort();
        }
      }
    }
    executeEvents(EventType.DELETE_POST);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for instance variable {@link #instance}.
   * 
   * @return value of instance variable {@link #instance}
   * @see #instance
   * @see #setInstance
   */
  public Instance getInstance() {
    return this.instance;
  }

  /**
   * The method gets all events for the given EventType and executes them in the
   * given order. If no events are defined, nothing is done. The method return
   * TRUE if a event was found, otherwise FALSE.
   * 
   * @param _context
   *          eFaps context for this request
   * @param _eventtype
   *          EventType to execute
   * @return true if a trigger was found and executed, otherwise false
   * @throws EFapsException 
   */
  private boolean executeEvents(final EventType _eventtype) throws EFapsException {
    List<EventDefinition> triggers =
        getInstance().getType().getEvents(_eventtype);
    if (triggers != null) {
      Parameter parameter = new Parameter();

      parameter.put(ParameterValues.INSTANCE, getInstance());
      for (EventDefinition evenDef : triggers) {
        evenDef.execute(parameter);
      }
      return true;
    }
    return false;
  }
}

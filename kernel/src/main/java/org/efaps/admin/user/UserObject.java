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

package org.efaps.admin.user;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.AdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id: UserObject.java 523 2006-11-03 22:36:48 +0000 (Fri, 03 Nov
 *          2006) tmo $
 * @todo description
 */
public abstract class UserObject extends AdminObject {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(UserObject.class);

  // ///////////////////////////////////////////////////////////////////////////
  // contruktors

  /**
   * Constructor to set the id and name of the user object.
   * 
   * @param _id
   *          id to set
   * @param _name
   *          name to set
   */
  protected UserObject(final long _id, final String _name) {
    super(_id, null, _name);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Checks, if the given person is assigned to this user object. The method
   * must be overwritten by the special implementations.
   * 
   * @param _person
   *          person to test
   * @return <i>true</i> if the person is assigned to this user object,
   *         otherwise <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  abstract public boolean hasChildPerson(final Person _person);

  /**
   * Checks, if the context user is assigned to this user object. The instance
   * method uses {@link #hasChildPerson} to test this.
   * 
   * @param _context
   *          context for this request
   * @see #hasChildPerson
   */
  public boolean isAssigned(final Context _context) {
    return hasChildPerson(_context.getPerson());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // methods to communicate with the database

  /**
   * Assign this user object to the given JAAS system under the given JAAS key.
   * 
   * @param _jaasSystem
   *          JAAS system to which the person is assigned
   * @param _jaasKey
   *          key under which the person is know in the JAAS system
   * @throws EFapsException
   *           if the assignment could not be made
   */
  public void assignToJAASSystem(final JAASSystem _jaasSystem,
                                 final String _jaasKey) throws EFapsException {

    ConnectionResource rsrc = null;
    try {
      Context context = Context.getThreadContext();
      rsrc = context.getConnectionResource();
      Type keyType = Type.get(EFapsClassName.USER_JAASKEY.name);

      PreparedStatement stmt = null;
      StringBuilder cmd = new StringBuilder();
      try {
        long keyId = 0;
        if (!Context.getDbType().supportsGetGeneratedKeys()) {
          keyId = Context.getDbType().getNewId(rsrc.getConnection(),
              keyType.getMainTable().getSqlTable(), "ID");
          cmd.append("insert into ").append(
              keyType.getMainTable().getSqlTable()).append(
              "(ID,KEY,CREATOR,CREATED,MODIFIER,MODIFIED,").append(
              "USERABSTRACT,USERJAASSYSTEM) ").append("values (").append(keyId)
              .append(",");
        } else {
          cmd.append("insert into ").append(
              keyType.getMainTable().getSqlTable()).append(
              "(KEY,CREATOR,CREATED,MODIFIER,MODIFIED,").append(
              "USERABSTRACT,USERJAASSYSTEM) ").append("values (");
        }
        cmd.append("'").append(_jaasKey).append("',").append(
            context.getPersonId()).append(",").append(
            Context.getDbType().getCurrentTimeStamp()).append(",").append(
            context.getPersonId()).append(",").append(
            Context.getDbType().getCurrentTimeStamp()).append(",").append(
            getId()).append(",").append(_jaasSystem.getId()).append(")");
        stmt = rsrc.getConnection().prepareStatement(cmd.toString());
        int rows = stmt.executeUpdate();
        if (rows == 0) {
          // TODO: exception in properties
          LOG.error("could not execute '" + cmd.toString() + "' "
              + "for JAAS system '" + _jaasSystem.getName() + "' "
              + "for user object '" + toString() + "' " + "with JAAS key '"
              + _jaasKey + "'");
          throw new EFapsException(getClass(),
              "assignToJAASSystem.NotInserted", _jaasSystem.getName(),
              _jaasKey, toString());
        }
      } catch (SQLException e) {
        // TODO: exception in properties
        LOG.error("could not execute '" + cmd.toString() + "' " + "to assign "
            + "user object '" + toString() + "' " + "with JAAS key '"
            + _jaasKey + "' " + "to JAAS system '" + _jaasSystem.getName()
            + "'", e);
        throw new EFapsException(getClass(), "assignToJAASSystem.SQLException",
            e, cmd.toString(), _jaasSystem.getName(), _jaasKey, toString());
      }
      finally {
        try {
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException e) {
        }
      }

      rsrc.commit();
    }
    finally {
      if ((rsrc != null) && rsrc.isOpened()) {
        rsrc.abort();
      }
    }
  }

  /**
   * Assign this user object to the given user object for given JAAS system.
   * 
   * @param _assignType
   *          type used to assign (in other words the relationship type)
   * @param _jaasSystem
   *          JAAS system for which this user object is assigned to the given
   *          object
   * @param _object
   *          user object to which this user object is assigned
   * @throws EFapsException
   *           if assignment could not be done
   */
  protected void assignToUserObjectInDb(final Type _assignType,
                                        final JAASSystem _jaasSystem,
                                        final UserObject _object)
                                                                 throws EFapsException {

    ConnectionResource rsrc = null;
    try {
      Context context = Context.getThreadContext();
      rsrc = context.getConnectionResource();

      Statement stmt = null;
      StringBuilder cmd = new StringBuilder();
      try {

        cmd.append("insert into ").append(
            _assignType.getMainTable().getSqlTable()).append("(");
        long keyId = 0;
        if (!Context.getDbType().supportsGetGeneratedKeys()) {
          keyId = Context.getDbType().getNewId(rsrc.getConnection(),
              _assignType.getMainTable().getSqlTable(), "ID");
          cmd.append("ID,");
        }
        cmd.append("TYPEID,CREATOR,CREATED,MODIFIER,MODIFIED,").append(
            "USERABSTRACTFROM,USERABSTRACTTO,USERJAASSYSTEM) ").append(
            "values (");
        if (keyId != 0) {
          cmd.append(keyId).append(",");
        }
        cmd.append(_assignType.getId()).append(",").append(
            context.getPersonId()).append(",").append(
            Context.getDbType().getCurrentTimeStamp()).append(",").append(
            context.getPersonId()).append(",").append(
            Context.getDbType().getCurrentTimeStamp()).append(",").append(
            getId()).append(",").append(_object.getId()).append(",").append(
            _jaasSystem.getId()).append(")");

        stmt = rsrc.getConnection().createStatement();
        int rows = stmt.executeUpdate(cmd.toString());
        if (rows == 0) {
          // TODO: exception in properties
          LOG.error("could not execute '" + cmd.toString() + "' "
              + "to assign " + "user object '" + toString() + "' "
              + "to object " + "'" + _object + "' " + "for JAAS system " + "'"
              + _jaasSystem + "' ");
          throw new EFapsException(getClass(), "assignInDb.NotInserted",
              _jaasSystem.getName(), _object.getName(), getName());
        }
      } catch (SQLException e) {
        // TODO: exception in properties
        LOG
            .error("could not execute '" + cmd.toString() + "' " + "to assign "
                + "user object '" + toString() + "' " + "to object " + "'"
                + _object + "' " + "for JAAS system " + "'" + _jaasSystem
                + "' ", e);
        throw new EFapsException(getClass(), "assignInDb.SQLException", e, cmd
            .toString(), getName());
      }
      finally {
        try {
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException e) {
        }
      }
      rsrc.commit();
    }
    finally {
      if ((rsrc != null) && rsrc.isOpened()) {
        rsrc.abort();
      }
    }
  }

  /**
   * Unassign this user object from the given user object for given JAAS system.
   * 
   * @param _unassignType
   *          type used to unassign (in other words the relationship type)
   * @param _jaasSystem
   *          JAAS system for which this user object is unassigned from given
   *          object
   * @param _object
   *          user object from which this user object is unassigned
   * @throws EFapsException
   *           if unassignment could not be done
   */
  protected void unassignFromUserObjectInDb(final Type _unassignType,
                                            final JAASSystem _jaasSystem,
                                            final UserObject _object)
                                                                     throws EFapsException {

    ConnectionResource rsrc = null;
    try {
      rsrc = Context.getThreadContext().getConnectionResource();
      Statement stmt = null;
      StringBuilder cmd = new StringBuilder();
      try {
        cmd.append("delete from ").append(
            _unassignType.getMainTable().getSqlTable()).append(" ").append(
            "where USERJAASSYSTEM=").append(_jaasSystem.getId()).append(" ")
            .append("and USERABSTRACTFROM=").append(getId()).append(" ")
            .append("and USERABSTRACTTO=").append(_object.getId());

        stmt = rsrc.getConnection().createStatement();
        stmt.executeUpdate(cmd.toString());

      } catch (SQLException e) {
        // TODO: exception in properties
        LOG.error("could not execute '" + cmd.toString() + "' "
            + "to unassign " + "user object '" + toString() + "' "
            + "from object " + "'" + _object + "' " + "for JAAS system " + "'"
            + _jaasSystem + "' ", e);
        throw new EFapsException(getClass(),
            "unassignFromUserObjectInDb.SQLException", e, cmd.toString(),
            getName());
      }
      finally {
        try {
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException e) {
        }
      }

      rsrc.commit();
    }
    finally {
      if ((rsrc != null) && rsrc.isOpened()) {
        rsrc.abort();
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link UserObject}.
   * 
   * @param _id
   *          id to search in the cache
   * @return instance of class {@link UserObject}
   */
  static public UserObject getUserObject(final long _id) throws Exception {
    UserObject ret = Role.get(_id);
    if (ret == null) {
      ret = Person.get(_id);
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link UserObject}.
   * 
   * @param _name
   *          name to search in the cache
   * @return instance of class {@link UserObject}
   */
  static public UserObject getUserObject(final String _name) throws Exception {
    UserObject ret = Role.get(_name);
    if (ret == null) {
      ret = Person.get(_name);
    }
    return ret;
  }
}

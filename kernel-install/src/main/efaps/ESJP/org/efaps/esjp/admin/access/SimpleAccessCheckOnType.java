/*
 * Copyright 2003-2008 The eFaps Team
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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is used to check if a user can Access this Type.<br>
 * The method execute is called with the Instance and the Accesstype as
 * parameters. For the instance object it is checked if the current context user
 * has the access defined in the list of access types.
 *
 * @author tmo
 * @version $Id:SimpleAccessCheckOnType.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("fd1ecee1-a882-4fbe-8b3c-5e5c3ed4d6b7")
public class SimpleAccessCheckOnType implements EventExecution
{
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG
          = LoggerFactory.getLogger(SimpleAccessCheckOnType.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Check for the instance object if the current context user has the access
   * defined in the list of access types.
   */
  private boolean checkAccess(final Instance _instance,
                              final AccessType _accessType)
      throws EFapsException
  {
    final Context context = Context.getThreadContext();

    final Type type = _instance.getType();
    final StringBuilder toTests = new StringBuilder();
    toTests.append(0);
    for (AccessSet accessSet : type.getAccessSets()) {
      if (accessSet.getAccessTypes().contains(_accessType)) {
        toTests.append(",").append(accessSet.getId());
      }
    }

    final StringBuilder users = new StringBuilder();
    users.append(context.getPersonId());
    for (final Role role : context.getPerson().getRoles()) {
      users.append(",").append(role.getId());
    }
    for (final Group group : context.getPerson().getGroups()) {
      users.append(",").append(group.getId());
    }

    return executeStatement(context, toTests, users);
  }

  private boolean executeStatement(final Context _context,
                                   final StringBuilder _accessSets,
                                   final StringBuilder _users)
      throws EFapsException
  {
    boolean hasAccess = false;

    final StringBuilder cmd = new StringBuilder();
    cmd.append("select count(*) from T_ACCESSSET2USER ").append(
        "where ACCESSSET in (").append(_accessSets).append(") ").append(
        "and USERABSTRACT in (").append(_users).append(")");

    ConnectionResource con = null;
    try {
      con = _context.getConnectionResource();

      Statement stmt = null;
      try {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(cmd.toString());
        if (rs.next()) {
          hasAccess = (rs.getLong(1) > 0) ? true : false;
        }
        rs.close();

      }
      finally {
        if (stmt != null) {
          stmt.close();
        }
      }

      con.commit();

    } catch (SQLException e) {
      LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
    }
    finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return hasAccess;
  }

  public Return execute(final Parameter _parameter)
      throws EFapsException
  {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final AccessType accessType = (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
    final Return ret = new Return();

    if (checkAccess(instance, accessType)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }
}

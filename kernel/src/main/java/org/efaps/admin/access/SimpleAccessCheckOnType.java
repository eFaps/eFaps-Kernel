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

package org.efaps.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class SimpleAccessCheckOnType implements AccessCheckInterface   {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG 
                            = LogFactory.getLog(SimpleAccessCheckOnType.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Check for the instance object if the current context user has the
   * access defined in the list of access types.
   */
  public boolean checkAccess(final Instance _instance, 
                             final AccessType _accessType) 
                                                      throws EFapsException  {

    Context context = Context.getThreadContext();

    Type type = _instance.getType();
    StringBuilder toTests = new StringBuilder();
    toTests.append(0);
    for (AccessSet accessSet : type.getAccessSets())  {
      if (accessSet.getAccessTypes().contains(_accessType))  {
        toTests.append(",").append(accessSet.getId());
      }
    }
    
    StringBuilder users = new StringBuilder();
    users.append(context.getPersonId());
    for (Role role : context.getPerson().getRoles())  {
      users.append(",").append(role.getId());
    }
    for (Group group : context.getPerson().getGroups())  {
      users.append(",").append(group.getId());
    }

    return executeStatement(context, toTests, users);
  }

  protected boolean executeStatement(final Context _context,
                                     final StringBuilder _accessSets,
                                     final StringBuilder _users) 
                                                      throws EFapsException  {
    boolean hasAccess = false;

    StringBuilder cmd = new StringBuilder();
    cmd.append("select count(*) from ACCESSSET2USER ")
       .append("where ACCESSSET in (").append(_accessSets).append(") ")
       .append("and USERABSTRACT in (").append(_users).append(")");

    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      Statement stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(cmd.toString());
        if (rs.next())  {
          hasAccess = (rs.getLong(1) > 0) ? true : false;
        }
        rs.close();

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }

      con.commit();

    } catch (SQLException e)  {
      LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
    } finally  {
      if ((con != null) && con.isOpened())  {
        con.abort();
      }
    }
    return hasAccess;
  }
}

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

package org.efaps.teamwork;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class to check the AccessRights for the UserInterfaces in TeamWork.<br>
 * 
 * @author jmo
 * @version $Id$
 */
public class AccessCheckOnUserInterface implements EventExecution {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG =
      LogFactory.getLog(AccessCheckOnUserInterface.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Check for the debending on the Current oid if the current context user has
   * the access defined in the list of access types.
   */
  private boolean checkAccess(final Type _type, final AccessType _accessType) {
    boolean hasAccess = false;

    // this only checks the rights for RootCollections, Collection

    try {
      Context context = Context.getThreadContext();
      for (Role role : context.getPerson().getRoles()) {
        // the TeamWorkAdmin has all rights in TeamWork, so
        // no further controlling is needed
        if (role.getName().equals("TeamWorkAdmin")) {
          return true;
        }
      }
      // search for the User specific rights
      SearchQuery query = new SearchQuery();
      query.setExpand(context.getParameter("oid"),
          "TeamWork_MemberRights\\AbstractLink");
      query.addSelect("AccessSetLink");
      query
          .addWhereExprEqValue("UserAbstractLink", context.getPerson().getId());

      query.execute();

      if (query.next()) {
        AccessSet accessSet =
            AccessSet.getAccessSet((Long) query.get("AccessSetLink"));
        if (accessSet.getAccessTypes().contains(_accessType)
            && accessSet.getDataModelTypes().contains(_type)) {
          hasAccess = true;
        }

      }

    } catch (EFapsException e) {
      LOG.error("checkAccess(Instance, AccessType)", e);
    }

    return hasAccess;
  }

  public Return execute(Parameter _parameter) {

    AccessType accesstype =
        AccessType.getAccessType((String) ((Map) _parameter
            .get(ParameterValues.PROPERTIES)).get("AccessType"));

    Type type =
        Type.get((String) ((Map) _parameter.get(ParameterValues.PROPERTIES))
            .get("Type"));

    Return ret = new Return();

    if (checkAccess(type, accesstype)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }
}

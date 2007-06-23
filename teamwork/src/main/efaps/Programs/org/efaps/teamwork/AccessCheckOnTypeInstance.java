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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.ParameterInterface;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.ReturnInterface;
import org.efaps.admin.event.ParameterInterface.ParameterValues;
import org.efaps.admin.event.ReturnInterface.ReturnValues;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class AccessCheckOnTypeInstance implements EventExecution {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG =
      LogFactory.getLog(AccessCheckOnTypeInstance.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Check for the instance object if the current context user has the access
   * defined in the list of access types.
   */
  private boolean checkAccess(final Instance _instance,
      final AccessType _accessType) {
    boolean hasAccess = false;

    // this only checks the rights for RootCollections, Collection
    if ("TeamWork_RootCollection".equals(_instance.getType().getName())
        || "TeamWork_Collection".equals(_instance.getType().getName())) {
      try {
        Context context = Context.getThreadContext();
        for (Role role : context.getPerson().getRoles()) {
          // the TeamWorkAdmin has all rights on a TeamWork_RootCollection, so
          // no further controlling is needed
          if (role.getName().equals("TeamWorkAdmin")) {
            return true;
          }
        }
        //search for the User specific rights
        SearchQuery query = new SearchQuery();
        //if create, get the parent
        if (_accessType == AccessType.getAccessType("create")) {
          query.setExpand(context.getParameter("oid"),
              "TeamWork_MemberRights\\AbstractLink");
        } else {
          query.setExpand(_instance, "TeamWork_MemberRights\\AbstractLink");
        }
        query.addSelect("AccessSetLink");
        query.addWhereExprEqValue("UserAbstractLink", context.getPerson()
            .getId());

        query.execute();

        if (query.next()) {
          AccessSet accessSet =
              AccessSet.getAccessSet((Long) query.get("AccessSetLink"));
          if (accessSet.getAccessTypes().contains(_accessType)) {
            hasAccess = true;
          }

        }

      } catch (EFapsException e) {
        LOG.error("checkAccess(Instance, AccessType)", e);
      }
    }

    return hasAccess;
  }

  public ReturnInterface execute(ParameterInterface _parameter) {
    Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    AccessType accessType =
        (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
    ReturnInterface ret = new Return();

    if (checkAccess(instance, accessType)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }
}

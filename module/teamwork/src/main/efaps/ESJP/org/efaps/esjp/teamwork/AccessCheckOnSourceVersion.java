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

package org.efaps.esjp.teamwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("9dd18ca7-95a9-4ce1-91ea-5aaf7c227240")
@EFapsRevision("$Rev$")
public class AccessCheckOnSourceVersion implements EventExecution {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG =
    LoggerFactory.getLogger(AccessCheckOnSourceVersion.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Check for the instance object if the current context user has the access
   * defined in the list of access types.
   */
  private boolean checkAccess(final Instance _instance,
      final AccessType _accessType) {
    boolean hasAccess = false;

    // this only checks the rights for ..
    if ("TeamWork_SourceVersion".equals(_instance.getType().getName())) {
      try {
        final Context context = Context.getThreadContext();
        for (final Role role : context.getPerson().getRoles()) {
          // the TeamWorkAdmin has all rights on a TeamWork_RootCollection, so
          // no further controlling is needed
          if (role.getName().equals("TeamWorkAdmin")) {
            return true;
          }
        }
        // if create, get the parent
        final Instance instance = Instance.get(context.getParameter("oid"));
        String OID = null;
        if ("TeamWork_SourceVersion".equals(instance.getType().getName())) {
          OID = getParentOID(context.getParameter("oid"));

        } else {
          OID = context.getParameter("oid");
        }

        // first check if the Person has rights, if no check for Groups
        long accessSetID =
            getSpecificAccessSetID(OID, context.getPerson().getId());

        if (accessSetID != 0) {
          final AccessSet accessSet = AccessSet.getAccessSet(accessSetID);
          if (accessSet.getAccessTypes().contains(_accessType)
              && (_accessType == AccessType.getAccessType("show") || accessSet
                  .getDataModelTypes().contains(_instance.getType()))) {
            hasAccess = true;
          }

        } else {
          for (final Group group : context.getPerson().getGroups()) {
            accessSetID = getSpecificAccessSetID(OID, group.getId());
            if (accessSetID != 0) {

              final AccessSet accessSet = AccessSet.getAccessSet(accessSetID);
              if (accessSet.getAccessTypes().contains(_accessType)
                  && (_accessType == AccessType.getAccessType("show") || accessSet
                      .getDataModelTypes().contains(_instance.getType()))) {
                hasAccess = true;
              }
              break;
            }

          }

        }

      } catch (final EFapsException e) {
        LOG.error("checkAccess(Instance, AccessType)", e);
      }
    }


    return hasAccess;
  }

  private String getParentOID(final String _oid) {
    final SearchQuery query = new SearchQuery();
    String OID = null;
    try {
      query.setObject(_oid);
      query.addSelect("ParentSourceLink.OID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        OID = (String) query.get("ParentSourceLink.OID");
      }

    } catch (final EFapsException e) {

      LOG.error("getParentOID(String)", e);
    }
    return OID;
  }

  private long getSpecificAccessSetID(final String _oid,
      final long _abstractuserid) {
    final SearchQuery query = new SearchQuery();
    long ret = 0;
    try {
      query.setExpand(_oid, "TeamWork_MemberRights\\AbstractLink");
      query.addSelect("AccessSetLink");
      query.addWhereExprEqValue("UserAbstractLink", _abstractuserid);

      query.executeWithoutAccessCheck();
      if (query.next()) {
        ret = (Long) query.get("AccessSetLink");
      }
    } catch (final EFapsException e) {
      LOG.error("getSpecificAccessSetID(Instance, long)", e);
    }

    return ret;

  }

  public Return execute(final Parameter _parameter) {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final AccessType accessType =
        (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
    final Return ret = new Return();

    if (checkAccess(instance, accessType)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }
}

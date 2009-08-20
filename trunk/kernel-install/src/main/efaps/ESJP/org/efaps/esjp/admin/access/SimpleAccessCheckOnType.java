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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
 */

package org.efaps.esjp.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
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
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * This Class is used to check if a user can Access this Type.<br>
 * The method execute is called with the Instance and the Accesstype as
 * parameters. For the instance object it is checked if the current context user
 * has the access defined in the list of access types.
 *
 * @author The eFaps Team
 * @version $Id:SimpleAccessCheckOnType.java 1563 2007-10-28 14:07:41Z tmo $
 */
@EFapsUUID("fd1ecee1-a882-4fbe-8b3c-5e5c3ed4d6b7")
@EFapsRevision("$Rev$")
public class SimpleAccessCheckOnType implements EventExecution
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleAccessCheckOnType.class);

    /**
     * Check for the instance object if the current context user has the access
     * defined in the list of access types.
     *
     * @param _instance instance to check for access for
     * @param _accessType accesstyep to check the access for
     * @return true if access is granted, else false
     * @throws EFapsException on error
     */
    private boolean checkAccess(final Instance _instance, final AccessType _accessType) throws EFapsException
    {
        final Context context = Context.getThreadContext();

        final StringBuilder cmd = new StringBuilder();
        cmd.append("select count(*) from T_ACCESSSET2USER ");

        final Type type = _instance.getType();
        if (type.isCheckStatus() && !_accessType.equals(AccessTypeEnums.CREATE.getAccessType())) {
            cmd.append(" join T_ACCESSSET2STATUS on T_ACCESSSET2USER.ACCESSSET = T_ACCESSSET2STATUS.ACCESSSET")
                .append(" join ").append(type.getMainTable().getSqlTable()).append(" on ")
                .append(type.getMainTable().getSqlTable()).append(".")
                .append(type.getStatusAttribute().getSqlColNames().get(0))
                .append("=T_ACCESSSET2STATUS.ACCESSSTATUS");
        }

        cmd.append(" where T_ACCESSSET2USER.ACCESSSET in (0");
        for (final AccessSet accessSet : type.getAccessSets()) {
            if (accessSet.getAccessTypes().contains(_accessType)) {
                cmd.append(",").append(accessSet.getId());
            }
        }
        cmd.append(") ").append("and T_ACCESSSET2USER.USERABSTRACT in (").append(context.getPersonId());
        for (final Role role : context.getPerson().getRoles()) {
            cmd.append(",").append(role.getId());
        }
        for (final Group group : context.getPerson().getGroups()) {
            cmd.append(",").append(group.getId());
        }
        cmd.append(")");
        if (type.isCheckStatus() && !_accessType.equals(AccessTypeEnums.CREATE.getAccessType())) {
            cmd.append(" and ").append(type.getMainTable().getSqlTable()).append(".ID=").append(_instance.getId());
        }
        return executeStatement(context, cmd);
    }

    /**
     * Method that queries against the database.
     *
     * @param _context Context
     * @param _cmd cmd
     * @return true if access granted else false
     * @throws EFapsException on error
     */
    private boolean executeStatement(final Context _context, final StringBuilder _cmd) throws EFapsException
    {
        boolean hasAccess = false;

        ConnectionResource con = null;
        try {
            con = _context.getConnectionResource();

            Statement stmt = null;
            try {

                stmt = con.getConnection().createStatement();

                final ResultSet rs = stmt.executeQuery(_cmd.toString());
                if (rs.next()) {
                    hasAccess = (rs.getLong(1) > 0) ? true : false;
                }
                rs.close();

            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }

            con.commit();

        } catch (final SQLException e) {
            SimpleAccessCheckOnType.LOG.error("sql statement '" + _cmd.toString() + "' not executable!", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                con.abort();
            }
        }
        return hasAccess;
    }

    /**
     * {@inheritDoc}
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {
        final AccessType accessType = (AccessType) _parameter.get(ParameterValues.ACCESSTYPE);
        final Instance instance = _parameter.getInstance();

        final Return ret = new Return();
        if (instance != null) {
            if (Context.getThreadContext().getPerson() == null || checkAccess(instance, accessType)) {
                ret.put(ReturnValues.TRUE, true);
            }
        } else {
            final List<?> instances = (List<?>) _parameter.get(ParameterValues.OTHERS);
            ret.put(ReturnValues.VALUES, checkAccess(instances, accessType));
        }
        return ret;
    }

    /**
     * Method to check the access for a list of instances.
     * @param _instances    instances to be checked
     * @param _accessType   type of access
     * @return map of access to boolean
     * @throws EFapsException on error
     */
    private Map<Instance, Boolean> checkAccess(final List<?> _instances, final AccessType _accessType)
        throws EFapsException
    {
        final Map<Instance, Boolean> accessMap = new HashMap<Instance, Boolean>();
        final Context context = Context.getThreadContext();

        final Type type = ((Instance) _instances.get(0)).getType();
        if (type.isCheckStatus()) {
            final StringBuilder cmd = new StringBuilder();
            cmd.append("select ").append(type.getMainTable().getSqlTable()).append(".ID ")
                .append(" from T_ACCESSSET2USER ")
                .append(" join T_ACCESSSET2STATUS on T_ACCESSSET2USER.ACCESSSET = T_ACCESSSET2STATUS.ACCESSSET")
                .append(" join ").append(type.getMainTable().getSqlTable()).append(" on ")
                .append(type.getMainTable().getSqlTable()).append(".")
                .append(type.getStatusAttribute().getSqlColNames().get(0))
                .append("=T_ACCESSSET2STATUS.ACCESSSTATUS")
                .append(" where T_ACCESSSET2USER.ACCESSSET in (0");
            for (final AccessSet accessSet : type.getAccessSets()) {
                if (accessSet.getAccessTypes().contains(_accessType)) {
                    cmd.append(",").append(accessSet.getId());
                }
            }
            cmd.append(") ").append("and T_ACCESSSET2USER.USERABSTRACT in (").append(context.getPersonId());
            for (final Role role : context.getPerson().getRoles()) {
                cmd.append(",").append(role.getId());
            }
            for (final Group group : context.getPerson().getGroups()) {
                cmd.append(",").append(group.getId());
            }
            cmd.append(")");
            final Set<Long> idList = new HashSet<Long>();

            ConnectionResource con = null;
            try {
                con = context.getConnectionResource();

                Statement stmt = null;
                try {

                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(cmd.toString());

                    while (rs.next()) {
                        idList.add(rs.getLong(1));
                    }
                    rs.close();

                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }

                con.commit();

            } catch (final SQLException e) {
                SimpleAccessCheckOnType.LOG.error("sql statement '" + cmd.toString() + "' not executable!", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    con.abort();
                }
                for (final Object inst : _instances) {
                    accessMap.put((Instance) inst, idList.contains(((Instance) inst).getId()));
                }
            }

        } else {
            final boolean access = checkAccess(((Instance) _instances.get(0)), _accessType);
            for (final Object inst : _instances) {
                accessMap.put((Instance) inst, access);
            }
        }
        return accessMap;
    }
}

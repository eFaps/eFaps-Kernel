/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class represents the instance of a group in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Group
    extends AbstractUserObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Group.class);

    /**
     * This is the SQL select statement to select all groups from the database.
     */
    private static final String SQL_SELECT = "select ID, NAME, STATUS from V_USERGROUP";

    /**
     * Cache for all existing groups.
     */
    private static GroupCache CACHE = new GroupCache();

    /**
     * Create a new group instance. The method is used from the static method
     * {@link #initialize()} to read all groups from the database.
     *
     * @param _id       id of the group
     * @param _name     name of the group
     * @param _status   status of the group
     */
    private Group(final long _id,
                  final String _name,
                  final boolean _status)
    {
        super(_id, null, _name, _status);
    }

    /**
     * Returns the viewable name of the group. The method {@link #getName} is
     * used for the viewing name.
     *
     * @param _context  context for this request
     * @return name of the group
     * @see #getName
     */
    public String getViewableName(final Context _context)
    {
        return getName();
    }

    /**
     * Checks, if the given person is assigned to this group.
     *
     * @param _person   person to test
     * @return <i>true</i> if the person is assigned to this group, otherwise
     *         <i>false</i>
     * @see Person#isAssigned(Group)
     */
    @Override
    public boolean hasChildPerson(final Person _person)
    {
// TODO: child groups
        return _person.isAssigned(this);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @see #CACHE
     */
    public static void initialize()
    {
        Group.CACHE.initialize(Group.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Group}.
     *
     * @param _id   id to search in the cache
     * @return instance of class {@link Group}
     * @see #CACHE
     */
    public static Group get(final long _id)
    {
        return Group.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Group}.
     *
     * @param _name     name to search in the cache
     * @return instance of class {@link Group}
     * @see #CACHE
     */
    public static Group get(final String _name)
    {
        return Group.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_jaasKey</i> the instance of class
     * {@link Group}. The parameter <i>_jaasKey</i> is the name of the group
     * used in the given JAAS system for the group.
     *
     * @param _jaasSystem   JAAS system for which the JAAS key is named
     * @param _jaasKey      key in the foreign JAAS system for which the group
     *                      is searched
     * @return instance of class {@link Group}, or <code>null</code> if group
     *         is not found
     * @throws EFapsException if group with JAAS key could not be fetched from
     *                        eFaps
     * @see #get(long)
     */
    public static Group getWithJAASKey(final JAASSystem _jaasSystem,
                                       final String _jaasKey)
        throws EFapsException
    {
        long groupId = 0;
        ConnectionResource rsrc = null;
        try {
            rsrc = Context.getThreadContext().getConnectionResource();

            Statement stmt = null;

            try {
                final  StringBuilder cmd = new StringBuilder()
                    .append("select ").append("ID ").append("from V_USERGROUPJASSKEY ")
                    .append("where JAASKEY='").append(_jaasKey).append("' ")
                    .append("and JAASSYSID=").append(_jaasSystem.getId());

                stmt = rsrc.getConnection().createStatement();
                final ResultSet resultset = stmt.executeQuery(cmd.toString());
                if (resultset.next()) {
                    groupId = resultset.getLong(1);
                }
                resultset.close();
            } catch (final SQLException e) {
                Group.LOG.error("search for group for JAAS system '" + _jaasSystem.getName() + "' "
                        + "with key '" + _jaasKey + "' is not possible", e);
                throw new EFapsException(Group.class, "getWithJAASKey.SQLException", e,
                                         _jaasSystem.getName(), _jaasKey);
            } finally {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    Group.LOG.error("Statement could not be closed", e);
                }
            }
            rsrc.commit();
        } finally {
            if ((rsrc != null) && rsrc.isOpened()) {
                rsrc.abort();
            }
        }
        return Group.get(groupId);
    }

    /**
     * Class to cache all group instances.
     */
    private static class GroupCache
        extends AbstractCache<Group>
    {
        /**
         * Reads the information for groups from the database and stores them
         * in the maps for the cache.
         *
         * @param _cache4Id     map between id and group instance used for
         *                      caching
         * @param _cache4Name   map between name and group instance used for
         *                      caching
         * @param _cache4UUID   map between UUID and group instance used for
         *                      caching
         * @throws CacheReloadException if reload of the cache failed
         */
        @Override
        protected void readCache(final Map<Long, Group> _cache4Id,
                                 final Map<String, Group> _cache4Name,
                                 final Map<UUID, Group> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {
                    stmt = con.getConnection().createStatement();
                    final ResultSet resultset = stmt.executeQuery(Group.SQL_SELECT);
                    while (resultset.next()) {
                        final long id = resultset.getLong(1);
                        final String name = resultset.getString(2).trim();
                        final boolean status = resultset.getBoolean(3);
                        Group.LOG.debug("read group '" + name + "' (id = " + id + ")");
                        final Group group = new Group(id, name, status);
                        _cache4Id.put(group.getId(), group);
                        _cache4Name.put(group.getName(), group);
                        _cache4UUID.put(group.getUUID(), group);
                    }
                    resultset.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read groups", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read groups", e);
            } finally  {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read groups", e);
                    }
                }
            }
        }
    }
}

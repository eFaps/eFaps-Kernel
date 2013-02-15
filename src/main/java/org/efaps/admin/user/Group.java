/*
 * Copyright 2003 - 2012 The eFaps Team
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Group.class);

    /**
     * This is the SQL select statement to select a Group from the database by ID.
     */
    private static final String SQL_ID = new SQLSelect().column("ID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERGROUP", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a Group from the database by Name.
     */
    private static final String SQL_NAME = new SQLSelect().column("ID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERGROUP", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = "Group4ID";

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = "Group4Name";


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
        if (InfinispanCache.get().exists(Group.IDCACHE)) {
            InfinispanCache.get().<Long, Group>getCache(Group.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Group>getCache(Group.IDCACHE).addListener(new CacheLogListener(Group.LOG));
        }
        if (InfinispanCache.get().exists(Group.NAMECACHE)) {
            InfinispanCache.get().<String, Group>getCache(Group.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Group>getCache(Group.NAMECACHE).addListener(new CacheLogListener(Group.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Group}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Group}
     * @see #CACHE
     */
    public static Group get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Group> cache = InfinispanCache.get().<Long, Group>getCache(Group.IDCACHE);
        if (!cache.containsKey(_id)) {
            Group.getGroupFromDB(Group.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Group}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Group}
     * @see #CACHE
     */
    public static Group get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Group> cache = InfinispanCache.get().<String, Group>getCache(Group.IDCACHE);
        if (!cache.containsKey(_name)) {
            Group.getGroupFromDB(Group.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * @param _group Group to be cached
     */
    private static void cacheGroup(final Group _group) {
        final Cache<String, Group> nameCache = InfinispanCache.get().<String, Group>getCache(Group.NAMECACHE);
        if (!nameCache.containsKey(_group.getName())) {
            nameCache.put(_group.getName(), _group);
        }
        final Cache<Long, Group> idCache = InfinispanCache.get().<Long, Group>getCache(Group.IDCACHE);
        if (!idCache.containsKey(_group.getId())) {
            idCache.put(_group.getId(), _group);
        }
    }

    /**
     * @param _sqlId
     * @param _id
     */
    private static void getGroupFromDB(final String _sql,
                                       final Object _criteria)
        throws CacheReloadException
    {
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(2).trim();
                    final boolean status = rs.getBoolean(3);
                    Group.LOG.debug("read group '" + name + "' (id = " + id + ")");
                    final Group group = new Group(id, name, status);
                    Group.cacheGroup(group);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read Groups", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read Groups", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read Groups", e);
                }
            }
        }
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
}

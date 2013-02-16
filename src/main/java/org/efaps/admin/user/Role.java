/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
 * Class represents the instance of a role in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Role
    extends AbstractUserObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Role.class);

    /**
     * This is the SQL select statement to select a role from the database by ID.
     */
    private static final String SQL_ID = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERROLE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by Name.
     */
    private static final String SQL_NAME = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERROLE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by UUID.
     */
    private static final String SQL_UUID = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERROLE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = "Role4UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = "Role4ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = "Role4Name";

    /**
     * Use to mark not found and return <code>null</code>.
     */
    private static final Role NULL = new Role(0, null, null, false);

    /**
     * Create a new role instance. The method is used from the static method
     * {@link #initialize()} to read all roles from the database.
     *
     * @param _id       id of the role
     * @param _uuid     uuid of the role
     * @param _name     name of the role
     * @param _status   status of the role
     */
    private Role(final long _id,
                 final String _uuid,
                 final String _name,
                 final boolean _status)
    {
        super(_id, _uuid, _name, _status);
    }

    /**
     * Checks, if the given person is assigned to this role.
     *
     * @param _person   person to test
     * @return <i>true</i> if the person is assigned to this role, otherwise
     *         <i>false</i>
     * @see Person#isAssigned(Role)
     */
    @Override
    public boolean hasChildPerson(final Person _person)
    {
        return _person.isAssigned(this);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Role.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Role>getCache(Role.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Role>getCache(Role.UUIDCACHE).addListener(new CacheLogListener(Role.LOG));
        }
        if (InfinispanCache.get().exists(Role.IDCACHE)) {
            InfinispanCache.get().<Long, Role>getCache(Role.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Role>getCache(Role.IDCACHE).addListener(new CacheLogListener(Role.LOG));
        }
        if (InfinispanCache.get().exists(Role.NAMECACHE)) {
            InfinispanCache.get().<String, Role>getCache(Role.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Role>getCache(Role.NAMECACHE).addListener(new CacheLogListener(Role.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Role}.
     *
     * @param _id  id to search in the cache
     * @return instance of class {@link Role}
     * @throws CacheReloadException on error
     * @see #CACHE
     */
    public static Role get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Role> cache = InfinispanCache.get().<Long, Role>getCache(Role.IDCACHE);
        if (!cache.containsKey(_id) && !Role.getRoleFromDB(Role.SQL_ID, _id)) {
            cache.put(_id, Role.NULL, 100, TimeUnit.SECONDS);
        }
        final Role ret = cache.get(_id);
        return ret.equals(Role.NULL) ? null : ret;
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Role}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Role}
     * @throws CacheReloadException on error
     * @see #CACHE
     */
    public static Role get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Role> cache = InfinispanCache.get().<String, Role>getCache(Role.NAMECACHE);
        if (!cache.containsKey(_name) && !Role.getRoleFromDB(Role.SQL_NAME, _name)) {
            cache.put(_name, Role.NULL, 100, TimeUnit.SECONDS);
        }
        final Role ret = cache.get(_name);
        return ret.equals(Role.NULL) ? null : ret;
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Role}.
     *
     * @param _uuid UUI to search for
     * @return instance of class {@link Role}
     * @throws CacheReloadException on error
     * @see #CACHE
     */
    public static Role get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, Role> cache = InfinispanCache.get().<UUID, Role>getCache(Role.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Role.getRoleFromDB(Role.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _role Role to be cached
     */
    private static void cacheRole(final Role _role) {
        final Cache<UUID, Role> cache4UUID = InfinispanCache.get().<UUID, Role>getCache(Role.UUIDCACHE);
        if (!cache4UUID.containsKey(_role.getUUID())) {
            cache4UUID.put(_role.getUUID(), _role);
        }

        final Cache<String, Role> nameCache = InfinispanCache.get().<String, Role>getCache(Role.NAMECACHE);
        if (!nameCache.containsKey(_role.getName())) {
            nameCache.put(_role.getName(), _role);
        }
        final Cache<Long, Role> idCache = InfinispanCache.get().<Long, Role>getCache(Role.IDCACHE);
        if (!idCache.containsKey(_role.getId())) {
            idCache.put(_role.getId(), _role);
        }
    }

    /**
     * @param _sqlId
     * @param _id
     */
    private static boolean getRoleFromDB(final String _sql,
                                      final Object _criteria) throws CacheReloadException
    {
        boolean ret = false;
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
                    final String uuid = rs.getString(2);
                    final String name = rs.getString(3).trim();
                    final boolean status = rs.getBoolean(4);

                    Role.LOG.debug("read role '" + name + "' (id = " + id + ")");
                    final Role role = new Role(id, uuid, name, status);
                    Role.cacheRole(role);
                }
                ret = true;
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read roles", e);
                }
            }
        }
        return ret;
    }

    /**
     * Returns for given parameter <code>_jaasKey</code> the instance of class
     * {@link Role}. The parameter <code>_jaasKey</code> is the name of the
     * role used in the given JAAS system for the role.
     *
     * @param _jaasSystem   JAAS system for which the JAAS key is named
     * @param _jaasKey      key in the foreign JAAS system for which the role is
     *                      searched
     * @throws EFapsException on error
     * @return instance of class {@link Role}, or <code>null</code> if role is
     *         not found
     * @see #get(long)
     */
    public static Role getWithJAASKey(final JAASSystem _jaasSystem,
                                      final String _jaasKey)
        throws EFapsException
    {
        long roleId = 0;
        ConnectionResource rsrc = null;
        try {
            rsrc = Context.getThreadContext().getConnectionResource();

            Statement stmt = null;

            try {
                final StringBuilder cmd = new StringBuilder()
                    .append("select ").append("ID ").append("from V_USERROLEJASSKEY ")
                    .append("where JAASKEY='").append(_jaasKey).append("' ")
                    .append("and JAASSYSID=").append(_jaasSystem.getId());

                stmt = rsrc.getConnection().createStatement();
                final ResultSet resultset = stmt.executeQuery(cmd.toString());
                if (resultset.next()) {
                    roleId = resultset.getLong(1);
                }
                resultset.close();

            } catch (final SQLException e) {
                Role.LOG.warn("search for role for JAAS system '" + _jaasSystem.getName()
                        + "' with key '" + _jaasKey + "' is not possible", e);
                throw new EFapsException(Role.class, "getWithJAASKey.SQLException", e,
                        _jaasSystem.getName(), _jaasKey);
            } finally {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    Role.LOG.warn("Catched SQLException in class " + Role.class);
                }
            }
            rsrc.commit();
        } finally {
            if ((rsrc != null) && rsrc.isOpened()) {
                rsrc.abort();
            }
        }
        return Role.get(roleId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Role) {
            ret = ((Role) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }
}

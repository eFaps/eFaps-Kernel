/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */


package org.efaps.admin.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * A relation mapping between a Group and a Role providing a 2 dimensional
 * access and right definition. e.g.<br/>
 * A Person has the right of Sales_Admin (Role) in a Department (Group)
 *
 * @author The eFaps Team
 *
 */
public class Association
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Association.class);

    /**
     * This is the SQL select statement to select a role from the database by ID.
     */
    private static final String SQL_ID = new SQLSelect().column("ID")
                    .column("ROLEID")
                    .column("GROUPID")
                    .from("T_USERASSOC", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = Association.class.getName() + ".ID";

    /**
     * Use to mark not found and return <code>null</code>.
     */
    private static final Association NULL = new Association(0, 0, 0);

    /**
     * Id of this Association.
     */
    private final long id;

    /**
     * The id of the Role.
     */
    private final long roleId;

    /**
     * The id of the group.
     */
    private final long groupId;

    /**
     * @param _id       id of this Association
     * @param _roleId   related role
     * @param _groupId  related group
     */
    public Association(final long _id,
                       final long _roleId,
                       final long _groupId)
    {
        this.id = _id;
        this.roleId = _roleId;
        this.groupId = _groupId;
    }

    /**
     * Getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    protected long getId()
    {
        return this.id;
    }

    /**
     * Get the Role.
     * @return Role for this Association.
     * @throws CacheReloadException on error
     */
    public Role getRole()
        throws CacheReloadException
    {
        return Role.get(this.roleId);
    }

    /**
     * Get the Group.
     * @return Group for this Association.
     * @throws CacheReloadException on error
     */
    public Group getGroup()
        throws CacheReloadException
    {
        return Group.get(this.groupId);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Association.IDCACHE)) {
            InfinispanCache.get().<Long, Association>getCache(Association.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Association>getCache(Association.IDCACHE)
                            .addListener(new CacheLogListener(Association.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Association}.
     *
     * @param _id  id to search in the cache
     * @return instance of class {@link Association}
     * @throws CacheReloadException on error
     */
    public static Association get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Association> cache = InfinispanCache.get().<Long, Association>getCache(Association.IDCACHE);
        if (!cache.containsKey(_id) && !Association.getAssociationFromDB(Association.SQL_ID, _id)) {
            cache.put(_id, Association.NULL, 100, TimeUnit.SECONDS);
        }
        final Association ret = cache.get(_id);
        return ret.equals(Association.NULL) ? null : ret;
    }

    /**
     * @param _association Association to be cached
     */
    private static void cacheAssociation(final Association _association)
    {
        final Cache<Long, Association> idCache = InfinispanCache.get().<Long, Association>getIgnReCache(
                        Association.IDCACHE);
        idCache.putIfAbsent(_association.getId(), _association);

    }

    /**
     * @param _sql      SQL Statment to be execuetd
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getAssociationFromDB(final String _sql,
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
                    final long roleId = rs.getLong(2);
                    final long groupId = rs.getLong(3);

                    Association.LOG.debug("read association id: {}, roleId: {}, groupId: {}", id, roleId, groupId);
                    final Association association = new Association(id, roleId, groupId);
                    Association.cacheAssociation(association);
                    ret = true;
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read Association", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read Association", e);
        } finally {
            if ((con != null) && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read Association", e);
                }
            }
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Association) {
            ret = ((Association) _obj).id == this.id;
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
        return Long.valueOf(this.id).intValue();
    }
}

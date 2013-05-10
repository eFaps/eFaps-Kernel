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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Company
    extends AbstractUserObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Company.class);

    /**
     * This is the SQL select statement to select a role from the database by ID.
     */
    private static final String SQL_ID = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERCOMPANY", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by Name.
     */
    private static final String SQL_NAME = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERCOMPANY", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by UUID.
     */
    private static final String SQL_UUID = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERCOMPANY", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = "Company4UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = "Company4ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = "Company4Name";

    /**
     * Use to mark not found and return <code>null</code>.
     */
    private static final Company NULL = new Company(0, null, null, false);

    /**
     * The company belonging to this Consortiums.
     */
    private final Set<Long> consortiumIds = new HashSet<Long>();

    /**
     * @param _id       id for this company
     * @param _uuid     uuid for this company
     * @param _name     name for this company
     * @param _status    status for this company
     */
    private Company(final long _id,
                    final String _uuid,
                    final String _name,
                    final boolean _status)
    {
        super(_id, _uuid, _name, _status);
    }

    /**
     * @param _consortiumId Consortium to add to this Company
     */
    protected void addConsortium(final Long _consortiumId)
    {
        this.consortiumIds.add(_consortiumId);
    }

    /**
     * Get the related Consortium (unmodifiable).
     * @return the set of related Consortiums
     */
    public Set<Long> getConsortiums()
    {
        return Collections.unmodifiableSet(this.consortiumIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildPerson(final Person _person)
    {
        return _person.isAssigned(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssigned()
    {
        boolean ret = false;
        try {
            if (Context.getThreadContext().getCompany() != null) {
                if (Context.getThreadContext().getCompany().getId() == getId()) {
                    ret = hasChildPerson(Context.getThreadContext().getPerson());
                }
            } else {
                ret = true;
            }
        } catch (final EFapsException e) {
            Company.LOG.error("could not read Company or Person from Context ", e);
        }
        return ret;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Company.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Company>getCache(Company.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Company>getCache(Company.UUIDCACHE)
                            .addListener(new CacheLogListener(Company.LOG));
        }
        if (InfinispanCache.get().exists(Company.IDCACHE)) {
            InfinispanCache.get().<Long, Company>getCache(Company.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Company>getCache(Company.IDCACHE)
                            .addListener(new CacheLogListener(Company.LOG));
        }
        if (InfinispanCache.get().exists(Company.NAMECACHE)) {
            InfinispanCache.get().<String, Company>getCache(Company.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Company>getCache(Company.NAMECACHE)
                            .addListener(new CacheLogListener(Company.LOG));
        }
    }

   /**
    * Returns for given parameter <i>_id</i> the instance of class {@link Company}.
    *
    * @param _id    id to search in the cache
    * @return instance of class {@link Company}
    * @throws CacheReloadException on error
    * @see #getCache
    */
    public static Company get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Company> cache = InfinispanCache.get().<Long, Company>getCache(Company.IDCACHE);
        if (!cache.containsKey(_id) && !Company.getCompanyFromDB(Company.SQL_ID, _id)) {
            cache.put(_id, Company.NULL, 100, TimeUnit.SECONDS);
        }
        final Company ret = cache.get(_id);
        return ret.equals(Company.NULL) ? null : ret;
    }

   /**
    * Returns for given parameter <i>_name</i> the instance of class
    * {@link Company}.
    *
    * @param _name   name to search in the cache
    * @return instance of class {@link Company}
    * @throws CacheReloadException on error
    * @see #getCache
    */
    public static Company get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Company> cache = InfinispanCache.get().<String, Company>getCache(Company.NAMECACHE);
        if (!cache.containsKey(_name) && !Company.getCompanyFromDB(Company.SQL_NAME, _name)) {
            cache.put(_name, Company.NULL, 100, TimeUnit.SECONDS);
        }
        final Company ret = cache.get(_name);
        return ret.equals(Company.NULL) ? null : ret;
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Company}.
     * @param _uuid UUI to search for
     * @return instance of class {@link Company}
     * @throws CacheReloadException on error
     */
    public static Company get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, Company> cache = InfinispanCache.get().<UUID, Company>getCache(Company.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Company.getCompanyFromDB(Company.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _role Company to be cached
     */
    private static void cacheCompany(final Company _role)
    {
        final Cache<UUID, Company> cache4UUID = InfinispanCache.get().<UUID, Company>getCache(Company.UUIDCACHE);
        if (!cache4UUID.containsKey(_role.getUUID())) {
            cache4UUID.put(_role.getUUID(), _role);
        }

        final Cache<String, Company> nameCache = InfinispanCache.get().<String, Company>getCache(Company.NAMECACHE);
        if (!nameCache.containsKey(_role.getName())) {
            nameCache.put(_role.getName(), _role);
        }
        final Cache<Long, Company> idCache = InfinispanCache.get().<Long, Company>getCache(Company.IDCACHE);
        if (!idCache.containsKey(_role.getId())) {
            idCache.put(_role.getId(), _role);
        }
    }

    /**
     * @param _sql      SQL Statment to be execuetd
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getCompanyFromDB(final String _sql,
                                            final Object _criteria)
        throws CacheReloadException
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

                    Company.LOG.debug("read company '" + name + "' (id = " + id + ")");
                    final Company role = new Company(id, uuid, name, status);
                    Company.cacheCompany(role);
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
}

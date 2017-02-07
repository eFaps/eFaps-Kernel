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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class Consortium
    extends AbstractUserObject
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Consortium.class);

    /**
     * This is the sql select statement to select all consortium 2 company
     * relations from the database.
     */
    private static final String SQL_SELECTREL = new SQLSelect()
                    .column("USERABSTRACTTO")
                    .from("V_CONSORTIUM2COMPANY")
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a Consortium from the database
     * by ID.
     */
    private static final String SQL_ID = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERCONSORTIUM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERCONSORTIUM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect().column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("STATUS")
                    .from("V_USERCONSORTIUM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = Consortium.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = Consortium.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = Consortium.class.getName() + ".Name";

    /**
     * Use to mark not found and return <code>null</code>.
     */
    private static final Consortium NULL = new Consortium(0, null, null, false);

    /**
     * The companies belonging to this Consortium.
     */
    private final Set<Long> companieIds = new HashSet<>();

    /**
     * @param _id id for this company
     * @param _uuid uuid for this company
     * @param _name name for this company
     * @param _status status for this company
     */
    private Consortium(final long _id,
                       final String _uuid,
                       final String _name,
                       final boolean _status)
    {
        super(_id, _uuid, _name, _status);
    }

    /**
     * @param _companyId ID of the Company to add to this Consortium
     */
    protected void addCompany(final Long _companyId)
    {
        this.companieIds.add(_companyId);
    }

    /**
     * Get the related Companies (unmodifiable).
     *
     * @return the set of related Companies
     */
    public Set<Long> getCompanies()
    {
        return Collections.unmodifiableSet(this.companieIds);
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
            Consortium.LOG.error("could not read Company or Person from Context ", e);
        }
        return ret;
    }

    @Override
    public boolean hasChildPerson(final Person _person)
    {
        return false;
    }

    @Override
    public Instance getInstance()
    {
        return Instance.get(CIAdminUser.Consortium.getType(), getId());
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof Consortium) {
            ret = ((Consortium) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Consortium.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Consortium>getCache(Consortium.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Consortium>getCache(Consortium.UUIDCACHE)
                            .addListener(new CacheLogListener(Consortium.LOG));
        }
        if (InfinispanCache.get().exists(Consortium.IDCACHE)) {
            InfinispanCache.get().<Long, Consortium>getCache(Consortium.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Consortium>getCache(Consortium.IDCACHE)
                            .addListener(new CacheLogListener(Consortium.LOG));
        }
        if (InfinispanCache.get().exists(Consortium.NAMECACHE)) {
            InfinispanCache.get().<String, Consortium>getCache(Consortium.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Consortium>getCache(Consortium.NAMECACHE)
                            .addListener(new CacheLogListener(Consortium.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Consortium}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Consortium}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static Consortium get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Consortium> cache = InfinispanCache.get().<Long, Consortium>getCache(Consortium.IDCACHE);
        if (!cache.containsKey(_id) && !Consortium.getConsortiumFromDB(Consortium.SQL_ID, _id)) {
            cache.put(_id, Consortium.NULL, 100, TimeUnit.SECONDS);
        }
        final Consortium ret = cache.get(_id);
        return ret.equals(Consortium.NULL) ? null : ret;
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Consortium}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Consortium}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static Consortium get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Consortium> cache = InfinispanCache.get().<String, Consortium>getCache(Consortium.IDCACHE);
        if (!cache.containsKey(_name) && !Consortium.getConsortiumFromDB(Consortium.SQL_NAME, _name)) {
            cache.put(_name, Consortium.NULL, 100, TimeUnit.SECONDS);
        }
        final Consortium ret = cache.get(_name);
        return ret.equals(Consortium.NULL) ? null : ret;
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Consortium}.
     *
     * @param _uuid UUI to search for
     * @return instance of class {@link Consortium}
     * @throws CacheReloadException on error
     */
    public static Consortium get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, Consortium> cache = InfinispanCache.get().<UUID, Consortium>getCache(Consortium.IDCACHE);
        if (!cache.containsKey(_uuid)) {
            Consortium.getConsortiumFromDB(Consortium.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _consortium Consortium to be cached
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED")
    private static void cacheConsortium(final Consortium _consortium)
    {
        final Cache<UUID, Consortium> cache4UUID = InfinispanCache.get().<UUID, Consortium>getIgnReCache(
                        Consortium.UUIDCACHE);
        cache4UUID.putIfAbsent(_consortium.getUUID(), _consortium);

        final Cache<String, Consortium> nameCache = InfinispanCache.get().<String, Consortium>getIgnReCache(
                        Consortium.NAMECACHE);
        nameCache.put(_consortium.getName(), _consortium);

        final Cache<Long, Consortium> idCache = InfinispanCache.get().<Long, Consortium>getIgnReCache(
                        Consortium.IDCACHE);
        idCache.put(_consortium.getId(), _consortium);
    }

    /**
     * @param _sql SQL Statment to be execuetd
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getConsortiumFromDB(final String _sql,
                                               final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        Connection con = null;
        try {
            Consortium consortium = null;
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String uuid = rs.getString(2);
                    final String name = rs.getString(3).trim();
                    final boolean status = rs.getBoolean(4);
                    Consortium.LOG.debug("read consortium '" + name + "' (id = " + id + ")");
                    consortium = new Consortium(id, uuid, name, status);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            if (consortium != null) {
                ret = true;
                Consortium.cacheConsortium(consortium);
                consortium.getCompanyRelationFromDB();
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read consortiums", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read consortiums", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }

    /**
     * @throws CacheReloadException on error
     */
    private void getCompanyRelationFromDB()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            final List<Long> companyIds = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(Consortium.SQL_SELECTREL);
                stmt.setObject(1, getId());
                final ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    companyIds.add(rs.getLong(1));
                }
                rs.close();

            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Long companyId : companyIds) {
                final Company company = Company.get(companyId);
                Consortium.LOG.debug("read consortium 2 company relation '{} - {}'",
                                new Object[] { getName(), company.getName() });
                if (company != null) {
                    addCompany(company.getId());
                    company.addConsortium(getId());
                }
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read consortiums", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read consortiums", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

}

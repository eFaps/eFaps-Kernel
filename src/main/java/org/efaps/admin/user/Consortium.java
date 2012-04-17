/*
 * Copyright 2003 - 2011 The eFaps Team
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Consortium
    extends AbstractUserObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Consortium.class);

    /**
     * This is the sql select statement to select all consortium from the database.
     */
    private static final SQLSelect SQL_SELECT = new SQLSelect()
                                                        .column("ID")
                                                        .column("UUID")
                                                        .column("NAME")
                                                        .column("STATUS")
                                                        .from("V_USERCONSORTIUM");

    /**
     * This is the sql select statement to select all consortium 2 company
     * relations from the database.
     */
    private static final SQLSelect SQL_SELECTREL = new SQLSelect()
                                                        .column("ID")
                                                        .column("USERABSTRACTFROM")
                                                        .column("USERABSTRACTTO")
                                                        .column("JAASSYSID")
                                                        .from("V_CONSORTIUM2COMPANY");

    /**
     * Stores all instances of class {@link Consortium}.
     *
     * @see #getCache
     */
    private static final ConsortiumCache CACHE = new ConsortiumCache();

    /**
     * The companies belonging to this Consortium.
     */
    private final Set<Company> companies = new HashSet<Company>();

    /**
     * @param _id       id for this company
     * @param _uuid     uuid for this company
     * @param _name     name for this company
     * @param _status    status for this company
     */
    private Consortium(final long _id,
                       final String _uuid,
                       final String _name,
                       final boolean _status)
    {
        super(_id, _uuid, _name, _status);
    }

    /**
     * @param _company Company to add to this Consortium
     */
    protected void addCompany(final Company _company)
    {
        this.companies.add(_company);
    }

    /**
     * Get the related Companies (unmodifiable).
     * @return the set of related Companies
     */
    public Set<Company> getCompanies()
    {
        return Collections.unmodifiableSet(this.companies);
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

    /* (non-Javadoc)
     * @see org.efaps.admin.user.AbstractUserObject#hasChildPerson(org.efaps.admin.user.Person)
     */
    @Override
    public boolean hasChildPerson(final Person _person)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        Consortium.CACHE.initialize(Consortium.class);
    }

   /**
    * Returns for given parameter <i>_id</i> the instance of class {@link Consortium}.
    *
    * @param _id    id to search in the cache
    * @return instance of class {@link Consortium}
    * @throws CacheReloadException on error
    * @see #getCache
    */
    public static Consortium get(final long _id)
        throws CacheReloadException
    {
        return Consortium.CACHE.get(_id);
    }

   /**
    * Returns for given parameter <i>_name</i> the instance of class
    * {@link Consortium}.
    *
    * @param _name   name to search in the cache
    * @return instance of class {@link Consortium}
    * @throws CacheReloadException on error
    * @see #getCache
    */
    public static Consortium get(final String _name)
        throws CacheReloadException
    {
        return Consortium.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Consortium}.
     * @param _uuid UUI to search for
     * @return instance of class {@link Consortium}
     * @throws CacheReloadException on error
     */
    public static Consortium get(final UUID _uuid)
        throws CacheReloadException
    {
        return Consortium.CACHE.get(_uuid);
    }

    /**
     * Method to get the Cache for Company.
     * @return Cache
     */
    public static AbstractCache<Consortium> getCache()
    {
        return Consortium.CACHE;
    }

    /**
     * Cache for Companies.
     */
    private static final class ConsortiumCache
        extends AbstractCache<Consortium>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, Consortium> _cache4Id,
                                 final Map<String, Consortium> _cache4Name,
                                 final Map<UUID, Consortium> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {
                    stmt = con.getConnection().createStatement();
                    final ResultSet resulset = stmt.executeQuery(Consortium.SQL_SELECT.getSQL());
                    while (resulset.next()) {
                        final long id = resulset.getLong(1);
                        final String uuid = resulset.getString(2);
                        final String name = resulset.getString(3).trim();
                        final boolean status = resulset.getBoolean(4);

                        Consortium.LOG.debug("read consortium '" + name + "' (id = " + id + ")");
                        final Consortium consortium = new Consortium(id, uuid, name, status);
                        _cache4Id.put(consortium.getId(), consortium);
                        _cache4Name.put(consortium.getName(), consortium);
                        _cache4UUID.put(consortium.getUUID(), consortium);
                    }
                    resulset.close();

                    final ResultSet relResulset = stmt.executeQuery(Consortium.SQL_SELECTREL.getSQL());
                    while (relResulset.next()) {
                        final long id = relResulset.getLong(1);
                        final long consortiumId = relResulset.getLong(2);
                        final long companyId = relResulset.getLong(3);
                        final Consortium consortium = Consortium.CACHE.get(consortiumId);
                        final Company company = Company.getCache().get(companyId);
                        Consortium.LOG.debug("read consortium 2 company relation '{} - {}' (relationid = {})",
                                        new Object[] { consortium.getName(), company.getName(), id});
                        consortium.addCompany(company);
                        company.addConsortium(consortium);
                    }
                    relResulset.close();

                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read consortiums", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read consortiums", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read consortiums", e);
                    }
                }
            }
        }
    }


}

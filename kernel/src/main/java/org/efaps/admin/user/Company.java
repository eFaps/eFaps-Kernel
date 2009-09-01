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

package org.efaps.admin.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Company extends AbstractUserObject
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Company.class);

    /**
     * This is the sql select statement to select all roles from the database.
     */
    private static final String SQL_SELECT = "select ID, UUID, NAME, STATUS from V_USERCOMPANY";

    /**
     * Stores all instances of class {@link Company}.
     *
     * @see #getCache
     */
    private static final CompanyCache CACHE = new CompanyCache();

    /**
     * @param _id       id for this company
     * @param _uuid     uuid for this company
     * @param _name     name for this company
     * @param _status    status for this company
     */
    private Company(final long _id, final String _uuid, final String _name, final boolean _status)
    {
        super(_id, _uuid, _name, _status);
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
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        Company.CACHE.initialize(Company.class);
    }

   /**
    * Returns for given parameter <i>_id</i> the instance of class {@link Company}.
    *
    * @param _id    id to search in the cache
    * @return instance of class {@link Company}
    * @throws CacheReloadException on error
    * @see #getCache
    */
    public static Company get(final long _id) throws CacheReloadException
    {
        return Company.CACHE.get(_id);
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
    public static Company get(final String _name) throws CacheReloadException
    {
        return Company.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Company}.
     * @param _uuid UUI to search for
     * @return instance of class {@link Company}
     * @throws CacheReloadException on error
     */
    public static Company get(final UUID _uuid) throws CacheReloadException
    {
        return Company.CACHE.get(_uuid);
    }

    /**
     * Method to get the Cache for Company.
     * @return Cache
     */
    public static Cache<Company> getCache()
    {
        return Company.CACHE;
    }

    /**
     * Cache for Companies.
     */
    private static final class CompanyCache extends Cache<Company>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, Company> _cache4Id, final Map<String, Company> _cache4Name,
                                 final Map<UUID, Company> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {

                    stmt = con.getConnection().createStatement();

                    final ResultSet resulset = stmt.executeQuery(Company.SQL_SELECT);
                    while (resulset.next()) {
                        final long id = resulset.getLong(1);
                        final String uuid = resulset.getString(2);
                        final String name = resulset.getString(3).trim();
                        final boolean status = resulset.getBoolean(4);

                        Company.LOG.debug("read company '" + name + "' (id = " + id + ")");
                        final Company company = new Company(id, uuid, name, status);
                        _cache4Id.put(company.getId(), company);
                        _cache4Name.put(company.getName(), company);
                        _cache4UUID.put(company.getUUID(), company);
                    }
                    resulset.close();
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
        }
    }
}

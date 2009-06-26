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

package org.efaps.admin.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.AbstractAdminObject;
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
public class Dimension extends AbstractAdminObject
{
    /**
     * This is the sql select statement to select all dimension from the
     * database.
     *
     * @see #initialise
     */
    private static final String SQL_SELECT_DIM = "select ID, NAME, UUID, DESCR, BASEUOM from T_DMDIM";

    private static final String SQL_SELECT_UOM = "select ID, DIMID, NAME, NUMERATOR, DENOMINATOR  from T_DMUOM";

    /**
     * Stores all instances of type.
     *
     * @see #get
     */
    private static DimensionCache CACHE = new DimensionCache();

    private static Map<Long, UoM> ID2UOM = new HashMap<Long, UoM>();
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Dimension.class);

    private final List<UoM> uoMs = new ArrayList<UoM>();

    private final long baseUoMId;

    private UoM baseUoM;

    /**
     * @param _id
     * @param _uuid
     * @param _name
     */
    protected Dimension(final long _id, final String _uuid, final String _name, final String _description,
                    final long _baseUoMId)
    {
        super(_id, _uuid, _name);
        this.baseUoMId = _baseUoMId;
    }

    /**
     * @param _uom
     */
    public void addUoM(final UoM _uom)
    {
        this.uoMs.add(_uom);
        if (_uom.getId() == this.baseUoMId) {
            this.baseUoM = _uom;
        }
    }

    /**
     * Getter method for instance variable {@link #uoMs}.
     *
     * @return value of instance variable {@link #uoMs}
     */
    public List<UoM> getUoMs()
    {
        return this.uoMs;
    }

    /**
     * Getter method for instance variable {@link #baseUoM}.
     *
     * @return value of instance variable {@link #baseUoM}
     */
    public UoM getBaseUoM()
    {
        return this.baseUoM;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @param _class class that called the method
     * @throws CacheReloadException on error
     */
    public static void initialize(final Class<?> _class) throws CacheReloadException
    {
        Dimension.CACHE.initialize(_class);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize() throws CacheReloadException
    {
        Dimension.initialize(Dimension.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Dimension}
     * .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Dimension}
     * @throws CacheReloadException
     */
    public static Dimension get(final long _id)
    {
        return Dimension.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Dimension}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Dimension}
     * @throws CacheReloadException
     */
    public static Dimension get(final String _name)
    {
        return Dimension.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Dimension}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Dimension}
     * @throws CacheReloadException
     */
    public static Dimension get(final UUID _uuid)
    {
        return Dimension.CACHE.get(_uuid);
    }

    /**
     * Static getter method for the type hashtable {@link #CACHE}.
     *
     * @return value of static variable {@link #CACHE}
     */
    public static Cache<Dimension> getTypeCache()
    {
        return Dimension.CACHE;
    }


    public static UoM getUoM(final Long _uoMId) {
       return Dimension.ID2UOM.get(_uoMId);
    }

    public class UoM
    {

        private final long id;
        private final long dimId;
        private final String name;
        private final int numerator;
        private final int denominator;

        /**
         * @param id
         * @param dimId
         * @param name
         * @param numerator
         * @param denominator
         */
        protected UoM(final long id, final long dimId, final String name, final int numerator, final int denominator)
        {
            this.id = id;
            this.dimId = dimId;
            this.name = name;
            this.numerator = numerator;
            this.denominator = denominator;
        }

        /**
         * Getter method for instance variable {@link #id}.
         *
         * @return value of instance variable {@link #id}
         */
        public long getId()
        {
            return this.id;
        }

        /**
         * Getter method for instance variable {@link #dimId}.
         *
         * @return value of instance variable {@link #dimId}
         */
        public long getDimId()
        {
            return this.dimId;
        }

        /**
         * Getter method for instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Getter method for instance variable {@link #numerator}.
         *
         * @return value of instance variable {@link #numerator}
         */
        public int getNumerator()
        {
            return this.numerator;
        }

        /**
         * Getter method for instance variable {@link #denominator}.
         *
         * @return value of instance variable {@link #denominator}
         */
        public int getDenominator()
        {
            return this.denominator;
        }

        public Dimension getDimension() {
            return Dimension.get(this.dimId);
        }
    }

    private static class DimensionCache extends Cache<Dimension>
    {
        /**
         * @see org.efaps.util.cache.Cache#readCache(java.util.Map, java.util.Map, java.util.Map)
         * @param _cache4Id     cache for id
         * @param _cache4Name   cache for name
         * @param _cache4UUID   cache for uuid
         * @throws CacheReloadException on error
         */
        @Override
        protected void readCache(final Map<Long, Dimension> _cache4Id, final Map<String, Dimension> _cache4Name,
                        final Map<UUID, Dimension> _cache4UUID) throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {

                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(Dimension.SQL_SELECT_DIM);
                    while (rs.next()) {
                        final long id = rs.getLong(1);
                        final String name = rs.getString(2).trim();
                        final String uuid = rs.getString(3).trim();
                        final String descr = rs.getString(4).trim();
                        final long baseuom = rs.getLong(5);

                        if (Dimension.LOG.isDebugEnabled()) {
                            Dimension.LOG.debug("read dimension '" + name + "' (id = " + id + ")");
                        }

                        final Dimension dim = new Dimension(id, uuid, name, descr, baseuom);

                        _cache4Id.put(dim.getId(), dim);
                        _cache4Name.put(dim.getName(), dim);
                        _cache4UUID.put(dim.getUUID(), dim);

                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }

                Statement stmt2 = null;
                try {

                    stmt2 = con.getConnection().createStatement();

                    final ResultSet rs = stmt2.executeQuery(Dimension.SQL_SELECT_UOM);
                    while (rs.next()) {
                        final long id = rs.getLong(1);
                        final long dimId = rs.getLong(2);
                        final String name = rs.getString(3).trim();
                        final int numerator = rs.getInt(4);
                        final int denominator = rs.getInt(5);

                        if (Dimension.LOG.isDebugEnabled()) {
                            Dimension.LOG.debug("read dimension '" + name + "' (id = " + id + ")");
                        }
                        final Dimension dim = _cache4Id.get(dimId);
                        final UoM uom = dim.new UoM(id, dimId, name, numerator, denominator);
                        dim.addUoM(uom);
                        Dimension.ID2UOM.put(uom.getId(), uom);
                    }
                    rs.close();
                } finally {
                    if (stmt2 != null) {
                        stmt2.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read Dimension", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read Dimension", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read Dimension", e);
                    }
                }
            }
        }
    }
}

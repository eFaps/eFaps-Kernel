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
 * Class for Dimensions inside eFaps.
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

    /**
     * This is the sql select statement to select all UoM from the
     * database.
     *
     * @see #initialise
     */
    private static final String SQL_SELECT_UOM = "select ID, DIMID, NAME, NUMERATOR, DENOMINATOR  from T_DMUOM";

    /**
     * Stores all instances of type.
     *
     * @see #get
     */
    private static DimensionCache CACHE = new DimensionCache();

    /**
     * Mapping of UoMId to UoM.
     */
    private static Map<Long, UoM> ID2UOM = new HashMap<Long, UoM>();
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Dimension.class);

    /**
     * List of UoM belonging to this Dimension.
     */
    private final List<UoM> uoMs = new ArrayList<UoM>();

    /**
     * Id of the base UoM.
     */
    private final long baseUoMId;

    /**
     * Base UoM.
     */
    private UoM baseUoM;


    /**
     * Constructor.
     * @param _id           id of this dimension
     * @param _uuid         UUID of this dimension
     * @param _name         Name of this dimension
     * @param _description  description for this dimension
     * @param _baseUoMId    id of the base UoM for this dimension
     */
    protected Dimension(final long _id, final String _uuid, final String _name, final String _description,
                        final long _baseUoMId)
    {
        super(_id, _uuid, _name);
        this.baseUoMId = _baseUoMId;
    }

    /**
     * Method to add an UoM to this dimension.
     * @param _uom UoM to add
     */
    private void addUoM(final UoM _uom)
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


    /**
     * Static Method to get an UoM for an id.
     * @param _uoMId    if the UoM is wanted for.
     * @return UoM
     */
    public static UoM getUoM(final Long _uoMId)
    {
        return Dimension.ID2UOM.get(_uoMId);
    }

    /**
     * Class for an UoM. (Unit of Measurement)
     */
    public class UoM
    {
        /**
         * Id of this UoM.
         */
        private final long id;

        /**
         * Id of the dimension this UoM belongs to.
         */
        private final long dimId;

        /**
         * Name of this UoM.
         */
        private final String name;

        /**
         * Numerator for this UoM.
         */
        private final int numerator;

        /**
         * Denominator for this UoM.
         */
        private final int denominator;

        /**
         * @param _id           id of this UoM
         * @param _dimId        Id of the dimension this UoM belongs to
         * @param _name         Name of this UoM
         * @param _numerator    Numerator for this UoM
         * @param _denominator  Denominator for this UoM
         */
        protected UoM(final long _id, final long _dimId, final String _name, final int _numerator,
                      final int _denominator)
        {
            this.id = _id;
            this.dimId = _dimId;
            this.name = _name;
            this.numerator = _numerator;
            this.denominator = _denominator;
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

        /**
         * Method to get the Dimension this UoM belongs to.
         * @return Dimension
         */
        public Dimension getDimension()
        {
            return Dimension.get(this.dimId);
        }

        /**
         * Method to calculate the given value into an base value.
         * @param _value value to be calculated
         * @return calculated base value
         */
        public Double getBaseDouble(final Double _value)
        {
            return _value * this.numerator / this.denominator;
        }
    }

    /**
     * Cache for Dimension.
     *
     */
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

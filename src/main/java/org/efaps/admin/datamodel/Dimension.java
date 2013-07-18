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

package org.efaps.admin.datamodel;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
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
 * Class for Dimensions inside eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Dimension
    extends AbstractAdminObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Dimension.class);

    /**
     * This is the sql select statement to select all UoM from the database.
     *
     * @see #initialise
     */
    private static final String SQL_SELECT_UOM4DIMID = new SQLSelect()
                    .column("ID")
                    .column("DIMID")
                    .column("NAME")
                    .column("NUMERATOR")
                    .column("DENOMINATOR")
                    .from("T_DMUOM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "DIMID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the sql select statement to select all UoM from the database.
     *
     * @see #initialise
     */
    private static final String SQL_SELECT_UOM4ID = new SQLSelect()
                    .column("ID")
                    .column("DIMID")
                    .column("NAME")
                    .column("NUMERATOR")
                    .column("DENOMINATOR")
                    .from("T_DMUOM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("UUID")
                    .column("DESCR")
                    .column("BASEUOM")
                    .from("T_DMDIM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("UUID")
                    .column("DESCR")
                    .column("BASEUOM")
                    .from("T_DMDIM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("UUID")
                    .column("DESCR")
                    .column("BASEUOM")
                    .from("T_DMDIM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = Dimension.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = Dimension.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = Dimension.class.getName() + ".Name";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE4UOM = Dimension.class.getName() + ".UoM4ID";

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
     *
     * @param _id id of this dimension
     * @param _uuid UUID of this dimension
     * @param _name Name of this dimension
     * @param _description description for this dimension
     * @param _baseUoMId id of the base UoM for this dimension
     */
    protected Dimension(final long _id,
                        final String _uuid,
                        final String _name,
                        final String _description,
                        final long _baseUoMId)
    {
        super(_id, _uuid, _name);
        this.baseUoMId = _baseUoMId;
    }

    /**
     * Method to add an UoM to this dimension.
     *
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
    public static void initialize(final Class<?> _class)
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(Dimension.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Dimension>getCache(Dimension.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Dimension>getCache(Dimension.UUIDCACHE)
                            .addListener(new CacheLogListener(Dimension.LOG));
        }
        if (InfinispanCache.get().exists(Dimension.IDCACHE)) {
            InfinispanCache.get().<Long, Dimension>getCache(Dimension.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Dimension>getCache(Dimension.IDCACHE)
                            .addListener(new CacheLogListener(Dimension.LOG));
        }
        if (InfinispanCache.get().exists(Dimension.NAMECACHE)) {
            InfinispanCache.get().<String, Dimension>getCache(Dimension.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Dimension>getCache(Dimension.NAMECACHE)
                            .addListener(new CacheLogListener(Dimension.LOG));
        }
        if (InfinispanCache.get().exists(Dimension.IDCACHE4UOM)) {
            InfinispanCache.get().<Long, UoM>getCache(Dimension.IDCACHE4UOM).clear();
        } else {
            InfinispanCache.get().<Long, UoM>getCache(Dimension.IDCACHE4UOM)
                            .addListener(new CacheLogListener(Dimension.LOG));
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        Dimension.initialize(Dimension.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Dimension} .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Dimension}
     * @throws CacheReloadException on error
     */
    public static Dimension get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, Dimension> cache = InfinispanCache.get().<Long, Dimension>getCache(Dimension.IDCACHE);
        if (!cache.containsKey(_id)) {
            Dimension.getDimensionFromDB(Dimension.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Dimension}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Dimension}
     * @throws CacheReloadException on error
     */
    public static Dimension get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, Dimension> cache = InfinispanCache.get().<String, Dimension>getCache(Dimension.NAMECACHE);
        if (!cache.containsKey(_name)) {
            Dimension.getDimensionFromDB(Dimension.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Dimension}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Dimension}
     * @throws CacheReloadException on error
     */
    public static Dimension get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, Dimension> cache = InfinispanCache.get().<UUID, Dimension>getCache(Dimension.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Dimension.getDimensionFromDB(Dimension.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * Static Method to get an UoM for an id.
     *
     * @param _uoMId if the UoM is wanted for.
     * @return UoM
     */
    public static UoM getUoM(final Long _uoMId)
    {
        final Cache<Long, UoM> cache = InfinispanCache.get().<Long, UoM>getCache(Dimension.IDCACHE4UOM);
        if (!cache.containsKey(_uoMId)) {
            try {
                Dimension.getUoMFromDB(Dimension.SQL_SELECT_UOM4ID, _uoMId);
            } catch (final CacheReloadException e) {
                Dimension.LOG.error("read UoM from DB failed for id: '{}'", _uoMId);
            }
        }
        return cache.get(_uoMId);
    }

    /**
     * @param _sql sql statment to be executed
     * @param _criteria filter criteria
     * @throws CacheReloadException on error
     */
    private static void getUoMFromDB(final String _sql,
                                     final Object _criteria)
        throws CacheReloadException
    {
        ConnectionResource con = null;
        try {
            final List<Object[]> values = new ArrayList<Object[]>();

            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(new Object[] {
                                    rs.getLong(1),
                                    rs.getLong(2),
                                    rs.getString(3).trim(),
                                    rs.getInt(4),
                                    rs.getInt(5)
                    });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            final Cache<Long, UoM> cache = InfinispanCache.get().<Long, UoM>getCache(Dimension.IDCACHE4UOM);
            for (final Object[] row : values) {
                final long id = (Long) row[0];
                final long dimId = (Long) row[1];
                final String name = (String) row[2];
                final int numerator = (Integer) row[3];
                final int denominator = (Integer) row[4];
                Dimension.LOG.debug("read UoM '" + name + "' (id = " + id + ")");
                final Dimension dim = Dimension.get(dimId);
                final UoM uom = new UoM(id, dimId, name, numerator, denominator);
                dim.addUoM(uom);
                cache.put(uom.getId(), uom);
            }
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

    /**
     * @param _role Dimension to be cached
     */
    private static void cacheDimension(final Dimension _role)
    {
        final Cache<UUID, Dimension> cache4UUID = InfinispanCache.get().<UUID, Dimension>getIgnReCache(
                        Dimension.UUIDCACHE);
        cache4UUID.putIfAbsent(_role.getUUID(), _role);

        final Cache<String, Dimension> nameCache = InfinispanCache.get().<String, Dimension>getIgnReCache(
                        Dimension.NAMECACHE);
        nameCache.putIfAbsent(_role.getName(), _role);

        final Cache<Long, Dimension> idCache = InfinispanCache.get().<Long, Dimension>getIgnReCache(
                        Dimension.IDCACHE);
        idCache.putIfAbsent(_role.getId(), _role);
    }

    /**
     * @param _sql sql statement to be executed
     * @param _criteria filter criteria
     * @throws CacheReloadException on error
     * @return false
     */
    private static boolean getDimensionFromDB(final String _sql,
                                              final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            Dimension dim = null;
            con = Context.getThreadContext().getConnectionResource();
            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(2).trim();
                    final String uuid = rs.getString(3).trim();
                    final String descr = rs.getString(4).trim();
                    final long baseuom = rs.getLong(5);
                    Dimension.LOG.debug("read Dimension '" + name + "' (id = " + id + ")");
                    dim = new Dimension(id, uuid, name, descr, baseuom);
                }
                ret = true;
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            if (dim != null) {
                Dimension.cacheDimension(dim);
                Dimension.getUoMFromDB(Dimension.SQL_SELECT_UOM4DIMID, dim.getId());
                // needed due to cluster serialization that does not update automatically
                Dimension.cacheDimension(dim);
            }
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
     * Class for an UoM. (Unit of Measurement)
     */
    public static class UoM
        implements Serializable
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

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
         * @param _id id of this UoM
         * @param _dimId Id of the dimension this UoM belongs to
         * @param _name Name of this UoM
         * @param _numerator Numerator for this UoM
         * @param _denominator Denominator for this UoM
         */
        protected UoM(final long _id,
                      final long _dimId,
                      final String _name,
                      final int _numerator,
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
         *
         * @return Dimension
         */
        public Dimension getDimension()
        {
            Dimension ret = null;
            try {
                ret = Dimension.get(this.dimId);
            } catch (final CacheReloadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return ret;
        }

        /**
         * Method to calculate the given value into an base value.
         *
         * @param _value value to be calculated
         * @return calculated base value
         */
        public Double getBaseDouble(final Double _value)
        {
            return _value * this.numerator / this.denominator;
        }
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof Dimension) {
            ret = ((Dimension) _obj).getId() == getId();
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
}

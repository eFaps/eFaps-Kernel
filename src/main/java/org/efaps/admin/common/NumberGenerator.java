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

package org.efaps.admin.common;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.Locale;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class NumberGenerator
    implements CacheObjectInterface, Serializable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column(0, "ID")
                    .column(1, "NAME")
                    .column(1, "UUID")
                    .column(0, "FORMAT")
                    .from("T_CMNUMGEN", 0)
                    .leftJoin("T_CMABSTRACT", 1, "ID", 0, "ID")
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column(0, "ID")
                    .column(1, "NAME")
                    .column(1, "UUID")
                    .column(0, "FORMAT")
                    .from("T_CMNUMGEN", 0)
                    .leftJoin("T_CMABSTRACT", 1, "ID", 0, "ID")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column(0, "ID")
                    .column(1, "NAME")
                    .column(1, "UUID")
                    .column(0, "FORMAT")
                    .from("T_CMNUMGEN", 0)
                    .leftJoin("T_CMABSTRACT", 1, "ID", 0, "ID")
                    .addPart(SQLPart.WHERE).addColumnPart(1, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = NumberGenerator.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = NumberGenerator.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = NumberGenerator.class.getName() + ".Name";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NumberGenerator.class);

    /**
     * Prefix for the name used in the database.
     */
    private static final String NAME_PREFIX = "numgen_";

    /**
     * Suffix for the name used in the database.
     */
    private static final String NAME_SUFFIX = "_seq";

    /**
     * Id of this NumberGenerator.
     */
    private final long id;

    /**
     * Name of this NumberGenerator.
     */
    private final String name;

    /**
     * UUID of this NumberGenerator.
     */
    private final UUID uuid;

    /**
     * Format for this NumberGenerator.
     */
    private final String format;

    /**
     * @param _id Id of this NumberGenerator.
     * @param _name Name of this NumberGenerator.
     * @param _uuid UUID of this NumberGenerator.
     * @param _format format of this NumberGenerator.
     */
    private NumberGenerator(final long _id,
                            final String _name,
                            final String _uuid,
                            final String _format)
    {
        this.id = _id;
        this.name = _name;
        this.uuid = UUID.fromString(_uuid);
        this.format = _format;
    }

    /**
     * Get the name used for this sequence in the database.
     *
     * @return name of this sequence in the database;
     */
    public String getDBName()
    {
        return NumberGenerator.NAME_PREFIX + this.id + NumberGenerator.NAME_SUFFIX;
    }

    /**
     * Method to get the next value for this sequence. To get the long value use
     * {@link #getNextValAsLong()}
     *
     * @return next value for this sequence
     * @throws EFapsException on error
     */
    public String getNextVal()
        throws EFapsException
    {
        return getNextVal(new Object[0]);
    }

    /**
     * Method to get the next value for this sequence. Including additional
     * format information. To get the long value use {@link #getNextValAsLong()}
     *
     * @param _args arguments for the formatter
     * @return next value for this sequence
     * @throws EFapsException on error
     */
    public String getNextVal(final Object... _args)
        throws EFapsException
    {
        final Object[] args = new Object[_args.length + 1];
        try {
            final long val = Context.getDbType().nextSequence(Context.getThreadContext().getConnectionResource(),
                            getDBName());
            args[0] = val;
        } catch (final SQLException e) {
            throw new EFapsException(NumberGenerator.class, " getNextVal()", e);
        }
        for (int i = 0; i < _args.length; i++) {
            args[i + 1] = _args[i];
        }
        final Locale local = Context.getThreadContext().getLocale();
        final Formatter formatter = new Formatter(local);
        formatter.format(this.format, args);
        final String ret = formatter.toString();
        formatter.close();
        return ret;
    }

    /**
     * Method to get the next long value for this sequence. To get the formated
     * value use {@link #getNextVal()}
     *
     * @return next value for this sequence
     * @throws EFapsException on error
     */
    public Long getNextValAsLong()
        throws EFapsException
    {
        long ret = 0;
        try {
            ret = Context.getDbType().nextSequence(Context.getThreadContext().getConnectionResource(),
                            getDBName());
        } catch (final SQLException e) {
            throw new EFapsException(NumberGenerator.class, " getNextValAsLong()", e);
        }
        return ret;
    }

    /**
     * Set the value for the numberGenerator. The next call of
     * {@link #getNextVal()} or {@link #getNextVal()} normally will return the
     * value + 1.
     *
     * @param _value value for the sequence
     * @throws EFapsException on error
     */
    public void setVal(final String _value)
        throws EFapsException
    {
        Connection con = null;
        try {
            con = Context.getConnection();
            Context.getDbType().setSequence(con, getDBName(), Long.parseLong(_value));
            con.commit();
        } catch (final SQLException e) {
            throw new EFapsException(NumberGenerator.class, "setVal()", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId()
    {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        NumberGenerator.initialize(NumberGenerator.class);
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
        if (InfinispanCache.get().exists(NumberGenerator.UUIDCACHE)) {
            InfinispanCache.get().<UUID, NumberGenerator>getCache(NumberGenerator.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, NumberGenerator>getCache(NumberGenerator.UUIDCACHE)
                            .addListener(new CacheLogListener(NumberGenerator.LOG));
        }
        if (InfinispanCache.get().exists(NumberGenerator.IDCACHE)) {
            InfinispanCache.get().<Long, NumberGenerator>getCache(NumberGenerator.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, NumberGenerator>getCache(NumberGenerator.IDCACHE)
                            .addListener(new CacheLogListener(NumberGenerator.LOG));
        }
        if (InfinispanCache.get().exists(NumberGenerator.NAMECACHE)) {
            InfinispanCache.get().<String, NumberGenerator>getCache(NumberGenerator.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, NumberGenerator>getCache(NumberGenerator.NAMECACHE)
                            .addListener(new CacheLogListener(NumberGenerator.LOG));
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class Type. .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static NumberGenerator get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, NumberGenerator> cache = InfinispanCache.get().<Long, NumberGenerator>getCache(
                        NumberGenerator.IDCACHE);
        if (!cache.containsKey(_id)) {
            NumberGenerator.getNumberGeneratorFromDB(NumberGenerator.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Type}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static NumberGenerator get(final String _name)
        throws CacheReloadException
    {
        final Cache<Long, NumberGenerator> cache = InfinispanCache.get().<Long, NumberGenerator>getCache(
                        NumberGenerator.NAMECACHE);
        if (!cache.containsKey(_name)) {
            NumberGenerator.getNumberGeneratorFromDB(NumberGenerator.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Type}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static NumberGenerator get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, NumberGenerator> cache = InfinispanCache.get().<UUID, NumberGenerator>getCache(
                        NumberGenerator.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            NumberGenerator.getNumberGeneratorFromDB(NumberGenerator.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _numberGenerator NumberGenerator to be cached
     */
    private static void cacheNumberGenerator(final NumberGenerator _numberGenerator)
    {
        final Cache<UUID, NumberGenerator> cache4UUID = InfinispanCache.get().<UUID, NumberGenerator>getIgnReCache(
                        NumberGenerator.UUIDCACHE);
        cache4UUID.putIfAbsent(_numberGenerator.getUUID(), _numberGenerator);

        final Cache<String, NumberGenerator> nameCache = InfinispanCache.get().<String, NumberGenerator>getIgnReCache(
                        NumberGenerator.NAMECACHE);
        nameCache.putIfAbsent(_numberGenerator.getName(), _numberGenerator);

        final Cache<Long, NumberGenerator> idCache = InfinispanCache.get().<Long, NumberGenerator>getIgnReCache(
                        NumberGenerator.IDCACHE);
        idCache.putIfAbsent(_numberGenerator.getId(), _numberGenerator);
    }

    /**
     * @param _sql sql statement to be executed
     * @param _criteria filter criteria
     * @throws CacheReloadException on error
     * @return false
     */
    private static boolean getNumberGeneratorFromDB(final String _sql,
                                                    final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(2).trim();
                    final String uuid = rs.getString(3).trim();
                    final String format = rs.getString(4).trim();
                    NumberGenerator.LOG.debug("read NumberGenerator '{}' (id = {}), format = '{}'", name, id, format);
                    final NumberGenerator generator = new NumberGenerator(id, name, uuid, format);
                    NumberGenerator.cacheNumberGenerator(generator);
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
            try {
                if (con != null && con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof NumberGenerator) {
            ret = ((NumberGenerator) _obj).getId() == getId();
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

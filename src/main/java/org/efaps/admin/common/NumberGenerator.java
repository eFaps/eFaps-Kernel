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

package org.efaps.admin.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class NumberGenerator implements CacheObjectInterface
{

    /**
     * Select statement that will be executed against the database
     * on reading the cache.
     */
    private static String SQL_SELECT = "select t1.ID, NAME, UUID, FORMAT"
        + " from T_CMNUMGEN t1 "
        + " join T_CMABSTRACT t2 on t1.id=t2.id";

    /**
     * Stores all instances of type.
     *
     * @see #get
     */
    private static NumberGeneratorCache CACHE = new NumberGeneratorCache();

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
     * Format for  this NumberGenerator.
     */
    private final String format;

    /**
     * @param _id       Id of this NumberGenerator.
     * @param _name     Name of this NumberGenerator.
     * @param _uuid     UUID of this NumberGenerator.
     * @param _format   format of this NumberGenerator.
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
     * @return name of this sequence in the database;
     */
    public String getDBName()
    {
        return NumberGenerator.NAME_PREFIX + this.id + NumberGenerator.NAME_SUFFIX;
    }

    /**
     * Method to get the next value for this sequence.
     * To get the long value use {@link #getNextValAsLong()}
     * @return next value for this sequence
     * @throws EFapsException on error
     */
    public String getNextVal()
        throws EFapsException
    {
        long ret = 0;
        try {
            ret = Context.getDbType().nextSequence(Context.getThreadContext().getConnection(), getDBName());
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
        formatter.applyPattern(this.format);
        return formatter.format(ret);
    }

    /**
     * Method to get the next long value for this sequence.
     * To get the formated value use {@link #getNextVal()}
     * @return next value for this sequence
     * @throws EFapsException on error
     */
    public Long getNextValAsLong()
        throws EFapsException
    {
        long ret = 0;
        try {
            ret = Context.getDbType().nextSequence(Context.getThreadContext().getConnection(), getDBName());
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Set the value for the numberGenerator. The next call of
     * {@link #getNextVal()} or {@link #getNextVal()} normally will
     * return the value + 1.
     * @param _value value for the sequence
     * @throws EFapsException on error
     */
    public void setVal(final String _value)
        throws EFapsException
    {
        try {
            Context.getDbType().setSequence(Context.getThreadContext().getConnection(), getDBName(), _value);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * {@inheritDoc}
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
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
        NumberGenerator.CACHE.initialize(_class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Type}
     * .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException
     */
    public static NumberGenerator get(final long _id)
    {
        return NumberGenerator.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Type}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException
     */
    public static NumberGenerator get(final String _name)
    {
        return NumberGenerator.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Type}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException
     */
    public static NumberGenerator get(final UUID _uuid)
    {
        return NumberGenerator.CACHE.get(_uuid);
    }



    /**
     * Static getter method for the type hashtable {@link #CACHE}.
     *
     * @return value of static variable {@link #CACHE}
     */
    public static Cache<NumberGenerator> getTypeCache()
    {
        return NumberGenerator.CACHE;
    }

    /**
     * Cache for Types.
     */
    private static class NumberGeneratorCache extends Cache<NumberGenerator>
    {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, NumberGenerator> _cache4Id,
                                 final Map<String, NumberGenerator> _cache4Name,
                                 final Map<UUID, NumberGenerator> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {

                    stmt = con.getConnection().createStatement();

                    final ResultSet rs = stmt.executeQuery(NumberGenerator.SQL_SELECT);
                    while (rs.next()) {
                        final long id = rs.getLong(1);
                        final String name = rs.getString(2).trim();
                        final String uuid = rs.getString(3).trim();
                        final String format = rs.getString(4).trim();

                        if (NumberGenerator.LOG.isDebugEnabled()) {
                            NumberGenerator.LOG.debug("read NumberGenerator '" + name + "' (id = " + id
                                            + ") + format = " + format);
                        }
                        final NumberGenerator generator = new NumberGenerator(id, name, uuid, format);
                        _cache4Id.put(generator.getId(), generator);
                        _cache4Name.put(generator.getName(), generator);
                        _cache4UUID.put(generator.getUUID(), generator);
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read NumberGenerator", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read NumberGenerator", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read NumberGenerator", e);
                    }
                }
            }
        }
    }
}

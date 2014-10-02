/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.db;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.infinispan.Cache;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CachedInstanceQuery
    extends InstanceQuery
    implements ICacheDefinition
{

    /**
     * Key used for the QueryKey instance.
     */
    private String key;

    /**
     * lifespan of the entry. Negative values are interpreted as unlimited
     * lifespan. 0 means do not apply
     */
    private long lifespan = 0;

    /**
     * time unit for lifespan.
     */
    private TimeUnit lifespanUnit;

    /**
     * the maximum amount of time this key is allowed to be idle for before it
     * is considered as expired. 0 means do not apply
     */
    private long maxIdleTime = 0;

    /**
     * time unit for max idle time.
     */
    private TimeUnit maxIdleTimeUnit;

    /**
     * Constructor setting the type by his UUID.
     *
     * @param _typeUUI UUID of the Type the query is based on
     * @throws CacheReloadException on error
     */
    public CachedInstanceQuery(final UUID _typeUUI)
        throws CacheReloadException
    {
        super(_typeUUI);
    }

    /**
     * Constructor setting the type.
     *
     * @param _type TYpe the query is based on
     */
    public CachedInstanceQuery(final Type _type)
    {
        super(_type);
    }

    /**
     * @param _key key to the cache
     * @param _typeUUID UUID of the Type the query is based on
     * @throws CacheReloadException on error
     */
    public CachedInstanceQuery(final String _key,
                               final UUID _typeUUID)
        throws CacheReloadException
    {
        this(_typeUUID);
        this.key = _key;
    }

    /**
     * @param _key key to the cache
     * @param _type TYpe the query is based on
     * @throws CacheReloadException on error
     */
    public CachedInstanceQuery(final String _key,
                               final Type _type)
        throws CacheReloadException
    {
        this(_type);
        this.key = _key;
    }

    /**
     * The instance method executes the query without an access check.
     *
     * @return true if the query contains values, else false
     * @throws EFapsException on error
     */
    @Override
    public List<Instance> executeWithoutAccessCheck()
        throws EFapsException
    {
        prepareQuery();
        final String sql = createSQLStatement();
        final QueryKey querykey = QueryKey.get(getKey(), sql);
        final Cache<QueryKey, Object> cache = QueryCache.getSqlCache();
        if (cache.containsKey(querykey)) {
            final Object object = cache.get(querykey);
            if (object instanceof List) {
                final List<?> values = (List<?>) object;
                for (final Object value : values) {
                    if (value instanceof Instance) {
                        getValues().add((Instance) value);
                    }
                }
            }
        } else {
            executeOneCompleteStmt(sql);
            QueryCache.put(this, querykey, getValues());
        }
        return getValues();
    }

    /**
     * Getter method for the instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * Setter method for instance variable {@link #key}.
     *
     * @param _key value for instance variable {@link #key}
     */
    public void setKey(final String _key)
    {
        this.key = _key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLifespan()
    {
        return this.lifespan;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeUnit getLifespanUnit()
    {
        return this.lifespanUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMaxIdleTime()
    {
        return this.maxIdleTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeUnit getMaxIdleTimeUnit()
    {
        return this.maxIdleTimeUnit;
    }

    /**
     * Setter method for instance variable {@link #lifespan}.
     *
     * @param _lifespan value for instance variable {@link #lifespan}
     * @return this instance to allow chaining
     */
    public CachedInstanceQuery setLifespan(final long _lifespan)
    {
        this.lifespan = _lifespan;
        return this;
    }

    /**
     * Setter method for instance variable {@link #lifespanUnit}.
     *
     * @param _lifespanUnit value for instance variable {@link #lifespanUnit}
     * @return this instance to allow chaining
     */
    public CachedInstanceQuery setLifespanUnit(final TimeUnit _lifespanUnit)
    {
        this.lifespanUnit = _lifespanUnit;
        return this;
    }

    /**
     * Setter method for instance variable {@link #maxIdleTime}.
     *
     * @param _maxIdleTime value for instance variable {@link #maxIdleTime}
     * @return this instance to allow chaining
     */
    public CachedInstanceQuery setMaxIdleTime(final long _maxIdleTime)
    {
        this.maxIdleTime = _maxIdleTime;
        return this;
    }

    /**
     * Setter method for instance variable {@link #maxIdleTimeUnit}.
     *
     * @param _maxIdleTimeUnit value for instance variable
     *            {@link #maxIdleTimeUnit}
     * @return this instance to allow chaining
     */
    public CachedInstanceQuery setMaxIdleTimeUnit(final TimeUnit _maxIdleTimeUnit)
    {
        this.maxIdleTimeUnit = _maxIdleTimeUnit;
        return this;
    }

    /**
     * Get a CachedInstanceQuery that will only cache during a request.
     * @param _type Type the query is based on
     * @throws CacheReloadException on error
     */
    public static CachedInstanceQuery get4Request(final Type _type)
        throws EFapsException
    {
        return new CachedInstanceQuery(Context.getThreadContext().getRequestId(), _type).setLifespan(5)
                        .setLifespanUnit(TimeUnit.MINUTES);
    }

    /**
     * Get a CachedInstanceQuery that will only cache during a request.
     * @param _typeUUID uuid of the Type the query is based on
     * @throws CacheReloadException on error
     */
    public static CachedInstanceQuery get4Request(final UUID _typeUUID)
        throws EFapsException
    {
        return new CachedInstanceQuery(Context.getThreadContext().getRequestId(), _typeUUID).setLifespan(5)
                        .setLifespanUnit(TimeUnit.MINUTES);
    }
}

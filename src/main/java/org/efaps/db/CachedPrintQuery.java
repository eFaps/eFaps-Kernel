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

package org.efaps.db;

import java.util.concurrent.TimeUnit;

import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CachedPrintQuery
    extends PrintQuery
    implements ICacheDefinition
{

    /**
     * Key used for the Cache.
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
     * @param _instance instance to be updated.
     * @param _key key used for caching
     * @throws EFapsException on error
     */
    public CachedPrintQuery(final Instance _instance,
                            final String _key)
        throws EFapsException
    {
        super(_instance);
        this.key = _key;
    }

    /**
     * @param _instance instance to be updated.
     * @throws EFapsException on error
     */
    public CachedPrintQuery(final Instance _instance)
        throws EFapsException
    {
        super(_instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return this.key;
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
    public CachedPrintQuery setLifespan(final long _lifespan)
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
    public CachedPrintQuery setLifespanUnit(final TimeUnit _lifespanUnit)
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
    public CachedPrintQuery setMaxIdleTime(final long _maxIdleTime)
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
    public CachedPrintQuery setMaxIdleTimeUnit(final TimeUnit _maxIdleTimeUnit)
    {
        this.maxIdleTimeUnit = _maxIdleTimeUnit;
        return this;
    }
}

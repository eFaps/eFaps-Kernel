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

package org.efaps.db;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CachedMultiPrintQuery
    extends MultiPrintQuery
    implements ICacheDefinition
{

    /**
     * Key used for Caching.
     */
    private final String key;

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
     * @param _instances instance to be updated.
     * @param _key key used for caching
     * @throws EFapsException on error
     */
    public CachedMultiPrintQuery(final List<Instance> _instances,
                                 final String _key)
        throws EFapsException
    {
        super(_instances);
        this.key = _key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled()
    {
        return true;
    }

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
    public CachedMultiPrintQuery setLifespan(final long _lifespan)
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
    public CachedMultiPrintQuery setLifespanUnit(final TimeUnit _lifespanUnit)
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
    public CachedMultiPrintQuery setMaxIdleTime(final long _maxIdleTime)
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
    public CachedMultiPrintQuery setMaxIdleTimeUnit(final TimeUnit _maxIdleTimeUnit)
    {
        this.maxIdleTimeUnit = _maxIdleTimeUnit;
        return this;
    }

    /**
     * Get a CachedMultiPrintQuery that will only cache during a request.
     *
     * @param _instances instance to be updated.
     * @return the 4 request
     * @throws EFapsException on error
     */
    public static CachedMultiPrintQuery get4Request(final List<Instance> _instances)
        throws EFapsException
    {
        return new CachedMultiPrintQuery(_instances, Context.getThreadContext().getRequestId()).setLifespan(5)
                        .setLifespanUnit(TimeUnit.MINUTES);
    }
}

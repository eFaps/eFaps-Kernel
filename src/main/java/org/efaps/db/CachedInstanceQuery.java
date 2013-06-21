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

import java.util.List;
import java.util.UUID;

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
{

    /**
     * Key used for the QueryKey instance.
     */
    private String key;

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
            cache.put(querykey, getValues());
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
}

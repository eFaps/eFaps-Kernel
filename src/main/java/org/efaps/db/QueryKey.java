/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db;

import java.io.Serializable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.infinispan.query.Transformable;

@Transformable(transformer = QueryKeyTransformer.class)
public final class QueryKey
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The sql statement executed.
     */
    private final String sql;

    /**
     * The name of the sql statement.
     */
    private final String key;

    /**
     * @param _key key
     * @param _sql sql statement
     */
    private QueryKey(final String _key,
                     final String _sql)
    {
        key = _key;
        if (_sql.length() > 30000) {
            sql = DigestUtils.md2Hex(_sql);
        } else {
            sql = _sql;
        }
    }

    /**
     * Getter method for the instance variable {@link #sql}.
     *
     * @return value of instance variable {@link #sql}
     */
    public String getSql()
    {
        return sql;
    }

    /**
     * Getter method for the instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return key;
    }
    /**
     * @return id represented by this instance
     */
    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    /**
     * @param _obj Object to compare
     * @return <i>true</i> if the given object in _obj is an instance and holds
     *         the same type and id
     */
    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret = false;
        if (_obj instanceof QueryKey) {
            final QueryKey queryKey = (QueryKey) _obj;
            ret = getKey().equals(queryKey.getKey()) && getSql().equals(queryKey.getSql());
        } else {
            super.equals(_obj);
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * @param _key key
     * @param _sql sql
     * @return new QueryKey
     */
    public static QueryKey get(final String _key,
                               final String _sql)
    {
        return new QueryKey(_key, _sql);
    }
}

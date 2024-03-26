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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

@Indexed
public class QueryValue
    implements Serializable
{

    private static final long serialVersionUID = 1L;
    @KeywordField
    private final String key;
    private final Object content;

    private QueryValue(final String key,
                       final Object content)
    {
        this.key = key;
        this.content = content;
    }

    public String getKey()
    {
        return key;
    }

    public Object getContent()
    {
        return content;
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
    public static QueryValue get(final String _key,
                                 final Object content)
    {
        return new QueryValue(_key, content);
    }
}

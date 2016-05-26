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
package org.efaps.admin.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Sort;
import org.efaps.util.EFapsException;

/**
 * The Interface IResultProvider.
 *
 * @author The eFaps Team
 */
public interface ISearch
    extends Serializable
{

    /**
     * Sets the query.
     *
     * @param _query the new query
     */
    void setQuery(String _query);

    /**
     * Gets the query.
     *
     * @return the query
     */
    String getQuery();

    /**
     * Gets the name.
     *
     * @return the name
     */
    default String getName()
    {
        return this.getClass().getName();
    };

    /**
     * Gets the num hits.
     *
     * @return the num hits
     */
    default int getNumHits()
    {
        return 100;
    }

    /**
     * Gets the result fields. Mapping of field keys
     * and names already translated to requested language.
     *
     * @return the field names
     */
    default Map<String, Collection<String>> getResultFields()
    {
        return Collections.emptyMap();
    }

    /**
     * Gets the result fields. Mapping of field keys
     * and names already translated to requested language.
     *
     * @return the field names
     */
    default Map<String, String> getResultLabel()
    {
        return Collections.emptyMap();
    }

    /**
     * Gets the sort criteria.
     *
     * @return the sort
     */
    default Sort getSort()
    {
        return null;
    };

    /**
     * Gets the config.
     *
     * @return the config
     * @throws EFapsException the e faps exception
     */
    default List<SearchConfig> getConfigs()
        throws EFapsException
    {
        return Collections.emptyList();
    }
}

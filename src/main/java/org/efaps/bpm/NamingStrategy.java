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

package org.efaps.bpm;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class NamingStrategy
    extends ImprovedNamingStrategy
{

    /**
     * Prefix for the tables managed by hibernate.
     */
    public static final String HIBERNATEPREFIX = "ht_";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Convert mixed case to underscores and add prefix.
     *
     * @param _tableName name of the table
     * @return tableName
     */
    @Override
    public String tableName(final String _tableName)
    {
        return NamingStrategy.HIBERNATEPREFIX +  super.tableName(_tableName);
    }

    /**
     * Return the unqualified class name, mixed case converted to underscores
     * and add prefix.
     *
     * @param _className name of the class
     * @return tableName
     */
    @Override
    public String classToTableName(final String _className)
    {
        return NamingStrategy.HIBERNATEPREFIX
                        + super.classToTableName(_className);
    }
}

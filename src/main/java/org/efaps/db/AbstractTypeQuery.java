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

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;

/**
 * The Class AbtractTypeQuery.
 *
 * @author The eFaps Team
 */
public abstract class AbstractTypeQuery
{

    /**
     * Base type this query is searching on.
     */
    private Type baseType;

    /**
     * Getter method for the instance variable {@link #baseType}.
     *
     * @return value of instance variable {@link #baseType}
     */
    public Type getBaseType()
    {
        return this.baseType;
    }

    /**
     * Sets the base type this query is searching on.
     *
     * @param _baseType the new base type this query is searching on
     */
    protected void setBaseType(final Type _baseType)
    {
        this.baseType = _baseType;
    }

    /**
     * Gets the index for sql table.
     *
     * @param _sqlTable the sql table
     * @return the index for sql table
     */
    public abstract Integer getIndex4SqlTable(final SQLTable _sqlTable);
}

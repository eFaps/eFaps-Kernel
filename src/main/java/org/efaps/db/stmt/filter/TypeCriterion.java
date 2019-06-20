/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.db.stmt.filter;

import org.efaps.db.wrapper.TableIndexer.TableIdx;

public class TypeCriterion
{

    private final TableIdx tableIdx;

    /** The sql col type. */
    private final String sqlColType;

    /** The id. */
    private final long typeId;

    private final boolean nullable;

    /**
     * Instantiates a new type criteria.
     *
     * @param _sqlColType the sql col type
     * @param _id the id
     */
    public TypeCriterion(final TableIdx _tableIdx,
                         final String _sqlColType,
                         final long _typeId,
                         final boolean _nullable)
    {
        tableIdx = _tableIdx;
        sqlColType = _sqlColType;
        typeId = _typeId;
        nullable = _nullable;
    }

    public String getSqlColType()
    {
        return sqlColType;
    }

    public long getTypeId()
    {
        return typeId;
    }

    public TableIdx getTableIdx()
    {
        return tableIdx;
    }

    public Integer getTableIndex()
    {
        return tableIdx.getIdx();
    }

    public boolean isNullable()
    {
        return nullable;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof TypeCriterion) {
            final TypeCriterion obj = (TypeCriterion) _obj;
            ret = sqlColType.equals(obj.sqlColType) && typeId == obj.typeId;
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return sqlColType.hashCode() + Long.valueOf(typeId).hashCode();
    }

    public static TypeCriterion of(final TableIdx _tableIdx,
                                   final String _sqlColType,
                                   final long _typeId,
                                   final boolean _nullable)
    {
        return new TypeCriterion(_tableIdx, _sqlColType, _typeId, _nullable);
    }

    public static TypeCriterion of(final TableIdx _tableIdx,
                                   final String _sqlColType,
                                   final long _typeId)
    {
        return new TypeCriterion(_tableIdx, _sqlColType, _typeId, false);
    }
}

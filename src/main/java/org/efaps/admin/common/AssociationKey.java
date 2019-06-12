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

package org.efaps.admin.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AssociationKey
    implements Serializable
{

    /** */
    private static final long serialVersionUID = 1L;

    private long companyId;

    private long typeId;

    public long getCompanyId()
    {
        return companyId;
    }

    public AssociationKey setCompanyId(final long companyId)
    {
        this.companyId = companyId;
        return this;
    }

    public long getTypeId()
    {
        return typeId;
    }

    public AssociationKey setTypeId(final long typeId)
    {
        this.typeId = typeId;
        return this;
    }

    /**
     * @return id represented by this instance
     */
    @Override
    public int hashCode()
    {
        return (int) (companyId + typeId);
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
        if (_obj instanceof AssociationKey) {
            final AssociationKey key = (AssociationKey) _obj;
            ret = key.getCompanyId() == companyId && key.getTypeId() == typeId;
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

    public static AssociationKey get(final long _companyId,
                                     final long _typeId)
    {
        return new AssociationKey()
                        .setCompanyId(_companyId)
                        .setTypeId(_typeId);
    }
}

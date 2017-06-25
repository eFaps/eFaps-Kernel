/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.admin.access.user;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.search.annotations.Indexed;

/**
 * The Class UserPermissions.
 *
 * @author The eFaps Team
 */
@Indexed
public class PermissionSet
{

    /** The person id. */
    private long personId;

    /** The instance type id. */
    private long typeId;

    /** The company id. */
    private long companyId;

    /** The access types. */
    private final Set<Long> accessTypeIds = new HashSet<>();

    /** The access types. */
    private final Set<Long> statusIds = new HashSet<>();

    /**
     * Getter method for the instance variable {@link #personId}.
     *
     * @return value of instance variable {@link #personId}
     */
    public long getPersonId()
    {
        return this.personId;
    }

    /**
     * Setter method for instance variable {@link #personId}.
     *
     * @param _personId value for instance variable {@link #personId}
     * @return the permissions
     */
    public PermissionSet setPersonId(final long _personId)
    {
        this.personId = _personId;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #companyId}.
     *
     * @return value of instance variable {@link #companyId}
     */
    public long getCompanyId()
    {
        return this.companyId;
    }

    /**
     * Setter method for instance variable {@link #companyId}.
     *
     * @param _companyId value for instance variable {@link #companyId}
     * @return the permissions
     */
    public PermissionSet setCompanyId(final long _companyId)
    {
        this.companyId = _companyId;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #typeId}.
     *
     * @return value of instance variable {@link #typeId}
     */
    public long getTypeId()
    {
        return this.typeId;
    }

    /**
     * Getter method for the instance variable {@link #accessTypeIds}.
     *
     * @return value of instance variable {@link #accessTypeIds}
     */
    public Set<Long> getAccessTypeIds()
    {
        return this.accessTypeIds;
    }

    /**
     * Adds the access type id.
     *
     * @param _ids the ids
     * @return the permissions
     */
    public PermissionSet addAccessTypeId(final long... _ids)
    {
        for (final long id : _ids) {
            this.accessTypeIds.add(id);
        }
        return this;
    }

    /**
     * Getter method for the instance variable {@link #statusIds}.
     *
     * @return value of instance variable {@link #statusIds}
     */
    public Set<Long> getStatusIds()
    {
        return this.statusIds;
    }

    /**
     * Adds the status id.
     *
     * @param _ids the ids
     * @return the permissions
     */
    public PermissionSet addStatusId(final long... _ids)
    {
        for (final long id : _ids) {
            this.statusIds.add(id);
        }
        return this;
    }

    /**
     * Setter method for instance variable {@link #typeId}.
     *
     * @param _typeId value for instance variable {@link #typeId}
     * @return the permissions
     */
    public PermissionSet setTypeId(final long _typeId)
    {
        this.typeId = _typeId;
        return this;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public Key getKey()
    {
        final Key ret = new Key().setPersonId(getPersonId())
                        .setCompanyId(getCompanyId())
                        .setTypeId(getTypeId());
        return ret;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

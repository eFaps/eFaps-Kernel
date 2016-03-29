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

package org.efaps.admin.access;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.admin.user.Company;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

/**
 * Used as a key to store the results of a Query related to access in a map.<br/>
 * Uses:<br/>
 * <ul>
 * <li>
 * Id of the type from the related Instance</li>
 * <li>
 * Id of object from the related Instance</li>
 * <li>
 * Id of the person the result belongs to</li>
 * <li>
 * Id of the company the result belongs to</li>
 * <li>
 * AccessType (e.g. CREATE, CHECKIN) the result belongs to</li>
 * </ul>
 *
 * @author The eFaps Team
 */
@Indexed
public final class AccessKey
    implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Id of a instance type.
     */
    @Field(analyze = Analyze.NO)
    private final UUID instanceTypeUUID;

    /**
     * Id of the Instance.
     */
    @Field(analyze = Analyze.NO)
    private final long instanceId;

    /**
     * Id of the Person.
     */
    @Field(analyze = Analyze.NO)
    private final long personId;

    /**
     * Id of the Company.
     */
    private final long companyId;

    /**
     * Type of the Access.
     */
    private final AccessTypeEnums accessType;

    /**
     * Is this key only valid for one Time access.
     */
    private final boolean oneTime;

    /**
     * @param _instanceTypeUUID UUID of a instance type
     * @param _instanceId Id of the Instance
     * @param _personId Id of the Person
     * @param _companyId Id of the Company
     * @param _accessType Type of the Access
     * @param _oneTime  ontime access definition
     */
    private AccessKey(final UUID _instanceTypeUUID,
                      final long _instanceId,
                      final long _personId,
                      final long _companyId,
                      final AccessTypeEnums _accessType,
                      final boolean _oneTime)
    {
        this.instanceTypeUUID = _instanceTypeUUID;
        this.instanceId = _instanceId;
        this.personId = _personId;
        this.companyId = _companyId;
        this.accessType = _accessType;
        this.oneTime = _oneTime;
    }

    /**
     * Getter method for the instance variable {@link #instanceTypeUUID}.
     *
     * @return value of instance variable {@link #instanceTypeUUID}
     */
    public UUID getInstanceTypeUUID()
    {
        return this.instanceTypeUUID;
    }

    /**
     * Getter method for the instance variable {@link #instanceId}.
     *
     * @return value of instance variable {@link #instanceId}
     */
    public long getInstanceId()
    {
        return this.instanceId;
    }

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
     * Getter method for the instance variable {@link #companyId}.
     *
     * @return value of instance variable {@link #companyId}
     */
    public long getCompanyId()
    {
        return this.companyId;
    }

    /**
     * Getter method for the instance variable {@link #accessType}.
     *
     * @return value of instance variable {@link #accessType}
     */
    public AccessTypeEnums getAccessType()
    {
        return this.accessType;
    }

    /**
     * Getter method for the instance variable {@link #oneTime}.
     *
     * @return value of instance variable {@link #oneTime}
     */
    public boolean isOneTime()
    {
        return this.oneTime;
    }

    /**
     * @return the Instance this AccessKet belongs to
     * @throws CacheReloadException on error
     */
    public Instance getInstance()
        throws CacheReloadException
    {
        return Instance.get(getInstanceTypeUUID(), getInstanceId());
    }

    /**
     * @return indexkey for mapping
     */
    protected String getIndexKey()
    {
        return new StringBuilder().append(this.instanceTypeUUID)
                        .append("-").append(this.instanceId)
                        .append("-").append(this.personId)
                        .append("-").append(this.companyId)
                        .append("-").append(this.accessType).toString();
    }

    /**
     * @return id represented by this instance
     */
    @Override
    public int hashCode()
    {
        return (int) (this.instanceTypeUUID.hashCode() + this.instanceId);
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
        if (_obj instanceof AccessKey) {
            final AccessKey accessKey = (AccessKey) _obj;
            ret = getInstanceTypeUUID().equals(accessKey.getInstanceTypeUUID())
                            && getInstanceId() == accessKey.getInstanceId()
                            && getPersonId() == accessKey.getPersonId()
                            && getCompanyId() == accessKey.getCompanyId()
                            && getAccessType().equals(accessKey.getAccessType());
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
     * @param _instance Instance the AccessKey is wanted for
     * @param _accessType AccessTyep the AccessKey is wanted for
     * @throws EFapsException on error
     * @return new AccessKey instance
     */
    public static AccessKey get(final Instance _instance,
                                final AccessType _accessType)
        throws EFapsException
    {
        return AccessKey.get(_instance, _accessType, false);
    }

    /**
     * @param _instance Instance the AccessKey is wanted for
     * @param _accessType AccessTyep the AccessKey is wanted for
     * @throws EFapsException on error
     * @return new AccessKey instance
     */
    public static AccessKey getOneTime(final Instance _instance,
                                       final AccessType _accessType)
        throws EFapsException
    {
        return AccessKey.get(_instance, _accessType, true);
    }

    /**
     * @param _instance Instance the AccessKey is wanted for
     * @param _accessType AccessTyep the AccessKey is wanted for
     * @param _oneTime oneTime
     * @throws EFapsException on error
     * @return new AccessKey instance
     */
    private  static AccessKey get(final Instance _instance,
                                  final AccessType _accessType,
                                  final boolean _oneTime)
        throws EFapsException
    {
        final Person person = Context.getThreadContext().getPerson();
        long personIdTmp;
        if (person == null) {
            personIdTmp = 0;
        } else {
            personIdTmp = person.getId();
        }
        final Company company = Context.getThreadContext().getCompany();
        long companyIdTmp;
        if (company == null) {
            companyIdTmp = 0;
        } else {
            companyIdTmp = company.getId();
        }
        return new AccessKey(_instance.getTypeUUID(), _instance.getId(), personIdTmp, companyIdTmp,
                        AccessTypeEnums.get(_accessType.getUUID()), _oneTime);
    }
}

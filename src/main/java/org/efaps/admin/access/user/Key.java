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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.infinispan.query.Transformable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PersonAccess.
 *
 * @author The eFaps Team
 */
@Transformable(transformer = KeyTransformer.class)
public class Key
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Key.class);

    /** The person id. */
    @Field(analyze = Analyze.NO)
    private long personId;

    /** The instance type id. */
    private long typeId;

    /** The company id. */
    private long companyId;

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
     * @return the key
     */
    public Key setPersonId(final long _personId)
    {
        this.personId = _personId;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #typeUUID}.
     *
     * @return value of instance variable {@link #typeUUID}
     */
    public long getTypeId()
    {
        return this.typeId;
    }

    /**
     * Setter method for instance variable {@link #typeUUID}.
     *
     * @param _typeId the type id
     * @return the key
     */
    public Key setTypeId(final long _typeId)
    {
        this.typeId = _typeId;
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
     * @return the key
     */
    public Key setCompanyId(final long _companyId)
    {
        this.companyId = _companyId;
        return this;
    }

    /**
     * @return id represented by this instance
     */
    @Override
    public int hashCode()
    {
        return (int) (this.companyId + this.companyId + this.typeId);
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
        if (_obj instanceof Key) {
            final Key accessKey = (Key) _obj;
            ret = getTypeId() == accessKey.getTypeId() && getPersonId() == accessKey.getPersonId()
                            && getCompanyId() == accessKey.getCompanyId();
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
     * Gets the for instance.
     *
     * @param _instance the instance
     * @return the for instance
     * @throws EFapsException on error
     */
    public static Key get4Instance(final Instance _instance)
        throws EFapsException
    {
        Key.LOG.debug("Retrieving Key for {}", _instance);
        final Key ret = new Key();
        ret.setPersonId(Context.getThreadContext().getPersonId());

        final Type type = _instance.getType();
        ret.setTypeId(type.getId());
        if (type.isCompanyDependent()) {
            ret.setCompanyId(Context.getThreadContext().getCompany().getId());
        }
        Key.LOG.debug("Retrieved Key {}", ret);
        return ret;
    }
}

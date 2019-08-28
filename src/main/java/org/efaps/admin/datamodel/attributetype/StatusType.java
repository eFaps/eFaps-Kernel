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

package org.efaps.admin.datamodel.attributetype;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 *
 * @author The eFaps Team
 *
 */
public class StatusType
    extends AbstractLinkType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate4Update(final Attribute _attribute,
                               final Instance _instance,
                               final Object[] _value)
        throws EFapsException
    {
        super.validate4Update(_attribute, _instance, _value);
        validate(_attribute, _instance, _value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate4Insert(final Attribute _attribute,
                               final Instance _instance,
                               final Object[] _value)
        throws EFapsException
    {
        super.validate4Update(_attribute, _instance, _value);
        validate(_attribute, _instance, _value);
    }

    /**
     * Method is executed on addition of an Attribute/Value pair for an Insert
     * and Update to validate if the value is permitted. Will throw the
     * exception if not valid.<br/>
     *
     * @param _attribute    the Attribute that will be updated with the _value
     * @param _instance     Instance that will be updated
     * @param _value        value that will be used for the update
     * @throws EFapsException if not valid
     */
    private void validate(final Attribute _attribute,
                          final Instance _instance,
                          final Object[] _value)
        throws EFapsException
    {
        final Long value = eval(_value);
        // check the basic values
        if (value == null) {
            throw new EFapsException(StatusType.class, "ValueIsNull", _attribute, _instance, _value);
        } else {
            final Status status = Status.get(value);
            // the statusType must be evaluated by the instance type due to the reason that the attribute
            // belongs to an abstract type
            final Type statusType = _instance.getType().getStatusAttribute().getLink();
            if (statusType == null) {
                throw new EFapsException(StatusType.class, "AttributeNoStatus", _attribute, _instance, _value);
            } else {
                if (!status.getStatusGroup().equals(Status.get(statusType.getUUID()))) {
                    throw new EFapsException(StatusType.class, "ValueIsNotValid", _attribute, _instance, _value);
                }
            }
        }
    }
}

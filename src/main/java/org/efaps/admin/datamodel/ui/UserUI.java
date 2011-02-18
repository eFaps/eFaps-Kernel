/*
 * Copyright 2003 - 2011 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.util.EFapsException;

/**
 * Class to represent a User for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UserUI
    extends AbstractUI
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        String ret = null;
        final Object value = _fieldValue.getValue();
        if (value instanceof Person) {
            final Person person = (Person) value;
            ret = person.getName();
        } else if (value instanceof Role) {
            final Role role = (Role) value;
            ret = role.getName();
        } else {
            throw new EFapsException(this.getClass(), "getViewHtml.noPersonOrRole", (Object[]) null);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
    {
        String value = null;
        if (_fieldValue.getValue() instanceof Person) {
            final Person person = (Person) _fieldValue.getValue();
            value = person.getName();
        } else if (_fieldValue.getValue() instanceof Role) {
            final Role role = (Role) _fieldValue.getValue();
            value = role.getName();
        }
        final String value2 = null;
        if (_fieldValue2.getValue() instanceof Person) {
            final Person person = (Person) _fieldValue2.getValue();
            value = person.getName();
        } else if (_fieldValue2.getValue() instanceof Role) {
            final Role role = (Role) _fieldValue2.getValue();
            value = role.getName();
        }
        return value.compareTo(value2);
    }
}

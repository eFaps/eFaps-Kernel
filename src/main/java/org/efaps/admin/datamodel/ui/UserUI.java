/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Company;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Person.AttrName;
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
            String display = EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.USERUI_DISPLAYPERSON);
            if (display == null) {
                display = "${LASTNAME}, ${FIRSTNAME}";
            }
            final Map<String, String> values = new HashMap<String, String>();
            for (final AttrName attr : AttrName.values()) {
                values.put(attr.name(), person.getAttrValue(attr));
            }
            final StrSubstitutor sub = new StrSubstitutor(values);
            ret =  sub.replace(display);
        } else if (value instanceof Role) {
            final Role role = (Role) value;
            ret = role.getName();
        } else if (value instanceof Group) {
            final Group group = (Group) value;
            ret = group.getName();
        } else if (value instanceof Company) {
            final Company company = (Company) value;
            ret = company.getName();
        } else {
            ret = DBProperties.getProperty("org.efaps.admin.datamodel.ui.UserUI.None");
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
        throws EFapsException
    {
        final String value;
        if (_fieldValue.getValue() instanceof Person) {
            final Person person = (Person) _fieldValue.getValue();
            value = person.getName();
        } else if (_fieldValue.getValue() instanceof Role) {
            final Role role = (Role) _fieldValue.getValue();
            value = role.getName();
        } else if (_fieldValue.getValue() instanceof Group) {
            final Group group = (Group) _fieldValue.getValue();
            value = group.getName();
        } else if (_fieldValue.getValue() instanceof Company) {
            final Company company = (Company) _fieldValue.getValue();
            value = company.getName();
        } else {
            value = _fieldValue.getValue() == null ? "" : _fieldValue.getValue().toString();
        }
        final String value2;
        if (_fieldValue2.getValue() instanceof Person) {
            final Person person = (Person) _fieldValue2.getValue();
            value2 = person.getName();
        } else if (_fieldValue2.getValue() instanceof Role) {
            final Role role = (Role) _fieldValue2.getValue();
            value2 = role.getName();
        } else if (_fieldValue2.getValue() instanceof Group) {
            final Group group = (Group) _fieldValue2.getValue();
            value2 = group.getName();
        } else if (_fieldValue2.getValue() instanceof Company) {
            final Company company = (Company) _fieldValue2.getValue();
            value2 = company.getName();
        } else {
            value2 = _fieldValue2.getValue() == null ? "" : _fieldValue2.getValue().toString();
        }
        return value.compareTo(value2);
    }
}

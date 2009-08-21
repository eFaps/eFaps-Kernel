/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.esjp.admin.user;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for Locale.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("5eb52b3e-3384-4675-9a55-396cdb5228be")
@EFapsRevision("$Rev$")
public class LocaleUI
{

    /**
     * Method is called from within the form Admin_User_Person to render a drop
     * down field with all Chronologies.
     *
     * @param _parameter Parameters as passed from eFaps
     * @return Return containing a drop down
     * @throws EFapsException on error
     */
    public Return get4Edit(final Parameter _parameter) throws EFapsException
    {
        final Return retVal = new Return();
        final FieldValue fieldValue = (FieldValue) _parameter.get(ParameterValues.UIOBJECT);

        final TargetMode mode = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);
        Locale locale;
        if (mode.equals(TargetMode.CREATE)) {
            locale = Locale.getDefault();
        } else {
            final String localeStr = (String) fieldValue.getValue();
            final String[] countries = localeStr.split("_");
            if (countries.length == 2) {
                locale = new Locale(countries[0], countries[1]);
            } else  if (countries.length == 3) {
                locale = new Locale(countries[0], countries[1], countries[2]);
            } else {
                locale = new Locale(localeStr);
            }
        }
        retVal.put(ReturnValues.SNIPLETT, getField(locale, fieldValue.getField().getName()));
        return retVal;
    }

    /**
     * Method to build a drop down field for html containing all locales.
     *
     * @param _locale       actual selected Locale
     * @param _fieldName    Name of the field
     * @return Html with drop down
     */
    private String getField(final Locale _locale, final String _fieldName)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("<select size=\"1\" name=\"").append(_fieldName).append("\">");
        final Map<String, Locale> values = new TreeMap<String, Locale>();
        for (final Locale locale : Locale.getAvailableLocales()) {
            values.put(locale.getDisplayName(), locale);
        }
        for (final Entry<String, Locale> entry : values.entrySet()) {
            ret.append("<option");
            if (_locale.equals(entry.getValue())) {
                ret.append(" selected=\"selected\" ");
            }
            ret.append(" value=\"").append(entry.getValue()).append("\">").append(entry.getKey())
                .append("</option>");
        }
        ret.append("</select>");
        return ret.toString();
    }
}

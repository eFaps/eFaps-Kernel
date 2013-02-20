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

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class StringWithUoMUI
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
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();

        if (value instanceof Object[]) {
            final Object[] values =  (Object[]) value;
            final String tmp = values[0] != null ?  values[0].toString() : "";
            final UoM uom = (UoM) values[1];
            ret.append("<span><span name=\"").append(field.getName()).append("\" ")
                .append(UIInterface.EFAPSTMPTAG).append(">")
                .append(StringEscapeUtils.escapeHtml4(tmp).replaceAll("\\n", "<br/>"))
                .append("</span>&nbsp;")
                .append("<span name=\"").append(field.getName()).append("UoM\" ").append(">")
                .append(uom.getName()).append("</span></span>");
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        String strValue = null;
        UoM uomValue = null;
        if (value instanceof Object[]) {
            final Object[] values =  (Object[]) value;
            strValue = values[0] != null ?  values[0].toString() : "";
            uomValue = (UoM) values[1];
        }

        if (_fieldValue.getTargetMode().equals(TargetMode.SEARCH)) {
            ret.append("<input type=\"text\"").append(" size=\"").append(field.getCols()).append("\" name=\"").append(
                           field.getName()).append("\" value=\"").append(value != null ? value : "*").append("\" />");
        } else {
            ret.append("<span><input type=\"text\" size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"").append(strValue != null ? strValue : "").append("\"")
                .append(UIInterface.EFAPSTMPTAG).append("/>")
                .append("<select name=\"").append(_fieldValue.getField().getName()).append("UoM\" size=\"1\">");

            final Dimension dim = _fieldValue.getAttribute().getDimension();
            for (final UoM uom : dim.getUoMs()) {
                ret.append("<option value=\"").append(uom.getId());
                if ((uomValue == null && uom.equals(dim.getBaseUoM())) || (uomValue != null && uomValue.equals(uom))) {
                    ret.append("\" selected=\"selected");
                }
                ret.append("\">").append(uom.getName()).append("</option>");
            }
            ret.append("</select></span>");

        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final FieldValue _fieldValue)
        throws EFapsException
    {
        return _fieldValue.getValue() == null ? "" : _fieldValue.getValue().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
    {
        final String value = _fieldValue.getValue().toString();
        final String value2 = _fieldValue2.getValue().toString();
        return value.compareTo(value2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final String _value,
                                final Attribute _attribute)
    {
        String ret = null;
        if (_attribute != null) {
            if (_value.length() > _attribute.getSize()) {
                ret = DBProperties.getProperty(StringWithUoMUI.class.getName() + ".InvalidValue")
                    + " " + _attribute.getSize();
            }
        }
        return ret;
    }
}

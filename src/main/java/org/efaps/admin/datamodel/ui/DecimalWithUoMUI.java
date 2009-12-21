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

package org.efaps.admin.datamodel.ui;

import java.text.DecimalFormat;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;


/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class DecimalWithUoMUI extends AbstractUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     * @throws EFapsException
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();

        if (value instanceof Object[]) {
            final Object[] values =  (Object[]) value;
            final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            final String strValue = values[0] instanceof Number ? formatter.format(values[0]) : values[0].toString();
            final UoM uom = (UoM) values[1];
            ret.append("<span name=\"").append(field.getName()).append("\" ").append(EFAPSTMPTAG).append(">")
                   .append(strValue).append("</span>&nbsp;")
                   .append("<span name=\"").append(field.getName()).append("UoM\" ").append(">")
                   .append(uom.getName()).append("</span>");
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public String getEditHtml(final FieldValue _fieldValue,
                              final TargetMode _mode)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        String strValue = null;
        UoM uomValue = null;
        if (value instanceof Object[]) {
            final Object[] values =  (Object[]) value;
            final DecimalFormat formatter
                = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext().getLocale());
            strValue = values[0] instanceof Number ? formatter.format(values[0]) : values[0].toString();
            uomValue = (UoM) values[1];
        }

        if (_mode.equals(TargetMode.SEARCH)) {
            ret.append("<input type=\"text\" size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"").append((value != null ? value : "*")).append("\" />");
        } else {
            ret.append("<input type=\"text\" size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"").append(strValue != null ? strValue : "").append("\"")
                .append(EFAPSTMPTAG).append("/>")
                .append("<select name=\"").append(_fieldValue.getField().getName()).append("UoM\" size=\"1\">");

            final Dimension dim = _fieldValue.getAttribute().getDimension();
            for (final UoM uom : dim.getUoMs()) {
                ret.append("<option value=\"").append(uom.getId());
                if ((uomValue == null && uom.equals(dim.getBaseUoM())) || (uomValue != null && uomValue.equals(uom))) {
                    ret.append("\" selected=\"selected");
                }
                ret.append("\">").append(uom.getName()).append("</option>");
            }
            ret.append("</select>");

        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public String validateValue(final String _value,
                                final Attribute _attribute)
    {
        String ret = null;
        try {
            DecimalType.parseLocalized(_value);
        } catch (final EFapsException e) {
            ret = DBProperties.getProperty(DecimalUI.class.getName() + ".InvalidValue");
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public Object format(final Object _object,
                         final String _pattern)
        throws EFapsException
    {
        final Object ret;
        final DecimalFormat formatter
            = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext().getLocale());
        formatter.applyPattern(_pattern);
        if (_object instanceof Object[]) {
            final String tmp = formatter.format(((Object[]) _object)[0]);
            ((Object[]) _object)[0] = tmp;
            ret = _object;
        } else {
            ret = formatter.format(_object);
        }
        return ret;
    }
}

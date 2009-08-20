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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class used to represent any type of decimal for the UI.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DecimalUI extends AbstractUI
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
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (value instanceof List<?>) {
            final List<?> values = (List<?>) value;
            boolean first = true;
            for (final Object obj : values) {
                final String tmp = obj != null ? obj instanceof Number ? formatter.format(obj) : obj.toString() : "";
                if (tmp != null) {
                    if (first) {
                        first = false;
                    } else {
                        ret.append("<br/>");
                    }
                    ret.append(tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\n", "<br/>"));
                }
            }
        } else {
            final String tmp = value != null
                                ? (value instanceof Number ? formatter.format(value) : value.toString())
                                : "";
            if (tmp != null) {
                ret.append("<span name=\"").append(field.getName()).append("\" ").append(EFAPSTMPTAG).append(">")
                    .append(tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\n", "<br/>"))
                    .append("</span>");
            }
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHiddenHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        final String tmp = value != null
                            ? (value instanceof Number ? formatter.format(value) : value.toString())
                            : "";
        ret.append("<input type=\"hidden\" ").append(" name=\"").append(field.getName())
            .append("\" value=\"").append(tmp).append("\"")
            .append(EFAPSTMPTAG).append("/>");

        return ret.toString();
    }

    /**
     * {@inheritDoc}
     * @throws EFapsException
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        final String tmp = value != null
                            ? (value instanceof Number ? formatter.format(value) : value.toString())
                            : "";
        if (_mode.equals(TargetMode.SEARCH)) {
            ret.append("<input type=\"text\"").append(" size=\"").append(field.getCols()).append("\" name=\"")
                .append(field.getName()).append("\" value=\"")
                 .append((tmp != null ? tmp : "*")).append("\" />");
        } else {
            if (field.getRows() > 1) {
                ret.append("<textarea type=\"text\"")
                    .append(" cols=\"").append(field.getCols())
                    .append("\" rows=\"").append(field.getRows())
                    .append("\" name=\"").append(field.getName()).append("\"")
                    .append(EFAPSTMPTAG).append("/>");
                if (value != null) {
                    ret.append(formatter.format(value));
                }
                ret.append("</textarea>");
            } else {
                ret.append("<input type=\"text\" size=\"").append(field.getCols())
                    .append("\" name=\"").append(field.getName())
                    .append("\" value=\"").append(tmp).append("\"")
                    .append(EFAPSTMPTAG).append("/>");
            }
        }
        return ret.toString();
    }

    @Override
    public String getStringValue(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        return _fieldValue.getValue() != null
                        ? (_fieldValue.getValue() instanceof Number
                                        ? formatter.format(_fieldValue.getValue())
                                        : _fieldValue.getValue().toString())
                        : "";
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue) throws EFapsException
    {
        return _fieldValue.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2)
    {
        int ret = 0;
        if (_fieldValue.getValue() instanceof BigDecimal && _fieldValue2.getValue() instanceof BigDecimal) {
            final BigDecimal num = (BigDecimal) _fieldValue.getValue();
            final BigDecimal num2 = (BigDecimal) _fieldValue2.getValue();
            ret = num.compareTo(num2);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final String _value, final Attribute _attribute)
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
    @Override
    public Object format(final Object _object, final String _pattern) throws EFapsException
    {
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        formatter.applyPattern(_pattern);
        return formatter.format(_object);
    }

}

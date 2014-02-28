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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
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
public class DecimalUI
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
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (_fieldValue.getAttribute() != null) {
            formatter.setMaximumFractionDigits(_fieldValue.getAttribute().getScale());
        }
        if (value instanceof List<?>) {
            final List<?> values = (List<?>) value;
            boolean first = true;
            for (final Object obj : values) {
                final int scale = ((BigDecimal) value).scale();
                if (formatter.getMinimumFractionDigits() < scale) {
                    formatter.setMinimumFractionDigits(scale);
                }
                final String tmp = evalString4Object(formatter, obj);
                if (tmp != null) {
                    if (first) {
                        first = false;
                    } else {
                        ret.append("<br/>");
                    }
                    ret.append(StringEscapeUtils.escapeHtml4(tmp));
                }
            }
        } else {
            final String tmp = evalString4Object(formatter, value);
            if (tmp != null) {
                ret.append("<span name=\"").append(field.getName()).append("\" ")
                    .append(UIInterface.EFAPSTMPTAG).append(">")
                    .append(StringEscapeUtils.escapeHtml4(tmp))
                    .append("</span>");
            }
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHiddenHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (_fieldValue.getAttribute() != null) {
            formatter.setMaximumFractionDigits(_fieldValue.getAttribute().getScale());
        }
        final String tmp = evalString4Object(formatter, value);
        ret.append("<input type=\"hidden\" ").append(" name=\"").append(field.getName())
                        .append("\" value=\"").append(StringEscapeUtils.escapeHtml4(tmp)).append("\"")
                        .append(UIInterface.EFAPSTMPTAG).append("/>");

        return ret.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @throws EFapsException
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (_fieldValue.getAttribute() != null) {
            formatter.setMaximumFractionDigits(_fieldValue.getAttribute().getScale());
        }
        final String tmp = evalString4Object(formatter, value);
        if (_fieldValue.getTargetMode().equals(TargetMode.SEARCH)) {
            ret.append("<input type=\"text\"").append(" size=\"").append(field.getCols()).append("\" name=\"")
                            .append(field.getName()).append("\" value=\"")
                            .append(value != null ? StringEscapeUtils.escapeHtml4(tmp) : "*").append("\" />");
        } else {
            if (field.getRows() > 1) {
                ret.append("<textarea type=\"text\"")
                                .append(" cols=\"").append(field.getCols())
                                .append("\" rows=\"").append(field.getRows())
                                .append("\" name=\"").append(field.getName()).append("\"")
                                .append(UIInterface.EFAPSTMPTAG).append("/>");
                if (value != null) {
                    ret.append(StringEscapeUtils.escapeHtml4(tmp));
                }
                ret.append("</textarea>");
            } else {
                ret.append("<input type=\"text\" size=\"").append(field.getCols())
                    .append("\" name=\"").append(field.getName())
                    .append("\" value=\"").append(StringEscapeUtils.escapeHtml4(tmp)).append("\"")
                    .append(UIInterface.EFAPSTMPTAG).append("/>");
            }
        }
        return ret.toString();
    }

    /**
     * @param _formatter Formatter to be used
     * @param _object object to be formatted
     * @return String vor the object
     */
    private String evalString4Object(final DecimalFormat _formatter,
                                     final Object _object)
    {
        final String ret;
        if (_object == null) {
            ret = "";
        } else {
            if (_object instanceof Number) {
                if (_object instanceof BigDecimal) {
                    final int scale = ((BigDecimal) _object).scale();
                    if (_formatter.getMinimumFractionDigits() < scale) {
                        _formatter.setMinimumFractionDigits(scale);
                    }
                }
                ret = _formatter.format(_object);
            } else {
                ret = _object.toString();
            }
        }
        return ret;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final FieldValue _fieldValue)
        throws EFapsException
    {
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (_fieldValue.getAttribute() != null) {
            formatter.setMaximumFractionDigits(_fieldValue.getAttribute().getScale());
        }
        return evalString4Object(formatter, _fieldValue.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue)
        throws EFapsException
    {
        return _fieldValue.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
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
    public String validateValue(final UIValue _value)
    {
        String ret = null;
        try {
            if (_value.getDbValue() != null) {
                DecimalType.parseLocalized(String.valueOf(_value.getDbValue()));
            }
        } catch (final EFapsException e) {
            ret = DBProperties.getProperty(DecimalUI.class.getName() + ".InvalidValue");
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object format(final Object _object,
                         final String _pattern)
        throws EFapsException
    {
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        formatter.applyPattern(_pattern);
        Object ret;
        if (_object instanceof BigDecimal
                        && formatter.getNegativeSuffix().isEmpty() && "-".equals(formatter.getNegativePrefix())
                        && formatter.getPositiveSuffix().isEmpty() && formatter.getPositivePrefix().isEmpty()) {
            int scale = formatter.getMinimumFractionDigits();
            if (scale < formatter.getMaximumFractionDigits()) {
                scale = formatter.getMaximumFractionDigits();
            }
            ret = ((BigDecimal) _object).setScale(scale, BigDecimal.ROUND_HALF_UP);
        } else {
            ret = formatter.format(_object);
        }
        return ret;
    }
}

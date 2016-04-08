/*
0 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.admin.datamodel.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

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
 *
 */
public class DecimalWithUoMUI
    extends AbstractUI
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final Field field = _fieldValue.getField();
        final Object value = _fieldValue.getValue();

        if (value instanceof Object[]) {
            final Object[] values =  (Object[]) value;
            final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            final String strValue = values[0] != null ? values[0] instanceof Number
                                                ? formatter.format(values[0]) : values[0].toString() : "";
            final UoM uom = (UoM) values[1];
            ret.append("<span><span name=\"").append(field.getName()).append("\" ")
                .append(UIInterface.EFAPSTMPTAG).append(">")
                .append(strValue).append("</span>&nbsp;");
            if (strValue.length() > 0 && uom != null) {
                ret.append("<span name=\"").append(field.getName()).append("UoM\" ").append(">")
                .append(uom.getName()).append("</span>");
            }
            ret.append("</span>");
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
        final StringBuilder ret = new StringBuilder();
        final Object value = _fieldValue.getValue();
        if (value instanceof Object[]) {
            final Object[] values =  (Object[]) value;
            final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            final String strValue = values[0] != null ? values[0] instanceof Number
                                                ? formatter.format(values[0]) : values[0].toString() : "";
            final UoM uom = (UoM) values[1];
            ret.append(strValue).append(" ");
            if (strValue.length() > 0 && uom != null) {
                ret.append(uom.getName());
            }
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
            final DecimalFormat formatter
                = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext().getLocale());
            strValue =  values[0] != null
                ? values[0] instanceof Number ? formatter.format(values[0]) : values[0].toString() : "";
            uomValue = (UoM) values[1];
        }

        if (_fieldValue.getTargetMode().equals(TargetMode.SEARCH)) {
            ret.append("<input type=\"text\" size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"").append(value != null ? value : "*").append("\" />");
        } else {
            ret.append("<span><input type=\"text\" size=\"").append(field.getCols())
                .append("\" name=\"").append(field.getName())
                .append("\" value=\"").append(strValue != null ? strValue : "").append("\"")
                .append(UIInterface.EFAPSTMPTAG).append("/>")
                .append("<select name=\"").append(_fieldValue.getField().getName()).append("UoM\" size=\"1\">");

            final Dimension dim = _fieldValue.getAttribute().getDimension();
            for (final UoM uom : dim.getUoMs()) {
                ret.append("<option value=\"").append(uom.getId());
                if (uomValue == null && uom.equals(dim.getBaseUoM()) || uomValue != null && uomValue.equals(uom)) {
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
    @SuppressWarnings("unchecked")
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
        throws EFapsException
    {
        int ret = 0;
        if (_fieldValue.getValue() instanceof Object[] && _fieldValue2.getValue() instanceof Object[]) {
            final Object[] values =  (Object[]) _fieldValue.getValue();
            final Object[] values2 =  (Object[]) _fieldValue2.getValue();

            if (values.length == 3 && values2.length == 3) {
                ret = ((Double) values[2]).compareTo((Double) values2[2]);
            } else {
                final BigDecimal val = (BigDecimal) values[0];
                final BigDecimal val2 = (BigDecimal) values2[0];
                final UoM uom = (UoM) values[1];
                final UoM uom2 = (UoM) values2[1];
                final BigDecimal tmpVal = val.multiply(new BigDecimal(uom.getNumerator())
                                .setScale(12, BigDecimal.ROUND_HALF_UP)
                                .divide(new BigDecimal(uom.getDenominator()), BigDecimal.ROUND_HALF_UP));
                final BigDecimal tmpVal2 = val2.multiply(new BigDecimal(uom2.getNumerator())
                                .setScale(12, BigDecimal.ROUND_HALF_UP)
                                .divide(new BigDecimal(uom2.getDenominator()), BigDecimal.ROUND_HALF_UP));
                ret = tmpVal.compareTo(tmpVal2);
            }
        } else if (_fieldValue.getValue() instanceof Comparable && _fieldValue2.getValue() instanceof Comparable) {
            ret = ((Comparable<Object>) _fieldValue.getValue()).compareTo(_fieldValue2.getValue());
        } else {
            ret = super.compare(_fieldValue, _fieldValue2);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue)
        throws EFapsException
    {
        Object ret = null;
        if (_fieldValue.getValue() != null && _fieldValue.getValue() instanceof Object[]) {
            final Object[] values =  (Object[]) _fieldValue.getValue();
            if (values.length == 3) {
                ret = values[2];
            } else {
                ret = values[0];
            }
        }
        return ret;
    }
}

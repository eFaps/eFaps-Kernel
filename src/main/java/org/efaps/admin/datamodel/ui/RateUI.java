/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.apache.commons.lang.StringEscapeUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RateUI
    extends AbstractUI
{
    /**
     * Suffix for the field in case the numerator is used.
     */
    public static final String INVERTEDSUFFIX = "_eFapsRateInverted";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Was the value for the rate inverted for the userinterface?
     * The value is set by the Return from the esjp by using the
     * <code>ReturnValue.TRUE</code>
     */
    private boolean inverted = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (_fieldValue.getAttribute() != null) {
            formatter.setMaximumFractionDigits(_fieldValue.getAttribute().getScale());
        }
        final StringBuilder ret = new StringBuilder();
        ret.append("<span name=\"").append(_fieldValue.getField().getName()).append("\" ")
            .append(UIInterface.EFAPSTMPTAG).append(">")
            .append(StringEscapeUtils.escapeHtml(formatter.format(getRate(_fieldValue))))
            .append("</span>");
        return ret.toString();
    }


    /**
     * Get the Rate for the UserInterface.
     * @param _fieldValue   FieldValue
     * @return rate
     * @throws EFapsException on error
     */
    protected BigDecimal getRate(final FieldValue _fieldValue)
        throws EFapsException
    {
        final Attribute attribute = _fieldValue.getAttribute();
        Object value = null;
        BigDecimal rate = BigDecimal.ONE;
        if (attribute != null && attribute.hasEvents(EventType.RATE_VALUE)) {
            final List<Return> returns = attribute.executeEvents(EventType.RATE_VALUE,
                                                ParameterValues.UIOBJECT, _fieldValue,
                                                ParameterValues.ACCESSMODE, _fieldValue.getTargetMode(),
                                                ParameterValues.ACCESSMODE, _fieldValue.getTargetMode(),
                                                ParameterValues.CALL_INSTANCE, _fieldValue.getCallInstance(),
                                                ParameterValues.INSTANCE, _fieldValue.getInstance(),
                                                ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            for (final Return values : returns) {
                value = values.get(ReturnValues.VALUES);
                this.inverted = values.get(ReturnValues.TRUE) != null;
            }
        } else {
            value = _fieldValue.getValue();
        }
        if (value instanceof Object[]) {
            final Object[] values = (Object[]) value;

            BigDecimal numerator;
            if (values[0] instanceof BigDecimal) {
                numerator = (BigDecimal) values[0];
            } else {
                numerator = DecimalType.parseLocalized(values[0].toString());
            }
            BigDecimal denominator;
            if (values[1] instanceof BigDecimal) {
                denominator = (BigDecimal) values[1];
            } else {
                denominator = DecimalType.parseLocalized(values[1].toString());
            }
            rate = numerator.divide(denominator,
                                numerator.scale() > denominator.scale() ? numerator.scale() : denominator.scale(),
                                BigDecimal.ROUND_UP);
        }
        return rate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        if (_fieldValue.getAttribute() != null) {
            formatter.setMaximumFractionDigits(_fieldValue.getAttribute().getScale());
        }
        final StringBuilder ret = new StringBuilder();
        ret.append("<input type=\"text\" size=\"").append(_fieldValue.getField().getCols())
            .append("\" name=\"").append(_fieldValue.getField().getName())
            .append("\" value=\"").append(StringEscapeUtils.escapeHtml(formatter.format(getRate(_fieldValue))))
            .append("\"").append(UIInterface.EFAPSTMPTAG).append("/>")
            .append("<input type=\"hidden\" name=\"").append(_fieldValue.getField().getName())
            .append(RateUI.INVERTEDSUFFIX).append("\" value=\"").append(this.inverted).append("\">");
        return ret.toString();
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
        return formatter.format(getRate(_fieldValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHiddenHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("<input type=\"hidden\" size=\"").append(_fieldValue.getField().getCols())
            .append("\" name=\"").append(_fieldValue.getField().getName())
            .append("\" value=\"").append(getRate(_fieldValue))
            .append("\"").append(UIInterface.EFAPSTMPTAG).append("/>")
            .append("<input type=\"hidden\" name=\"").append(_fieldValue.getField().getName())
            .append(RateUI.INVERTEDSUFFIX).append("\" value=\"").append(this.inverted).append("\">");
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
        throws EFapsException
    {
        final BigDecimal value;
        if (_fieldValue.getValue() instanceof BigDecimal) {
            value = (BigDecimal) _fieldValue.getValue();
        } else {
            value = getRate(_fieldValue);
        }
        final BigDecimal value2;
        if (_fieldValue2.getValue() instanceof BigDecimal) {
            value2 = (BigDecimal) _fieldValue2.getValue();
        } else {
            value2 = getRate(_fieldValue2);
        }
        return value.compareTo(value2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue)
        throws EFapsException
    {
        final BigDecimal value;
        if (_fieldValue.getValue() instanceof BigDecimal) {
            value = (BigDecimal) _fieldValue.getValue();
        } else {
            value = getRate(_fieldValue);
        }
        return value;
    }
}

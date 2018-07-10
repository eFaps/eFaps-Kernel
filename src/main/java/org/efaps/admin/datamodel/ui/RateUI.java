/*
 * Copyright 2003 - 2016 The eFaps Team
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class RateUI
    extends AbstractProvider
{
    /**
     * Suffix for the field in case the numerator is used.
     */
    public static final String INVERTEDSUFFIX = "_eFapsRateInverted";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        return transformObject(_uiValue, _uiValue.getDbValue());
    }

    @Override
    public String validateValue(final UIValue _uiValue)
        throws EFapsException
    {
        return null;
    }

    @Override
    public Object transformObject(final UIValue _uiValue,
                                  final Object _object)
        throws EFapsException
    {
        final Attribute attribute = _uiValue.getAttribute();
        Object value = null;
        boolean inverted = false;
        if (attribute != null && attribute.hasEvents(EventType.RATE_VALUE)) {
            if (_uiValue.getObject() == null && _object != null) {
                _uiValue.setDbValue((Serializable) _object);
            }
            final List<Return> returns = attribute.executeEvents(EventType.RATE_VALUE,
                            ParameterValues.UIOBJECT, _uiValue,
                            ParameterValues.ACCESSMODE, _uiValue.getTargetMode(),
                            ParameterValues.CALL_INSTANCE, _uiValue.getCallInstance(),
                            ParameterValues.INSTANCE, _uiValue.getInstance(),
                            ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            for (final Return values : returns) {
                value = values.get(ReturnValues.VALUES);
                inverted = values.get(ReturnValues.TRUE) != null;
            }
        } else {
            value = _uiValue.getDbValue();
        }
        final BigDecimal denominator;
        final BigDecimal numerator;
        if (value instanceof Object[]) {
            final Object[] values = (Object[]) value;

            if (values[0] instanceof BigDecimal) {
                numerator = (BigDecimal) values[0];
            } else {
                numerator = DecimalType.parseLocalized(values[0].toString());
            }

            if (values[1] instanceof BigDecimal) {
                denominator = (BigDecimal) values[1];
            } else {
                denominator = DecimalType.parseLocalized(values[1].toString());
            }
        } else {
            numerator = BigDecimal.ONE;
            denominator = BigDecimal.ONE;
        }
        return new Value(numerator, denominator, inverted);
    }

    /**
     * The Class Value.
     */
    public static class Value
        implements Serializable, Comparable<Value>
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The inverted. */
        private boolean inverted;

        /**
         * Numerator.
         */
        private BigDecimal numerator;

        /**
         * Denominator.
         */
        private BigDecimal denominator;


        /**
         * Instantiates a new value.
         * Constructor used by serialization implementations.
         */
        public Value()
        {
        }

        /**
         * Instantiates a new value.
         *
         * @param _numerator the _numerator
         * @param _denominator the _denominator
         * @param _inverted the _inverted
         */
        public Value(final BigDecimal _numerator,
                     final BigDecimal _denominator,
                     final boolean _inverted)
        {
            this.numerator = _numerator;
            this.denominator = _denominator;
            this.inverted = _inverted;
        }

        /**
         * Gets the rate.
         *
         * @return the rate
         */
        public BigDecimal getRate()
        {
            return getNumerator().divide(getDenominator(), getNumerator().scale() > getDenominator().scale()
                            ? getNumerator().scale() : getDenominator().scale(), RoundingMode.HALF_UP);
        }

        /**
         * Getter method for the instance variable {@link #inverted}.
         *
         * @return value of instance variable {@link #inverted}
         */
        public boolean isInverted()
        {
            return this.inverted;
        }

        /**
         * Setter method for instance variable {@link #inverted}.
         *
         * @param _inverted value for instance variable {@link #inverted}
         * @return the value
         */
        public Value setInverted(final boolean _inverted)
        {
            this.inverted = _inverted;
            return this;
        }

        /**
         * Getter method for the instance variable {@link #numerator}.
         *
         * @return value of instance variable {@link #numerator}
         */
        public BigDecimal getNumerator()
        {
            return this.numerator;
        }

        /**
         * Setter method for instance variable {@link #numerator}.
         *
         * @param _numerator value for instance variable {@link #numerator}
         * @return the value
         */
        public Value setNumerator(final BigDecimal _numerator)
        {
            this.numerator = _numerator;
            return this;
        }

        /**
         * Getter method for the instance variable {@link #denominator}.
         *
         * @return value of instance variable {@link #denominator}
         */
        public BigDecimal getDenominator()
        {
            return this.denominator;
        }

        /**
         * Setter method for instance variable {@link #denominator}.
         *
         * @param _denominator value for instance variable {@link #denominator}
         * @return the value
         */
        public Value setDenominator(final BigDecimal _denominator)
        {
            this.denominator = _denominator;
            return this;
        }

        @Override
        public int compareTo(final Value _arg0)
        {
            int ret = 0;
            if (_arg0 != null) {
                ret = _arg0.getRate().compareTo(getRate());
            }
            return ret;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}

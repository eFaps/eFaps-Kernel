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


package org.efaps.admin.datamodel.attributevalue;

import java.math.BigDecimal;


/**
 * Value for a Rate stored in the eFaps Database.
 *
 * @author The eFaps Team
 *
 */
public class Rate
{
    /**
     * Numerator.
     */
    private final BigDecimal numerator;

    /**
     * Denominator.
     */
    private final BigDecimal denominator;

    /**
     * @param _numerator    Numerator
     * @param _denominator  Denominator
     */
    public Rate(final BigDecimal _numerator,
                final BigDecimal _denominator)
    {
        this.numerator = _numerator;
        this.denominator = _denominator;
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
     * Getter method for the instance variable {@link #denominator}.
     *
     * @return value of instance variable {@link #denominator}
     */
    public BigDecimal getDenominator()
    {
        return this.denominator;
    }
}

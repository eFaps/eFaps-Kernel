/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.admin.datamodel.attributevalue;

import org.efaps.admin.datamodel.Dimension.UoM;

/**
 * Abstract class used to implement attribute values with unit of measures.
 *
 * @param <VALUE>   class of the VALUE with unit of measure
 * @author The eFaps Team
 * @version $Id$
 * @see UoM
 */
public abstract class AbstractWithUoM<VALUE extends Number>
{
    /**
     * Link to the unit of measure instance.
     */
    private final UoM uom;

    /**
     * Value itself.
     */
    private final VALUE value;

    /**
     * Initializes this class with {@link #value} and link to the
     * {@link #uom unit of measure}.
     *
     * @param _value    new value
     * @param _uom      link to the unit of measure
     * @see #value
     * @see #uom
     */
    protected AbstractWithUoM(final VALUE _value,
                              final UoM _uom)
    {
        this.value = _value;
        this.uom = _uom;
    }

    /**
     * Returns the {@link #uom unit of measure} instance.
     *
     * @return unit of measure instance
     * @see #uom
     */
    public UoM getUoM()
    {
        return this.uom;
    }

    /**
     * Returns the {@link #value}.
     *
     * @return value
     * @see #value
     */
    public VALUE getValue()
    {
        return this.value;
    }

    /**
     * Converts the {@link #value} in a standardized value depended on defined
     * {@link UoM#getNumerator() numerator} and {@link UoM#getDenominator() denominator}.
     *
     * @return standardized value
     * @see #value
     * @see UoM#getBaseDouble(Double)
     */
    public Double getBaseDouble()
    {
        return (this.value == null) || (this.uom == null)
               ? null
               : this.uom.getBaseDouble(this.value.doubleValue());
    }
}

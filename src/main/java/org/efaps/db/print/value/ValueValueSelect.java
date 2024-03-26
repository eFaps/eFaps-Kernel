/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.print.value;

import java.math.BigDecimal;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractWithUoMType;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.attributetype.RateType;
import org.efaps.db.print.OneSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class ValueValueSelect
    extends AbstractValueSelect
    implements IAttributeChildValueSelect
{

    /**
     * @param _oneSelect OneSelect
     */
    public ValueValueSelect(final OneSelect _oneSelect)
    {
        super(_oneSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "value";
    }

    /**
     * @param _attribute Attribute this value is wanted for
     * @param _object object containing the value for the attribute
     * @return value
     * @throws EFapsException on error
     */
    public Object get(final Attribute _attribute,
                      final Object _object)
        throws EFapsException
    {
        final Object ret;
        if (_attribute.getAttributeType().getDbAttrType() instanceof RateType) {
            ret = getRate(_attribute, _object);
        } else if (_attribute.getAttributeType().getDbAttrType() instanceof AbstractWithUoMType) {
            ret = getValueUOM(_object);
        } else {
            ret = _object;
        }
        return ret;
    }

    /**
     * @param _attribute    Attribute the rate is wanted for
     * @param _object       object the rate is wanted for
     * @return Object
     * @throws EFapsException on error
     */
    protected Object getRate(final Attribute _attribute,
                             final Object _object)
        throws EFapsException
    {
        Object ret = null;
        if (_object instanceof Object[]) {
            final Object[] values = (Object[]) _object;

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
            // the oracle jdbc returns BigDecimal from the database that does not have the scale as
            // it is set explicitly, therefore it is forced here (before calculation)
            if (_attribute != null) {
                if (numerator.scale() < _attribute.getScale()) {
                    numerator = numerator.setScale(_attribute.getScale());
                }
                if (denominator.scale() < _attribute.getScale()) {
                    denominator = denominator.setScale(_attribute.getScale());
                }
            }
            ret = numerator.divide(denominator,
                                numerator.scale() > denominator.scale() ? numerator.scale() : denominator.scale(),
                                BigDecimal.ROUND_UP);
        }
        return ret;
    }

    /**
     * @param _object object the value is wanted for
     * @return Object
     */
    protected Object getValueUOM(final Object _object)
    {
        Object ret = null;
        if (_object instanceof Object[]) {
            final Object[] values = (Object[]) _object;
            ret = values[0];
        }
        return ret;
    }
}

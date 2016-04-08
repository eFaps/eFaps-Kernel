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

package org.efaps.admin.datamodel.attributetype;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.attributevalue.DecimalWithUoM;
import org.efaps.db.Context;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * Implements the mapping between values in the database and
 * {@link DecimalWithUoM} values in eFaps.
 *
 * @author The eFaps Team
 *
 */
public class DecimalWithUoMType
    extends AbstractWithUoMType
    implements IFormattableType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The method prepares the statement for insert the object in the database.
     * It must be overwritten, because this type has at least two columns.
     * {@inheritDoc}
     */
    @Override
    public void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                        final Attribute _attribute,
                        final Object... _values)
        throws SQLException
    {
        if (_attribute.getSqlColNames().size() == 3)  {
            checkSQLColumnSize(_attribute, 3);
        } else  {
            checkSQLColumnSize(_attribute, 2);
        }

        final DecimalWithUoM value = eval(_values);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), value == null ? null : value.getValue());
        _insertUpdate.column(_attribute.getSqlColNames().get(1), value == null ? null : value.getUoM().getId());
        if (_attribute.getSqlColNames().size() == 3) {
            _insertUpdate.column(_attribute.getSqlColNames().get(2), value == null ? null : value.getBaseDouble());
        }
    }

    /**
     * The localized string and the internal string value are equal. So the
     * internal value can be evaluated directly.
     *
     * @param _values   values to evaluate
     * @return evaluated big decimal value with unit of measure
     * @throws SQLException on error
     */
    protected DecimalWithUoM eval(final Object... _values)
        throws SQLException
    {
        final DecimalWithUoM ret;

        if (_values == null || _values.length < 2)  {
            ret = null;
        } else  {
            final BigDecimal value;
            if (_values[0] instanceof String && ((String) _values[0]).length() > 0) {
                try {
                    value = DecimalType.parseLocalized((String) _values[0]);
                } catch (final EFapsException e) {
                    throw new SQLException(e);
                }
            } else if (_values[0] instanceof BigDecimal) {
                value = (BigDecimal) _values[0];
            } else if (_values[0] instanceof Number) {
                value = new BigDecimal(((Number) _values[0]).toString());
            } else  {
                value = null;
            }

            final UoM uom;
            if (_values[1] instanceof UoM) {
                uom = (UoM) _values[1];
            } else if (_values[1] instanceof String && ((String) _values[1]).length() > 0) {
                uom = Dimension.getUoM(Long.parseLong((String) _values[1]));
            } else if (_values[1] instanceof Number) {
                uom = Dimension.getUoM(((Number) _values[1]).longValue());
            } else  {
                uom = null;
            }
            ret = new DecimalWithUoM(value, uom);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object readValue(final Object _object)
    {
        final BigDecimal ret;

        if (_object instanceof BigDecimal) {
            ret = (BigDecimal) _object;
        } else if (_object != null) {
            ret = new BigDecimal(_object.toString());
        } else  {
            ret = null;
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
        final Object ret;
        final DecimalFormat formatter
            = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext().getLocale());
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

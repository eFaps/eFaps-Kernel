/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributevalue.Rate;
import org.efaps.db.query.CachedResult;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RateType
    extends AbstractType
{

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
        checkSQLColumnSize(_attribute, 2);
        final Rate value = eval(_values);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), value.getNumerator());
        _insertUpdate.column(_attribute.getSqlColNames().get(1), value.getDenominator());
    }

    /**
     * The localized string and the internal string value are equal. So the
     * internal value can be evaluated directly.
     *
     * @param _values values to evaluate
     * @return evaluated big decimal value with unit of measure
     * @throws SQLException on error
     */
    protected Rate eval(final Object... _values)
        throws SQLException
    {
        final Rate ret;
        if ((_values == null) || (_values[0] == null)) {
            ret = null;
        } else if (_values[0] instanceof Object[]) {
            final Object[] valueTmp = (Object[]) _values[0];
            if (valueTmp.length < 2) {
                ret = null;
            } else {
                ret = new Rate(evalObject(valueTmp[0]), evalObject(valueTmp[1]));
            }
        } else {
            ret = new Rate(evalObject(_values[0]), evalObject(_values[1]));
        }
        return ret;
    }

    /**
     * @param _value value to evaluated
     * @return BigDecimal
     * @throws SQLException on error
     */
    protected BigDecimal evalObject(final Object _value)
        throws SQLException
    {
        BigDecimal ret;
        if ((_value instanceof String) && (((String) _value).length() > 0)) {
            try {
                ret = DecimalType.parseLocalized((String) _value);
            } catch (final EFapsException e) {
                throw new SQLException(e);
            }
        } else if (_value instanceof BigDecimal) {
            ret = (BigDecimal) _value;
        } else if (_value instanceof Number) {
            ret = new BigDecimal(((Number) _value).toString());
        } else {
            ret = BigDecimal.ONE;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        final List<Object[]> ret = new ArrayList<Object[]>();
        for (final Object object : _objectList) {
            final Object[] temp = (Object[]) object;
            final Object numerator = readValue(temp[0]);
            final Object denominator = readValue(temp[1]);
            ret.add(new Object[] { numerator, denominator });
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * @param _object objetc to read
     * @return object
     */
    protected Object readValue(final Object _object)
    {
        final BigDecimal ret;
        if (_object instanceof BigDecimal) {
            ret = (BigDecimal) _object;
        } else if (_object != null) {
            ret = new BigDecimal(_object.toString());
        } else {
            ret = BigDecimal.ONE;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
        throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }
}

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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * Implements the mapping between values in the database and {@link BigDecimal}
 * values in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DecimalType extends AbstractType
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                        final Attribute _attribute,
                        final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_values));
    }

    /**
     * Evaluate the value.
     * @param _values value to be evaluated
     * @return BigDecimal value
     * @throws SQLException on error
     */
    protected BigDecimal eval(final Object... _values)
        throws SQLException
    {
        final BigDecimal ret;

        if ((_values == null) || (_values.length == 0) || (_values[0] == null))  {
            ret = null;
        } else if ((_values[0] instanceof String) && (((String) _values[0]).length() > 0)) {
            try {
                ret = DecimalType.parseLocalized((String) _values[0]);
            } catch (final EFapsException e) {
                throw new SQLException(e);
            }
        } else if (_values[0] instanceof BigDecimal) {
            ret = (BigDecimal) _values[0];
        } else if (_values[0] instanceof Number) {
            ret = new BigDecimal(((Number) _values[0]).toString());
        } else  {
            ret = null;
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
    {

        return _rs.getDecimal(_indexes.get(0).intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
    {
        final List<BigDecimal> ret = new ArrayList<BigDecimal>();
        for (final Object object : _objectList) {
            if (object instanceof BigDecimal) {
                ret.add((BigDecimal) object);
            } else if (object != null) {
                ret.add(new BigDecimal(object.toString()));
            }
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : (ret.size() > 0 ? ret.get(0) : null)) : null;
    }

    /**
     * Method to parse a localized String to an BigDecimal.
     *
     * @param _value value to be parsed
     * @return  BigDecimal
     * @throws EFapsException on error
     */
    public static BigDecimal parseLocalized(final String _value) throws EFapsException
    {
        final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        format.setParseBigDecimal(true);
        try {
            return (BigDecimal) format.parse(_value);
        } catch (final ParseException e) {
            throw new EFapsException(DecimalType.class, "ParseException", e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString4Where(final Object _value)
        throws EFapsException
    {
        String ret = "";
        if (_value instanceof BigDecimal) {
            ret =  ((BigDecimal) _value).toPlainString();
        } else if (_value instanceof String) {
            ret = (String) _value;
        } else if (_value != null) {
            ret = (new BigDecimal(_value.toString())).toPlainString();
        }
        return ret;
    }
}

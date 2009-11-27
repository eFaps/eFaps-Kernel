/*
 * Copyright 2003 - 2009 The eFaps Team
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class DecimalType extends AbstractType
{
    /**
     * Value of this type.
     */
    private BigDecimal value = new BigDecimal(0);

    /**
     *{@inheritDoc}
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
        throws SQLException
    {
        _stmt.setBigDecimal(_index, this.value);
        return 1;
    }

    /**
     *{@inheritDoc}
     */
    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {

        final BigDecimal val = _rs.getDecimal(_indexes.get(0).intValue());
        this.value = (val != null) ? val : new BigDecimal(0);
        return this.value;
    }

    /**
     *{@inheritDoc}
     */
    public Object readValue(final List<Object> _objectList)
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
     *{@inheritDoc}
     */
    public void set(final Object[] _value) throws EFapsException
    {
        if (_value != null) {
            if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
                this.value = parseLocalized((String) _value[0]);
            } else if (_value[0] instanceof BigDecimal) {
                this.value = ((BigDecimal) _value[0]);
            } else if (_value[0] instanceof Number) {
                this.value = (new BigDecimal(((Number) _value[0]).toString()));
            }
        }
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
        final DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        format.setParseBigDecimal(true);
        try {
            return (BigDecimal) format.parse(_value);
        } catch (final ParseException e) {
            throw new EFapsException(DecimalType.class, "ParseException", e);
        }
    }
}

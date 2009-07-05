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
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class DecimalType extends AbstractType
{
    /**
     * @see #getValue
     * @see #setValue
     */
    private BigDecimal value = new BigDecimal(0);

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
            throws SQLException
    {
        _stmt.setBigDecimal(_index, getValue());
        return 1;
    }

    /**
     * @todo test that only one value is given for indexes
     */

    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {

        final BigDecimal val = _rs.getDecimal(_indexes.get(0).intValue());
        this.value = (val != null) ? val : new BigDecimal(0);
        return this.value;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return Decimal
     * TODO throw error if more than one value is given
     */
    public Object readValue(final List<Object> _objectList)
    {
        BigDecimal ret = null;
        final Object obj = _objectList.get(0);
        if (obj instanceof BigDecimal) {
            ret = (BigDecimal) obj;
        } else if (obj != null) {
            ret = new BigDecimal(obj.toString());
        }

        this.value = ret;
        return ret;
    }

    public void set(final Object[] _value)
    {
        if (_value != null) {
            if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
                setValue(new BigDecimal((String) _value[0]));
            } else if (_value[0] instanceof BigDecimal) {
                setValue((BigDecimal) _value[0]);
            } else if (_value[0] instanceof Number) {
                setValue(new BigDecimal(((Number) _value[0]).toString()));
            }
        }
    }

    /**
     * This is the setter method for instance variable {@link #value}.
     *
     * @param _value new value for instance variable {@link #value}
     * @see #value
     * @see #getValue
     */
    public void setValue(final BigDecimal _value)
    {
        this.value = _value;
    }

    /**
     * This is the getter method for instance variable {@link #value}.
     *
     * @return the value of the instance variable {@link #value}.
     * @see #value
     * @see #setValue
     */
    public BigDecimal getValue()
    {
        return this.value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.efaps.admin.datamodel.AttributeTypeInterface#get()
     */
    public Object get()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return "" + getValue();
    }

}

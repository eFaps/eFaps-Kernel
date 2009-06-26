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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.admin.datamodel.Dimension;
import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class IntegerWithUoMType extends AbstractWithUoMType
{

    /**
     * @see #getValue
     * @see #setValue
     */
    private int value = 0;

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index) throws SQLException
    {
        _stmt.setInt(_index, getValue());
        _stmt.setLong(_index + 1, getUoM().getId());
        return 2;
    }

    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        this.value = _rs.getLong(_indexes.get(0)).intValue();
        setUoM(Dimension.getUoM(_rs.getLong(_indexes.get(1))));
        return new Object[]{this.value, getUoM()};
    }

    /**
     * The localised string and the internal string value are equal. So the
     * internal value can be set directly with method {@link #setValue}.
     *
     * @param _value new value to set
     */
    public void set(final Object[] _value)
    {
        if (_value instanceof Object[]) {
            if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
                this.value = (Integer.parseInt((String) _value[0]));
            } else if (_value[0] instanceof Number) {
                this.value = (((Number) _value[0]).intValue());
            }
            if ((_value[1] instanceof String) && (((String) _value[1]).length() > 0)) {
                setUoM(Dimension.getUoM(Long.parseLong((String) _value[1])));
            } else if (_value[1] instanceof Number) {
                setUoM(Dimension.getUoM(((Number) _value[1]).longValue()));
            }
        }
    }

    /**
     * This is the getter method for instance variable {@link #value}.
     *
     * @return the value of the instance variable {@link #value}.
     * @see #value
     * @see #setValue
     */
    public Integer getValue()
    {
        return this.value;
    }
}

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
    private Integer value = 0;

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
    @Override
    protected Double getValue()
    {
        return this.value.doubleValue();
    }

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractWithUoMType#setValueStmt(java.sql.PreparedStatement, int)
     * @param _stmt     prepared statement
     * @param _index    index
     * @throws SQLException on error
     */
    @Override
    protected void setValueStmt(final PreparedStatement _stmt, final int _index) throws SQLException
    {
        _stmt.setInt(_index, this.value);
    }

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractWithUoMType#readValue(org.efaps.db.query.CachedResult, int)
     * @param _rs       cached result
     * @param _index     index
     * @return the value as Integer
     */
    @Override
    protected Object readValue(final CachedResult _rs, final int _index)
    {
        this.value = _rs.getLong(_index).intValue();
        return this.value;
    }
}

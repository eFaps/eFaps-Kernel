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

import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class RealType extends AbstractType
{

    /**
     * The instance method stores the value for this real type.
     *
     * @see #getValue
     * @see #setValue
     */
    private double value = 0;
    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _indexes)
                    throws SQLException
    {
        _stmt.setDouble(_indexes, this.value);
        return 1;
    }

    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        this.value = (_rs.getDouble(_indexes.get(0).intValue()));
        return _rs.getDouble(_indexes.get(0).intValue());
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return DateTime
     * TODO throw error if more than one value is given
     */
    public Object readValue(final List<Object> _objectList)
    {
        Double ret = null;
        final Object obj = _objectList.get(0);
        if (obj instanceof Number) {
            ret = ((Number) obj).doubleValue();
        } else if (obj != null) {
            ret = Double.parseDouble(obj.toString());
        }
        this.value = ret;
        return ret;
    }

    public void set(final Object[] _value)
    {
        if (_value != null) {
            if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
                this.value = (Double.parseDouble((String) _value[0]));
            } else if (_value[0] instanceof Number) {
                this.value = (((Number) _value[0]).doubleValue());
            }
        }
    }

    @Override
    public String toString()
    {
        return "" + this.value;
    }
}

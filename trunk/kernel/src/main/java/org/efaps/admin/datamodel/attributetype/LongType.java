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
import java.util.ArrayList;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 * @todo description
 */
public class LongType extends AbstractType
{

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * @see #getValue
     * @see #setValue
     */
    private long value = 0;

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _Object, final PreparedStatement _stmt, final int _index)
                    throws SQLException
    {
        _stmt.setLong(_index, this.value);
        return 1;
    }

    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        final Long temp = _rs.getLong(_indexes.get(0).intValue());
        this.value = (temp != null ? temp.longValue() : 0);
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    public Object readValue(final List<Object> _objectList)
    {
        Object ret = null;
        if (_objectList.size() < 1) {
            ret = null;
        } else if (_objectList.size() > 1) {
            final List<Long> list = new ArrayList<Long>();
            for (final Object object : _objectList) {
                if (object instanceof Number) {
                    ret = ((Number) object).longValue();
                } else if (object != null) {
                    ret = Long.parseLong(object.toString());
                }
                list.add((Long) (object == null ? new Long(0) : object));
            }
            ret = list;
        } else {
            final Object object = _objectList.get(0);
            if (object instanceof Number) {
                ret = ((Number) object).longValue();
            } else if (object != null) {
                ret = Long.parseLong(object.toString());
            }
            ret = object == null ? 0 : object;
        }
        return ret;
    }

    public void set(final Object[] _value)
    {
        if (_value != null) {
            if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
                this.value = Long.parseLong((String) _value[0]);
            } else if (_value[0] instanceof Number) {
                this.value = (((Number) _value[0]).longValue());
            }
        }
    }


    @Override
    public String toString()
    {
        return "" + this.value;
    }
}

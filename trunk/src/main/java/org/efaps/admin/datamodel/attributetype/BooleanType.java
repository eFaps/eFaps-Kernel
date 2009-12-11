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
public class BooleanType extends AbstractType
{
    /**
     * @see #getValue
     * @see #setValue
     */
    private boolean value = false;

    /**
     * {@inheritDoc}
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index) throws SQLException
    {
        _stmt.setBoolean(_index, this.value);
        return 1;
    }

    /**
     * TODO: test that only one value is given for indexes
     */
    public Object readValue(final CachedResult _rs, final List<Integer> _indexes) throws SQLException
    {
        final Boolean tmp = _rs.getBoolean(_indexes.get(0).intValue());
        if (tmp != null) {
            this.value = tmp;
        }
        return tmp;
    }

    /**
     * {@inheritDoc}
     */
    public Object readValue(final List<Object> _objectList)
    {
        Boolean ret = null;
        final Object obj = _objectList.get(0);
        if (obj instanceof Boolean) {
            ret = (Boolean) obj;
        } else if (obj instanceof Number) {
            final Integer intvalue = ((Number) obj).intValue();
            if ((intvalue != null) && (intvalue != 0)) {
                ret = true;
            } else {
                ret = false;
            }
        }
        if (ret != null) {
            this.value = ret;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public void set(final Object[] _value)
    {
        if (_value != null) {
            if (_value[0] instanceof String) {
                if (((String) _value[0]).equalsIgnoreCase("TRUE")) {
                    this.value = true;
                } else {
                    this.value = false;
                }
            } else if (_value[0] instanceof Boolean) {
                this.value = ((Boolean) _value[0]);
            }
        } else {
            this.value = false;
        }
    }

    @Override
    public String toString()
    {
        return "" + this.value;
    }
}

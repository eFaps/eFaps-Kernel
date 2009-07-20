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
 */
public class StringType extends AbstractType
{
    /**
     * @see #getValue
     * @see #setValue
     */
    private final List<String> values = new ArrayList<String>();;

    /**
     *{@inheritDoc}
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
                    throws SQLException
    {
        _stmt.setString(_index, getValue());
        return 1;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(org.efaps.db.query.CachedResult, java.util.List)
     * @param _rs       CachedResult
     * @param _indexes  indexsx
     * @return Object
     */
    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        String value = (_rs.getString(_indexes.get(0).intValue()));
        this.values.add(value);
        if (value != null) {
            value = value.trim();
        }
        return value;
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
            final List<String> list = new ArrayList<String>();
            for (final Object object : _objectList) {
                list.add(object == null ? "" : object.toString().trim());
            }
            ret = list;
        } else {
            final Object object = _objectList.get(0);
            ret = object == null ? "" : object.toString().trim();
        }
        return ret;
    }

    /**
     * The localised string and the internal string value are equal. So the
     * internal value can be set directly with method {@link #setValue}.
     *
     * @param _value new value to set
     */
    public void set(final Object[] _value)
    {
        if (_value[0] instanceof String) {
            this.values.add((String) _value[0]);
        } else if (_value[0] != null) {
            this.values.add(_value[0].toString());
        }
    }

    /**
     * Getter method for instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    protected String getValue()
    {
        return this.values.size() > 0 ? this.values.get(0) : "";
    }

    /**
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     */
    protected void setValue(final String _value)
    {
        this.values.set(0, _value);
    }

    /**
     * @see java.lang.Object#toString()
     * @return string representation
     */
    @Override
    public String toString()
    {
        return "" + this.values;
    }
}

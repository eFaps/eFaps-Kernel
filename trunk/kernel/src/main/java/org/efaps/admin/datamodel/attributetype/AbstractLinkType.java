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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.efaps.db.query.CachedResult;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 * @todo till now only integer / Long ID's are allowed; this must be changed
 */
public abstract class AbstractLinkType extends AbstractType
{
    /**
     * @see #getValue
     * @see #setValue
     */
    private Object value = null;

    /**
     * @param _value new value to set
     */
    public void set(final Object[] _value)
    {
        if (_value != null) {
            if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
                this.value = (Long.parseLong((String) _value[0]));
            } else if (_value[0] instanceof Long) {
                this.value = (_value[0]);
            }
        }
    }

    /**
     *  Updates the value in the database with the stored value in the cache. If
     * the value is '0', the value in the database is set to <i>NULL</i> (a zero
     * in the cache means no link!).
     *
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index) throws SQLException
    {
        if (this.value == null) {
            _stmt.setNull(_index, Types.INTEGER);
        } else {
            _stmt.setObject(_index, getValue());
        }
        return 1;
    }

    /**
     * @param _rs
     * @param _index
     * @todo test that only one value is given for indexes
     */
    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        this.value = (_rs.getObject(_indexes.get(0)));
        return this.value;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return Object
     * TODO throw error if more than one value is given
     * @throws EFapsException
     */
    public Object readValue(final List<Object> _objectList) throws EFapsException
    {
        Object ret = null;
        this.value = _objectList.get(0);
        if (_objectList.size() > 0) {
            final List<Object> list = new ArrayList<Object>();
            ret = list;
            list.addAll(_objectList);
        } else {
            ret = this.value;
        }
        return ret;
    }

    /**
     * This is the getter method for instance variable {@link #value}.
     *
     * @return the value of the instance variable {@link #value}.
     * @see #value
     * @see #setValue
     */
    protected Object getValue()
    {
        return this.value;
    }

    /**
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     */
    protected void setValue(final Object _value)
    {
        this.value = _value;
    }
}

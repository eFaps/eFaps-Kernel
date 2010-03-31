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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.query.CachedResult;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * Implements the mapping between values in the database and {@link Long}
 * values in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 * TODO: description
 */
public class LongType
    extends AbstractType
{
    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    /**
     * {@inheritDoc}
     */
    @Override()
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_values));
    }

    protected Long eval(final Object... _values)
    {
        final Long ret;

        if ((_values == null) || (_values.length == 0) || (_values[0] == null))  {
            ret = null;
        } else if ((_values[0] instanceof String) && !"".equals(_values[0])) {
            ret = Long.parseLong((String) _values[0]);
        } else if (_values[0] instanceof Number) {
            ret = (((Number) _values[0]).longValue());
        } else  {
            ret = null;
        }

        return ret;
    }

    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
    {
        return _rs.getLong(_indexes.get(0).intValue());
    }

    /**
     * {@inheritDoc}
     */
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString4Where(final Object _value)
        throws EFapsException
    {
        String ret = "";
        if (_value instanceof Number) {
            ret =  ((Number) _value).toString();
        } else if (_value instanceof String) {
            ret = (String) _value;
        } else if (_value != null) {
            ret = _value.toString();
        }
        return ret;
    }
}

/*
 * Copyright 2003 - 2011 The eFaps Team
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
 * Implements the mapping between values in the database and {@link String}
 * values in eFaps.

 * @author The eFaps Team
 * @version $Id$
 */
public class StringType
    extends AbstractType
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_values));
    }

    /**
     * The localized string and the internal string value are equal. So the
     * internal value can be set directly with method {@link #setValue}.
     *
     * @param _values   values to evaluate
     * @return string representation for the <code>_values</code>
     */
    protected String eval(final Object[] _values)
    {
        final String ret;
        if ((_values == null) || (_values.length == 0) || (_values[0] == null))  {
            ret = null;
        } else if (_values[0] instanceof String) {
            ret = (String) _values[0];
        } else if (_values[0] != null) {
            ret = _values[0].toString();
        } else  {
            ret = null;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
    {
        String value = _rs.getString(_indexes.get(0).intValue());
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
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
     * {@inheritDoc}
     */
    @Override
    public String toString4Where(final Object _value)
        throws EFapsException
    {
        String ret = "";
        if (_value instanceof String) {
            ret = (String) _value;
        } else if (_value != null) {
            ret = _value.toString();
        }
        return ret;
    }
}

/*
 * Copyright 2003 - 2013 The eFaps Team
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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.attributevalue.StringWithUoM;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;

/**
 * Implements the mapping between values in the database and {@link String}
 * values in eFaps.

 * @author The eFaps Team
 * @version $Id$
 */
public class StringWithUoMType
    extends AbstractWithUoMType
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
        checkSQLColumnSize(_attribute, 2);
        final StringWithUoM value = eval(_values);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), value.getValue());
        _insertUpdate.column(_attribute.getSqlColNames().get(1), value.getUoM().getId());
    }

    /**
     * The localized string and the internal string value are equal. So the
     * internal value can be set directly with method {@link #setValue}.
     *
     * @param _values   values to evaluate
     * @return string representation for the <code>_values</code>
     */
    protected StringWithUoM eval(final Object[] _values)
    {
        StringWithUoM ret;
        if ((_values == null) || (_values.length == 0) || (_values.length < 2))  {
            ret = null;
        } else  {
            final String value;
            if ((_values[0] instanceof String) && !((String) _values[0]).isEmpty()) {
                value = (String) _values[0];
            } else if (_values[0] != null) {
                value =  _values[0].toString();
            } else  {
                value = null;
            }

            final UoM uom;
            if (_values[1] instanceof UoM) {
                uom = (UoM) _values[1];
            } else if ((_values[1] instanceof String) && (((String) _values[1]).length() > 0)) {
                uom = Dimension.getUoM(Long.parseLong((String) _values[1]));
            } else if (_values[1] instanceof Number) {
                uom = Dimension.getUoM(((Number) _values[1]).longValue());
            } else  {
                uom = null;
            }
            ret = new StringWithUoM(value, uom);
        }
        return ret;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Object readValue(final Object _object)
    {
        final String ret;
        if (_object instanceof String) {
            ret = (String) _object;
        } else if (_object != null) {
            ret = _object.toString();
        } else  {
            ret = null;
        }
        return ret == null ? ret : ret.trim();
    }
}

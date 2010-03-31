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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.attributevalue.IntegerWithUoM;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;

/**
 * Implements the mapping between values in the database and
 * {@link IntegerWithUoM} values in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class IntegerWithUoMType
    extends AbstractWithUoMType
{
    /**
     * The method prepares the statement for insert the object in the database.
     * It must be overwritten, because this type has at least two columns.
     * {@inheritDoc}
     */
    @Override()
    public void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                        final Attribute _attribute,
                        final Object... _values)
        throws SQLException
    {
        if (_attribute.getSqlColNames().size() == 3)  {
            checkSQLColumnSize(_attribute, 3);
        } else  {
            checkSQLColumnSize(_attribute, 2);
        }

        final IntegerWithUoM value = eval(_values);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), value.getValue());
        _insertUpdate.column(_attribute.getSqlColNames().get(1), value.getUoM().getId());
        if (_attribute.getSqlColNames().size() == 3) {
            _insertUpdate.column(_attribute.getSqlColNames().get(2), value.getBaseDouble());
        }
    }

    /**
     * The localized string and the internal string value are equal. So the
     * internal value can be set directly with method {@link #setValue}.
     *
     * @param _values new value to set
     * @return related value with unit of measure
     */
    protected IntegerWithUoM eval(final Object... _values)
    {
        final IntegerWithUoM ret;

        if ((_values == null) || (_values.length < 2) || (_values[0] == null))  {
            ret = null;
        } else  {
            final Long value;
            if ((_values[0] instanceof String) && (((String) _values[0]).length() > 0)) {
                value = Long.parseLong((String) _values[0]);
            } else if (_values[0] instanceof Number) {
                value = ((Number) _values[0]).longValue();
            } else  {
                value = null;
            }

            final UoM uom;
            if ((_values[1] instanceof String) && (((String) _values[1]).length() > 0)) {
                uom = Dimension.getUoM(Long.parseLong((String) _values[1]));
            } else if (_values[1] instanceof Number) {
                uom = Dimension.getUoM(((Number) _values[1]).longValue());
            } else  {
                uom = null;
            }

            ret = new IntegerWithUoM(value, uom);
        }
        return ret;
    }

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractWithUoMType#readValue(org.efaps.db.query.CachedResult, int)
     * @param _object    Object to read
     * @return the value as Integer
     */
    @Override()
    protected Object readValue(final Object _object)
    {
        final Long ret;
        if (_object instanceof Number) {
            ret = ((Number) _object).longValue();
        } else if (_object != null) {
            ret = Long.parseLong(_object.toString());
        } else  {
            ret = null;
        }
        return ret;
    }
}

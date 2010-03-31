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
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.query.CachedResult;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;

/**
 * Implements the mapping between values in the database and {@link Boolean}
 * values in eFaps. Internally in the database the boolean value could be
 * implemented native as boolean or as a number value. If the database uses
 * number values all values which are not <code>null</code> and not zero are
 * interpreted from eFaps as <i>true</i>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class BooleanType
    extends AbstractType
{
    /**
     * Converts given <code>_value</code> into a {@link Boolean} value and set
     * for the {@link Attribute#getSqlColNames() SQL columns} in
     * <code>_attribute</code> this value in <code>_insertUpdate</code> SQL
     * statement.
     *
     * @param _insertUpdate SQL insert / update statement
     * @param _attribute    SQL update statement
     * @param _value        new object value to set; values are localized and
     *                      are coming from the user interface
     * @throws SQLException if size of the SQL columns of
     *                      <code>_attribute</code> is not correct
     */
    @Override()
    public void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                        final Attribute _attribute,
                        final Object... _value)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), this.eval(_value));
    }

    /**
     * Evaluates given <code>_value</code> and converts them to a
     * {@link Boolean} value.
     *
     * @param _value    value to evaluate
     * @return related evaluated boolean value
     */
    protected boolean eval(final Object... _value)
    {
        final Boolean ret;

        if (_value == null) {
            ret = null;
        } else if (_value[0] instanceof String) {
            ret = Boolean.valueOf((String) _value[0]);
        } else if (_value[0] instanceof Boolean) {
            ret = (Boolean) _value[0];
        } else  {
            ret = false;
        }

        return ret;
    }

    /**
     * TODO: test that only one value is given for indexes
     *
     * @throws SQLException if value could not be read
     */
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
        throws SQLException
    {
        return _rs.getBoolean(_indexes.get(0).intValue());
    }

    /**
     * Evaluates the <code>_objectList</code> and interprets this value as
     * boolean.
     *
     * @param _attribute        related eFaps attribute definition; not used
     * @param _objectList       list of values for the attribute definition
     * @return related interpreted boolean value if object list is
     *         {@link Boolean} or {@link Number}; otherwise <code>null</code>
     *         is returned
     */
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
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
        return ret;
    }
}

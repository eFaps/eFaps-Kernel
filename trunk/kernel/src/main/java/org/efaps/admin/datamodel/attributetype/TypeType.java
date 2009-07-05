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

import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class TypeType extends AbstractType
{

    /**
     * The value stores the instance of {@link org.efaps.admin.datamodel.Type}
     * which represents current value.
     *
     * @see #setType
     * @see #getType
     */
    private Type value = null;

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
            throws SQLException
    {
        throw new SQLException("Update value for Type not allowed!!!");
    }

    /**
     * The method reads from a SQL result set the value for the type. If no type
     * sql column is given in the type description, the value is read directly
     * from the attribute.
     */
    public Object readValue(final CachedResult _rs, final List<Integer> _indexes) throws Exception
    {
        Type ret;
        if (getAttribute().getSqlColNames().size() > 0) {
            ret = Type.get(_rs.getLong(_indexes.get(0).intValue()));
        } else {
            ret = getAttribute().getParent();
        }
        this.value = ret;
        return ret;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return DateTime
     * TODO throw error if more than one value is given
     */
    public Object readValue(final List<Object> _objectList)
    {
        Type ret;
        if (getAttribute().getSqlColNames().size() > 0) {
            ret = Type.get((Long) _objectList.get(0));
        } else {
            ret = getAttribute().getParent();
        }
        this.value = ret;
        return ret;
    }

    public void set(final Object[] _value)
    {
    }

    @Override
    public String toString()
    {
        return "" + this.value;
    }
}

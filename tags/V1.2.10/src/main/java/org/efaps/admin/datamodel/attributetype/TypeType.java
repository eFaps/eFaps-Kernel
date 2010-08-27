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
import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.CachedResult;
import org.efaps.db.wrapper.SQLUpdate;

/**
 * Implements the mapping between values in the database and {@link Type}
 * values in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TypeType
    extends AbstractType
{
    /**
     * A {@link SQLException} is always thrown because an update of a type is
     * not allowed.
     *
     * @param _update       update SQL statement
     * @param _attribute    related eFaps attribute; ignored, because update is
     *                      not allowed
     * @param _values       values ignored, because update not allowed
     * @throws SQLException always, because update is not allowed
     */
    @Override()
    public void prepareUpdate(final SQLUpdate _update,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        throw new SQLException("Update value for Type not allowed!!!");
    }

    /**
     * The method reads from a SQL result set the value for the type. If no
     * type SQL column is given in the type description, the value is read
     * directly from the attribute.
     */
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
        throws Exception
    {
        Type ret;
        if (!_attribute.getSqlColNames().isEmpty()) {
            ret = Type.get(_rs.getLong(_indexes.get(0).intValue()));
        } else {
            ret = _attribute.getParent();
        }
        return ret;
    }

    /**
     * The method reads from a SQL result set the value for the type. If no
     * type SQL column is given in the type description, the value is read
     * directly from the attribute.
     *
     * @param _attribute    related eFaps attribute
     * @param _objectList   list of objects
     * @return evaluated type instance
     * TODO throw error if more than one value is given
     */
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
    {
        Type ret;
        if (!_attribute.getSqlColNames().isEmpty()) {
            ret = Type.get((Long) _objectList.get(0));
        } else {
            ret = _attribute.getParent();
        }
        return ret;
    }
}
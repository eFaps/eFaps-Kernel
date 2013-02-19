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
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.wrapper.SQLUpdate;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class OIDType
    extends StringType
{

    /**
     * An update of an OID is not allowed and therefore a {@link SQLException}
     * is always thrown.
     *
     * @param _update       update SQL statement; ignored
     * @param _attribute    related eFaps attribute; ignored because update is
     *                      not allowed for the eFaps object id
     * @param _values       ignored
     * @throws SQLException always because update is not allowed
     */
    @Override
    public void prepareUpdate(final SQLUpdate _update,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        throw new SQLException("Update value for OID not allowed!!!");
    }

    /**
     * The oid (object id) is the type id, than a point and the id itself. If in
     * the attribute the attribute has no defined type id SQL column name, the
     * type from the attribute is used (this means, the type itself is not
     * derived and has no childs).
     *
     * @param _attribute    related attribute which is read
     * @param _objectList   list of objects from the eFaps Database
     * @return Object as needed for eFaps
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
    {
        final List<String> ret = new ArrayList<String>();
        for (final Object object : _objectList) {
            final StringBuilder oid = new StringBuilder();
            if (object instanceof Object[]) {
                final Object[] temp = (Object[]) object;
                oid.append(temp[0]).append(".").append(temp[1]);
            } else {
                oid.append(_attribute.getParent().getId()).append(".").append(object);
            }
            ret.add(oid.toString());
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }
}

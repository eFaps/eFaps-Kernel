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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.cache.CacheReloadException;

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
     * {@inheritDoc}
     */
    @Override
    public void prepareUpdate(final SQLUpdate _update,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        throw new SQLException("Update value for Type not allowed!!!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws CacheReloadException
    {
        Object ret;
        if (!_attribute.getSqlColNames().isEmpty()) {
            if (_objectList.size() < 1) {
                ret = null;
            } else {
                final Object object = _objectList.get(0);
                Object temp = null;
                // Oracle database stores all IDs as Decimal
                if (object instanceof BigDecimal) {
                    temp = ((BigDecimal) object).longValue();
                } else {
                    temp = object;
                }
                ret = Type.get((Long) temp);
            }
        } else {
            ret = _attribute.getParent();
        }
        return ret;
    }
}

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
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractLinkType
    extends AbstractType
{

    /**
     * Evaluate the value.
     * @param _value value to be evaluated
     * @return Long value
     */
    protected Long eval(final Object[] _value)
    {
        final Long ret;
        if (_value == null) {
            ret = null;
        } else  if ((_value[0] instanceof String) && (((String) _value[0]).length() > 0)) {
            ret = Long.parseLong((String) _value[0]);
        } else if (_value[0] instanceof Long) {
            ret = (Long) _value[0];
        } else if (_value[0] instanceof Integer) {
            ret = ((Integer) _value[0]).longValue();
        } else  {
            ret = null;
        }
        return ret;
    }

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
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
    {
        return _rs.getLong(_indexes.get(0));
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
        if (_objectList.size() > 0) {
            if (_objectList.size() > 1) {
                final List<Object> list = new ArrayList<Object>();
                ret = list;
                list.addAll(_objectList);
            } else {
                ret = _objectList.get(0);
            }
        }
        return ret;
    }
}
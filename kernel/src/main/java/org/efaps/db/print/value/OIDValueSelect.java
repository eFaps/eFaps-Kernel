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

package org.efaps.db.print.value;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class OIDValueSelect extends AbstractValueSelect
{

    /**
     * Type belonging to this OIDValueSelect.
     */
    private Type type;

    /**
     * {@inheritDoc}
     */
    @Override
    public int append2SQLSelect(final Type _type, final StringBuilder _fromBldr, final int _tableIndex,
                    final int _colIndex)
    {
        int ret = 0;
        if (getParent() == null || !"type".equals(getParent().getValueType())) {
            this.type = _type;
            _fromBldr.append(",T").append(_tableIndex).append(".ID");
            getColIndexs().add(_colIndex);
            ret++;
        }
        // in case that the type has a column for type it must be added
        if (this.type != null) {
            if (this.type.getMainTable().getSqlColType() != null) {
                _fromBldr.append(",T").append(_tableIndex).append(".").append(this.type.getMainTable().getSqlColType());
                getColIndexs().add(_colIndex + ret);
                ret++;
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _object) throws EFapsException
    {
        final StringBuilder bldr = new StringBuilder();
        boolean retNull = false;
        if (_object instanceof Object[]) {
            final Object[] object = (Object[]) _object;
            if (object[0] == null || object[1] == null) {
                retNull = true;
            } else {
                bldr.append(object[1]).append(".").append(object[0]);
            }
        } else {
            if (_object == null) {
                retNull = true;
            } else {
                bldr.append(this.type.getId()).append(".").append(_object);
            }
        }
        return retNull ? null : bldr.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final List<Object> _objectList) throws EFapsException
    {
        final List<Object> ret = new ArrayList<Object>();
        for (final Object object : _objectList) {
            ret.add(getValue(object));
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "oid";
    }
}

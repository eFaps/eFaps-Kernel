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

package org.efaps.db.print.value;

import java.math.BigDecimal;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.db.print.OneSelect;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TypeValueSelect
    extends AbstractValueSelect
{
    /**
     * Type this TypeValueSelect belongs to.
     */
    private Type type;

    /**
     * @param _oneSelect OneSelect
     */
    public TypeValueSelect(final OneSelect _oneSelect)
    {
        super(_oneSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int append2SQLSelect(final Type _type,
                                final SQLSelect _select,
                                final int _tableIndex,
                                final int _colIndex)
    {
        int ret = 0;
        if (_type.getMainTable().getSqlColType() != null) {
            _select.column(_tableIndex, _type.getMainTable().getSqlColType());
            getColIndexs().add(_colIndex + ret);
            ret++;
        }
        this.type = _type;
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _currentObject)
        throws EFapsException
    {
        Object ret = null;
        Type tempType;
        if (this.type.getMainTable().getSqlColType() == null) {
            tempType = this.type;
        } else if (_currentObject != null) {
            // check is necessary because Oracle JDBC returns for getObject always a BigDecimal
            if (_currentObject instanceof BigDecimal) {
                tempType = Type.get(((BigDecimal) _currentObject).longValue());
            } else {
                tempType = Type.get((Long) _currentObject);
            }
        } else {
            tempType = null;
        }
        if (tempType != null && getChildValueSelect() != null) {
            if ("label".equals(getChildValueSelect().getValueType())) {
                ret = tempType.getLabel();
            } else if ("oid".equals(getChildValueSelect().getValueType())) {
                ret = new StringBuilder().append(CIAdminDataModel.Type.getType().getId()).append(".").append(
                                tempType.getId()).toString();
            } else if ("UUID".equals(getChildValueSelect().getValueType())) {
                ret = tempType.getUUID();
            } else if ("Name".equals(getChildValueSelect().getValueType())) {
                ret = tempType.getName();
            }
        } else {
            ret = tempType;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "type";
    }
}

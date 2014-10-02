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

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.print.OneSelect;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StatusValueSelect
    extends AbstractValueSelect
{
    /**
     * @param _oneSelect OneSelect
     */
    public StatusValueSelect(final OneSelect _oneSelect)
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
        Type tmpType = _type;
        // not all types inside a hierarchy have the typattribute assigned so it must be searched for
        while (!tmpType.isCheckStatus() && tmpType.getParentType() != null) {
            tmpType = tmpType.getParentType();
        }
        if (tmpType.isCheckStatus()) {
            for (final String colName : tmpType.getStatusAttribute().getSqlColNames()) {
                _select.column(_tableIndex, colName);
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
    public Object getValue(final Object _currentObject)
        throws EFapsException
    {
        Object ret = null;
        Status tempStatus;
        if (_currentObject != null) {
            // check is necessary because Oracle JDBC returns for getObject always a BigDecimal
            if (_currentObject instanceof BigDecimal) {
                tempStatus = Status.get(((BigDecimal) _currentObject).longValue());
            } else {
                tempStatus = Status.get((Long) _currentObject);
            }
        } else {
            tempStatus = null;
        }
        if (tempStatus != null && getChildValueSelect() != null) {
            switch (getChildValueSelect().getValueType()) {
                case "label":
                    ret = tempStatus.getLabel();
                    break;
                case "oid":
                    ret = new StringBuilder().append(Type.get(tempStatus.getStatusGroup().getUUID()).getId())
                        .append(".").append(tempStatus.getId()).toString();
                    break;
                case "key":
                    ret = tempStatus.getKey();
                    break;
                case "type":
                    ret = ((TypeValueSelect) getChildValueSelect()).analyzeChildValue(getChildValueSelect(),
                                    Type.get(tempStatus.getStatusGroup().getUUID()));
                    break;
                default:
                    ret = tempStatus;
                    break;
            }
        } else {
            ret = tempStatus;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "status";
    }
}

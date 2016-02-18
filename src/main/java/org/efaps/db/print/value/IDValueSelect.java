/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.db.print.value;

import java.math.BigDecimal;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.print.OneSelect;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * Class is used to select an id.
 * Can be used directly or in conjunction with {@link TypeValueSelect}
 * or Status.
 *
 * @author The eFaps Team
 *
 */
public class IDValueSelect
    extends AbstractValueSelect
{
    /**
     * Attribute belonging to this IDValueSelect.
     */
    private Attribute attribute;

    /**
     * @param _oneSelect OneSelect
     */
    public IDValueSelect(final OneSelect _oneSelect)
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
        if (getParent() == null || !"type".equals(getParent().getValueType())) {
            _select.column(_tableIndex, "ID");
            getColIndexs().add(_colIndex);
            ret++;
            this.attribute = _type.getAttribute("ID");
        }
        return ret;
    }

    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        Long tempId;
        // check is necessary because Oracle JDBC returns for getObject always a BigDecimal
        if (_object instanceof BigDecimal) {
            tempId = ((BigDecimal) _object).longValue();
        } else {
            tempId = (Long) _object;
        }
        return tempId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "id";
    }
}

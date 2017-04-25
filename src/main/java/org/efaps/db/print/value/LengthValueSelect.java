/*
 * Copyright 2003 - 2017 The eFaps Team
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

import org.efaps.admin.datamodel.Type;
import org.efaps.db.print.OneSelect;
import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * ValueSelct to select the length value from a store instance.
 *
 * @author The eFaps Team
 *
 */
public class LengthValueSelect
    extends AbstractValueSelect
{
    /**
     * Constructor setting the OneSelect this LengthValueSelect belongs to.
     *
     * @param _oneSelect OneSelect
     */
    public LengthValueSelect(final OneSelect _oneSelect)
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
        _select.column(_tableIndex, AbstractStoreResource.COLNAME_FILELENGTH);
        getColIndexs().add(_colIndex);
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "length";
    }

    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        final Object tempId;
        // check is necessary because Oracle JDBC returns for getObject always a BigDecimal
        if (_object instanceof BigDecimal) {
            tempId = ((BigDecimal) _object).longValue();
        } else {
            tempId = _object;
        }
        return tempId;
    }
}

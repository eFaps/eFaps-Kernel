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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;

/**
 * Class is used to select an id.
 * Can be used directly or in conjunction with {@link TypeValueSelect}
 * or Status.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class IDValueSelect extends AbstractValueSelect
{
    /**
     * Attribute belonging to this IDValueSelect.
     */
    private Attribute attribute;

    /**
     * {@inheritDoc}
     */
    @Override
    public int append2SQLSelect(final Type _type, final StringBuilder _fromBldr, final int _tableIndex,
                                final int _colIndex)
    {
        int ret = 0;
        if (getParent() == null || !"type".equals(getParent().getValueType())) {
            _fromBldr.append(",T").append(_tableIndex).append(".ID");
            getColIndexs().add(_colIndex);
            ret++;
            this.attribute = _type.getAttribute("ID");
        }
        return ret;
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

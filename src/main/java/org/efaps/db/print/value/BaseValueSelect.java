/*
 * Copyright 2003 - 2011 The eFaps Team
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
import org.efaps.admin.datamodel.attributetype.AbstractWithUoMType;
import org.efaps.db.print.OneSelect;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class BaseValueSelect
    extends AbstractValueSelect
    implements IAttributeChildValueSelect
{

    /**
     * @param _oneSelect OneSelect
     */
    public BaseValueSelect(final OneSelect _oneSelect)
    {
        super(_oneSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "base";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final Attribute _attribute,
                      final Object _object)
        throws EFapsException
    {
        final Object ret;
        if (_attribute.getAttributeType().getDbAttrType() instanceof AbstractWithUoMType) {
            ret = getValueUOM(_object);
        } else {
            ret = _object;
        }
        return ret;
    }

    /**
     * @param _object object the value is wanted for
     * @return Object
     */
    protected Object getValueUOM(final Object _object)
    {
        Object ret = null;
        if (_object instanceof Object[]) {
            final Object[] values = (Object[]) _object;
            if (values.length == 3) {
                ret = values[2];
            } else {
                ret = values[0];
            }
        }
        return ret;
    }
}

/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.print.value;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.IFormattableType;
import org.efaps.db.print.OneSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class FormatValueSelect
    extends AbstractValueSelect
    implements IAttributeChildValueSelect
{

    /**
     * Pattern the formatter will use.
     */
    private final String pattern;

    /**
     * @param _oneSelect OneSelect
     * @param _pattern pattern the formatter will use
     */
    public FormatValueSelect(final OneSelect _oneSelect,
                             final String _pattern)
    {
        super(_oneSelect);
        this.pattern = _pattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "format";
    }

    /**
     * Method to format a given object.
     *
     * @param _attribute Attribute the object belongs to
     * @param _object Object to be formated
     * @return formated object
     * @throws EFapsException on error
     */
    @Override
    public Object get(final Attribute _attribute,
                      final Object _object)
        throws EFapsException
    {
        Object ret = null;
        if (_object != null && _attribute.getAttributeType().getDbAttrType() instanceof IFormattableType) {
            final IFormattableType attrType = (IFormattableType) _attribute.getAttributeType().getDbAttrType();
            if (_object instanceof List<?>) {
                final List<?> objectList = (List<?>) _object;
                final List<Object> temp = new ArrayList<>();
                for (final Object object : objectList) {
                    temp.add(attrType.format(object, this.pattern));
                }
                ret = temp;
            } else {
                ret = attrType.format(_object, this.pattern);
            }
        }
        return ret;
    }
}

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

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;


/**
 * Abstract class for an attribute type with an UoM.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractWithUoMType
    extends AbstractType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
    {
        final List<Object[]> ret = new ArrayList<Object[]>();
        for (final Object object : _objectList) {
            final Object[] temp = (Object[]) object;
            final Object value = readValue(temp[0]);
            final UoM uom = Dimension.getUoM((Long) temp[1]);
            if (temp.length > 2) {
                final Object obj = temp[2];
                double dbl = 0;
                if (obj instanceof Number) {
                    dbl = ((Number) obj).doubleValue();
                } else if (obj != null) {
                    dbl = Double.parseDouble(obj.toString());
                }
                ret.add(new Object[]{value, uom, dbl});
            } else {
                ret.add(new Object[]{value, uom});
            }

        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * Method to read the value from the cached result.
     * @see #readValue(CachedResult, List)
     *
     * @param _object   Object to read
     * @return value as object
     */
    protected abstract Object readValue(final Object _object);
}

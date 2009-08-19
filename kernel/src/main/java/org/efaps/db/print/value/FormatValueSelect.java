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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.attributetype.DecimalWithUoMType;
import org.efaps.admin.datamodel.attributetype.LongType;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

import com.sun.jdi.IntegerType;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FormatValueSelect extends AbstractValueSelect
{

    /**
     * Pattern the formatter will use.
     */
    private final String pattern;

    /**
     * @param _pattern pattern the formatter will use
     */
    public FormatValueSelect(final String _pattern)
    {
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
     * @param _attribute    Attribute the object belongs to
     * @param _object       Object to be formated
     * @return  formated object
     * @throws EFapsException on error
     */
    public Object format(final Attribute _attribute, final Object _object) throws EFapsException
    {
        Object ret = null;

        final IAttributeType attrType = _attribute.newInstance();
        DecimalFormat formatter = null;
        if (attrType instanceof DecimalType || attrType instanceof DecimalWithUoMType) {
            formatter = (DecimalFormat) DecimalFormat.getInstance(Context.getThreadContext().getLocale());
        } else if (attrType instanceof IntegerType || attrType instanceof LongType) {
            formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext().getLocale());
        }

        if (formatter != null) {
            formatter.applyPattern(this.pattern);
            if (_object instanceof List<?>) {
                final List<Object> temp = new ArrayList<Object>();
                final List<?> objectList = (List<?>) _object;
                for (final Object object : objectList) {
                    temp.add(formatter.format(object));
                }
                ret = temp;
            } else {
                ret = formatter.format(_object);
            }
        }
        return ret;
    }

}

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

package org.efaps.admin.datamodel.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class NumberWithUoMUI
    extends StringWithUoMUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final String _value,
                                final Attribute _attribute)
    {
        String ret = null;
        try {
            @SuppressWarnings("unused")
            final Number test = new Long(_value);
        } catch (final NumberFormatException e) {
            ret = DBProperties.getProperty(NumberWithUoMUI.class.getName() + ".InvalidValue");
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object format(final Object _object,
                         final String _pattern)
        throws EFapsException
    {
        final Object ret;
        final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                        .getLocale());
        formatter.applyPattern(_pattern);
        if (_object instanceof Object[]) {
            final String tmp = formatter.format(((Object[]) _object)[0]);
            ((Object[]) _object)[0] = tmp;
            ret = _object;
        } else {
            ret = formatter.format(_object);
        }
        return ret;
    }
}

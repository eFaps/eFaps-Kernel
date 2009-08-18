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

package org.efaps.admin.datamodel.ui;

import java.math.BigDecimal;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;


/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class DecimalWithUoMUI extends StringWithUoMUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final String _value, final Attribute _attribute)
    {
        String ret = null;
        try {
            @SuppressWarnings("unused")
            final BigDecimal test = new BigDecimal(_value);
        } catch (final NumberFormatException e) {
            ret = DBProperties.getProperty(DecimalWithUoMUI.class.getName() + ".InvalidValue");
        }
        return ret;
    }
}

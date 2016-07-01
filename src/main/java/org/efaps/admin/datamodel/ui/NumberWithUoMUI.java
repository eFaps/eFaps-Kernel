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

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.dbproperty.DBProperties;

/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 *
 *
 */
public class NumberWithUoMUI
    extends AbstractWithUoMProvider
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final UIValue _value)
    {
        String ret = null;
        try {
            if (_value.getDbValue() != null) {
                Long.valueOf(String.valueOf(_value.getDbValue()));
            }
        } catch (final NumberFormatException e) {
            ret = DBProperties.getProperty(NumberWithUoMUI.class.getName() + ".InvalidValue");
        }
        return ret;
    }
}

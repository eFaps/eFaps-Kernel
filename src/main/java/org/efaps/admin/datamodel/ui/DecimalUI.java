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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Class used to represent any type of decimal for the UI.
 *
 * @author The eFaps Team
 *
 */
public class DecimalUI
    extends AbstractProvider
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
                DecimalType.parseLocalized(String.valueOf(_value.getDbValue()));
            }
        } catch (final EFapsException e) {
            ret = DBProperties.getProperty(DecimalUI.class.getName() + ".InvalidValue");
        }
        return ret;
    }

    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        return _uiValue.getDbValue();
    }

    @Override
    public String getStringValue(final IUIValue _uiValue)
        throws EFapsException
    {
        final String ret;
        if (_uiValue.getObject() == null) {
            ret = null;
        } else if (_uiValue.getObject() instanceof BigDecimal) {
            final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            ret = formatter.format(_uiValue.getObject());
        } else {
            ret = String.valueOf(_uiValue.getObject());
        }
        return ret;
    }
}

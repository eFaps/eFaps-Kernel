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

import org.efaps.util.EFapsException;

/**
 * This class returns the different Html-Snipplets wich are needed for a
 * PasswordType.
 *
 * @author The eFaps Team
 *
 */
public class PasswordUI
    extends AbstractProvider
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        return _uiValue.getDbValue();
    }

    @Override
    public String validateValue(final UIValue _uiValue)
        throws EFapsException
    {
        return null;
    }

    @Override
    public Object transformObject(final UIValue _uiValue,
                                  final Object _object)
        throws EFapsException
    {
        return null;
    }
}

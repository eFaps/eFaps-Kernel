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

import java.io.Serializable;

import org.efaps.util.EFapsException;

/**
 * Abstract class for the UIInterface interface implementing for all required
 * methods a default.
 *
 * @author The eFaps Team
 *
 */
public abstract class AbstractUI
    implements IUIProvider, UIInterface, Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEditHtml(final FieldValue _fieldValuee)
        throws EFapsException
    {
        return "edit";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHiddenHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        return "hidden";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        return "read only";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final FieldValue _fieldValue)
        throws EFapsException
    {
        return getReadOnlyHtml(_fieldValue);
    }

    @Override
    public String getStringValue(final IUIValue _uiValue)
        throws EFapsException
    {
        final FieldValue fieldValue = new FieldValue(_uiValue.getField(), _uiValue.getAttribute(), _uiValue.getObject(),
                        _uiValue.getInstance(), _uiValue.getCallInstance());
        return getStringValue(fieldValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * @throws EFapsException
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
        throws EFapsException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final UIValue _value)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final UIValue _uiValue)
        throws EFapsException
    {
        return _uiValue.getDbValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object transformObject(final UIValue _uiValue,
                                  final Object _object)
        throws EFapsException
    {
        if (_object instanceof Serializable) {
            _uiValue.setDbValue((Serializable) _object);
        }
        return _object;
    }
}

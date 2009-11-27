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

import java.io.Serializable;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.util.EFapsException;

/**
 * Abstract class for the UIInterface interface implementing for all required
 * methods a default.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUI implements UIInterface, Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public String getEditHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return "edit";
    }

    /**
     * {@inheritDoc}
     */
    public String getHiddenHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return "hidden";
    }

    /**
     * {@inheritDoc}
     */
    public String getReadOnlyHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return "read only";
    }

    /**
     * {@inheritDoc}
     */
    public String getStringValue(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return getReadOnlyHtml(_fieldValue, _mode);
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject4Compare(final FieldValue _fieldValue) throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2)
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public String validateValue(final String _value, final Attribute _attribute)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object format(final Object _object, final String _pattern) throws EFapsException
    {
        return _object;
    }
}

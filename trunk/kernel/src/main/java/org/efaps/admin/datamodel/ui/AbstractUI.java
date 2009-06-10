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
     * Method to get the Value for editing in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode the target mode
     * @return "edit"
     * @throws EFapsException on error
     */
    public String getEditHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return "edit";
    }

    /**
     * Method to get the Value for hidden field in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode the target mode
     * @return "view"
     * @throws EFapsException on error
     */
    public String getHiddenHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return "hidden";
    }

    /**
     * Method to get the Value for a read only field in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode the target mode
     * @return "view"
     * @throws EFapsException on error
     */
    public String getReadOnlyHtml(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return "read only";
    }


    /**
     * @see org.efaps.admin.datamodel.ui.UIInterface#getStringValue(org.efaps.admin.datamodel.ui.FieldValue, org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode)
     * @param _fieldValue Fieldvalue the representation is requested
     * @param _mode the target mode
     * @return "String-Value"
     * @throws EFapsException on error
     */
    public String getStringValue(final FieldValue _fieldValue, final TargetMode _mode) throws EFapsException
    {
        return getReadOnlyHtml(_fieldValue, _mode);
    }

    /**
     * Method to get the Object for use in case of comparison.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @return null
     * @throws EFapsException on error
     */
    public Object getObject4Compare(final FieldValue _fieldValue) throws EFapsException
    {
        return null;
    }

    /**
     * Method to compare the values.
     *
     * @param _fieldValue first Value
     * @param _fieldValue2 second Value
     * @return 0
     */
    public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2)
    {
        return 0;
    }
}

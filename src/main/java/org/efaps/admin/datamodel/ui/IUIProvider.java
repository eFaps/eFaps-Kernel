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
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public interface IUIProvider
{

    /**
     * Get the Value for the UserInterface.
     * @param _uiValue  the UIValue the value for the UserInterface is wanted for
     * @return Object   for the UserInterface
     * @throws EFapsException on any error
     */
    Object getValue(final UIValue _uiValue)
        throws EFapsException;

    /**
     * Method is used to validate a value given from an UserInterface.
     *
     * @param _uiValue UIValue to validate
     * @return if the given value is valid for this type null must be returned,
     *         else the message that will be shown to the user as a snipplet
     *         must be returned
     * @throws EFapsException on error
     */
    String validateValue(final UIValue _uiValue)
        throws EFapsException;

    /**
     * Method is used to transform a object retrieved from an FieldValue Event into
     * the form specified by the UIProvider.
     *
     * @param _uiValue UIValue to transform
     * @param _object the object to transform
     * @return the transformed value
     * @throws EFapsException on error
     */
    Object transformObject(final UIValue _uiValue,
                           final Object _object)
        throws EFapsException;

    /**
     * Method to get a String representation of the value. This is used e.g. for
     * labels and Phrases.
     *
     * @param _uiValue the user value
     * @return String representation of the object for viewing
     * @throws EFapsException on error
     */
    String getStringValue(final IUIValue _uiValue)
        throws EFapsException;

}

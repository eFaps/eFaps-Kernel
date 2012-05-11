/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.util.EFapsException;

/**
 * Interface for all classes used to evaluate value for presentation in an user
 * interface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface UIInterface
{
    /**
     * Used as a temporary tag inside html. It is used to set e.g. the id or add
     * javascript during the rendering;
     */
    String EFAPSTMPTAG = " eFapsTempTag ";

    /**
     * Method to get the Value for creation in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @return String representation of the object for creation
     * @throws EFapsException on error
     */
    String getHiddenHtml(final FieldValue _fieldValue)
        throws EFapsException;

    /**
     * Method to get the Value for editing in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @return String representation of the object for editing
     * @throws EFapsException on error
     */
    String getEditHtml(final FieldValue _fieldValue)
        throws EFapsException;

    /**
     * Method to get the Value for viewing in an html document.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @return String representation of the object for viewing
     * @throws EFapsException on error
     */
    String getReadOnlyHtml(final FieldValue _fieldValue)
        throws EFapsException;

    /**
     * Method to get a String representation of the value. This is used e.g. for
     * labels. In comparison to the Html-Methods it should not contain html.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @return String representation of the object for viewing
     * @throws EFapsException on error
     */
    String getStringValue(final FieldValue _fieldValue)
        throws EFapsException;

    /**
     * Method to get the Object for use in case of comparison.
     *
     * @param _fieldValue Fieldvalue the representation is requested
     * @return Object for comparison
     * @throws EFapsException on error
     */
    Object getObject4Compare(final FieldValue _fieldValue)
        throws EFapsException;

    /**
     * Method to compare the values.
     *
     * @param _fieldValue first Value
     * @param _fieldValue2 second Value
     * @return int
     * @throws EFapsException on error
     */
    int compare(final FieldValue _fieldValue,
                final FieldValue _fieldValue2)
        throws EFapsException;

    /**
     * Method is used to validate a string value given from an Userinterface.
     *
     * @param _value value to validate
     * @param _attribute Attribute the value is related to
     * @return if the given value is valid for this type null must be returned,
     *         else the message that will be shown to the user as a snipplet
     *         must be returned
     * @throws EFapsException on error
     */
    String validateValue(final String _value,
                         final Attribute _attribute)
        throws EFapsException;;

    /**
     * Method to format the given Object.
     *
     * @param _object the object to be formated
     * @param _pattern pattern to be used by the formatter
     * @return formated object
     * @throws EFapsException on error
     */
    Object format(final Object _object,
                  final String _pattern)
        throws EFapsException;
}

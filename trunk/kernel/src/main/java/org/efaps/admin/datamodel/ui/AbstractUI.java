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

import org.efaps.util.EFapsException;

/**
 * Abstract class for the UIInterface interface implementing for all
 * required methods a default.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractUI implements UIInterface, Serializable {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Method to get the Value for creation in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return "create"
   * @throws EFapsException on error
   */
  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    return "create";
  }

  /**
   * Method to get the Value for editing in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return "edit"
   * @throws EFapsException on error
   */
  public String getEditHtml(final FieldValue _fieldValue)
      throws EFapsException {
    return "edit";
  }

  /**
   * Method to get the Value for search in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return "search"
   * @throws EFapsException on error
   */
  public String getSearchHtml(final FieldValue _fieldValue)
      throws EFapsException {
    return "search";
  }

  /**
   * Method to get the Value for viewing in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return "view"
   * @throws EFapsException on error
   */
  public String getViewHtml(final FieldValue _fieldValue)
      throws EFapsException {
    return "view";
  }

  /**
   * Method to get the Object for use in case of comparison.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return null
   * @throws EFapsException on error
   */
  public Object getObject4Compare(final FieldValue _fieldValue)
      throws EFapsException {
    return null;
  }

  /**
   * Method to compare the values.
   *
   * @param _fieldValue first Value
   * @param _fieldValue2 second Value
   * @return 0
   */
  public int compare(final FieldValue _fieldValue,
                     final FieldValue _fieldValue2) {
    return 0;
  }
}

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

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.util.EFapsException;

/**
 * Class to represent a Date for the user interface.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class TypeUI extends AbstractUI {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Method to get the Value for view in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return "create"
   * @throws EFapsException on error
   */
  @Override
  public String getViewHtml(final FieldValue _fieldValue)
      throws EFapsException {
    String ret = null;

    if (_fieldValue.getValue() instanceof Type) {
      final Type value = ((Type) _fieldValue.getValue());

      final String name = value.getName();

      ret = DBProperties.getProperty(name + ".Label");

    } else {
      throw new EFapsException(this.getClass(),
                               "getViewHtml.noType",
                               (Object[]) null);
    }
    return ret;
  }

  /**
   * Method to compare the values.
   *
   * @param _fieldValue first Value
   * @param _fieldValue2 second Value
   * @return 0
   */
  @Override
  public int compare(final FieldValue _fieldValue,
                     final FieldValue _fieldValue2) {
    String value = null;
    String value2 = null;
    if (_fieldValue.getValue() instanceof Type) {
      final Type type = ((Type) _fieldValue.getValue());
      value = DBProperties.getProperty(type.getName() + ".Label");
    }
    if (_fieldValue2.getValue() instanceof Type) {
      final Type type = ((Type) _fieldValue2.getValue());
      value2 = DBProperties.getProperty(type.getName() + ".Label");
    }

    return value.compareTo(value2);
  }
}

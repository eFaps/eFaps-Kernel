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

import org.efaps.util.EFapsException;

/**
 * Class used to represent any type of number for the UI.
 *
 * @author jmox
 * @version $Id$
 */
public class NumberUI extends StringUI {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Method to get the Object for use in case of comparison.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return value
   * @throws EFapsException on error
   */
  @Override
  public Object getObject4Compare(final FieldValue _fieldValue)
      throws EFapsException {
    return _fieldValue.getValue();
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
    int ret = 0;
    if (_fieldValue.getValue() instanceof Long
        && _fieldValue2.getValue() instanceof Long) {
      final Long num = (Long) _fieldValue.getValue();
      final Long num2 = (Long) _fieldValue2.getValue();
      ret = num.compareTo(num2);
    } else if (_fieldValue.getValue() instanceof Integer
        && _fieldValue2.getValue() instanceof Integer) {
      final Integer num = (Integer) _fieldValue.getValue();
      final Integer num2 = (Integer) _fieldValue2.getValue();
      ret = num.compareTo(num2);
     }
    return ret;
  }
}

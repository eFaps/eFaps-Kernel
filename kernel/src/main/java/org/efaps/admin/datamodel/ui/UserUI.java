/*
 * Copyright 2003-2007 The eFaps Team
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

import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @author jmo
 * @version $Id$
 */
public class UserUI extends AbstractUI {
  @Override
  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    String ret = null;
    Object value = _fieldValue.getValue();
    if (value instanceof Person) {
      Person person = (Person) value;
      ret = person.getViewableName(null);
    } else if (value instanceof Role) {
      Role role = (Role) value;
      ret = role.getViewableName(null);
    } else {
      // throw new EFapsException();
    }
    return ret;
  }

 
  @Override
  public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2) {
    String value = null;
    if (_fieldValue.getValue() instanceof Person) {
      Person person = (Person) _fieldValue.getValue();
      value = person.getViewableName(null);
    } else if (_fieldValue.getValue() instanceof Role) {
      Role role = (Role) _fieldValue.getValue();
      value = role.getViewableName(null);
    }
    String value2 = null;
    if (_fieldValue2.getValue() instanceof Person) {
      Person person = (Person) _fieldValue2.getValue();
      value = person.getViewableName(null);
    } else if (_fieldValue2.getValue() instanceof Role) {
      Role role = (Role) _fieldValue2.getValue();
      value = role.getViewableName(null);
    }

    return value.compareTo(value2);
  }
}

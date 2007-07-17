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
 * Revision:        $Rev: 961 $
 * Last Changed:    $Date: 2007-07-08 15:49:39 -0500 (Sun, 08 Jul 2007) $
 * Last Changed By: $Author: tmo $
 */

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.ui.Field;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class UserUI implements UIInterface {

  public String getViewHtml(final Object _value, final Field _field)
      throws EFapsException {
    String ret = null;

    if (_value instanceof Person) {
      Person person = (Person) _value;
      ret = person.getViewableName(null);
    } else if (_value instanceof Role) {
      Role role = (Role) _value;
      ret = role.getViewableName(null);
    } else {
      // throw new EFapsException();
    }
    return ret;
  }

  public String getEditHtml(final Object _value, final Field _field)
      throws EFapsException {
    return "edit";
  }

  public String getCreateHtml(final Object _value, final Field _field)
      throws EFapsException {

    StringBuilder ret = new StringBuilder();
    ret.append("<input type=\"hidden\" ").append("name=\"").append(
        _field.getName()).append("\" id=\"UserUI\">");

    ret.append("<iframe frameborder=\"0\" name=\"").append(_field.getName())
        .append("\" src=\"UserUI.jsp?\"></iframe>");
    return ret.toString();

  }

  public String getSearchHtml(final Object _value, final Field _field)
      throws EFapsException {
    return "search";
  }

  public int compareTo(final UIInterface _uiinterface) {

    return 0;
  }
}

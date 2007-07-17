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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Field;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class TypeUI implements UIInterface {

  public String getViewHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    String ret = null;

    if (_value instanceof Type) {
      Type value = ((Type) _value);

      String name = value.getName();

      ret = DBProperties.getProperty(name + ".Label");

    } else {
      // throw new EFapsException();
    }
    return ret;
  }

  public String getEditHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    return "edit";
  }

  public String getCreateHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    return "create";
  }

  public String getSearchHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    return "search";
  }

  public int compareTo(UIInterface _uiinterface, UIInterface __uiinterface2) {
    // TODO Auto-generated method stub
    return 0;
  }

}

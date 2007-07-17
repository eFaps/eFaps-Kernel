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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Field;
import org.efaps.util.EFapsException;

/**
 * A boolean value is shown in create mode with radio boxen which are not
 * preselected. In edit mode, the user could select a value.
 * 
 * @author jmo
 * @version $Id$
 * @todo description
 * @todo preseletect in create for default value
 */
public class BooleanUI implements UIInterface {

  public String getCreateHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    StringBuilder ret = new StringBuilder();
    ret.append("<input type=\"radio\" ").append("name=\"").append(
        _field.getName()).append("\" ").append("value=\"").append("TRUE")
        .append("\"/>").append(getTrue(_field)).append("<br/>").append(
            "<input type=\"radio\" ").append("name=\"")
        .append(_field.getName()).append("\" ").append("value=\"").append(
            "FALSE").append("\"/>").append(getFalse(_field));
    return ret.toString();

  }

  public String getEditHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    StringBuilder ret = new StringBuilder();

    if (_value instanceof Boolean) {
      boolean bool = (Boolean) _value;

      ret.append("<input type=\"radio\" ").append(
          bool ? "checked=\"checked\" " : "").append("name=\"").append(
          _field.getName()).append("\" ").append("value=\"").append("TRUE")
          .append("\"/>").append(getTrue(_field)).append("<br/>").append(
              "<input type=\"radio\" ").append(
              bool ? "" : "checked=\"checked\" ").append("name=\"").append(
              _field.getName()).append("\" ").append("value=\"")
          .append("FALSE").append("\"/>").append(getFalse(_field));
    }
    return ret.toString();
  }

  public String getSearchHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getViewHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    String ret = null;

    if (_value instanceof Boolean) {
      boolean bool = (Boolean) _value;
      if (bool) {
        ret = this.getTrue(_field);
      } else {
        ret = this.getFalse(_field);
      }
    }
    return ret;
  }

  private String getFalse(final Field _field) {
    String ret;

    if (DBProperties.hasProperty(_field.getLabel() + ".false")) {
      ret = DBProperties.getProperty(_field.getLabel() + ".false");
    } else {
      ret = "FALSE";
    }
    return ret;
  }

  private String getTrue(final Field _field) {
    String ret;
    if (DBProperties.hasProperty(_field.getLabel() + ".true")) {
      ret = DBProperties.getProperty(_field.getLabel() + ".true");
    } else {
      ret = "TRUE";
    }
    return ret;
  }

  public int compareTo(UIInterface _uiinterface, UIInterface __uiinterface2) {
    // TODO Auto-generated method stub
    return 0;
  }
}

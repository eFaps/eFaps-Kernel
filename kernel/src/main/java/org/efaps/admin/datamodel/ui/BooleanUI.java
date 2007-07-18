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
 * A boolean value is shown in create mode with radio boxen which are only
 * preselected if a defaultvalue for the attribute was defined. In edit mode,
 * the user could select a value.
 * 
 * @author jmo
 * @version $Id$
 * @todo description
 * @todo preseletect in create for default value
 */
public class BooleanUI extends AbstractUI {
  @Override
  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    StringBuilder ret = new StringBuilder();
    Boolean bool = null;
    Attribute attribute = _fieldValue.getAttribute();
    Field field = _fieldValue.getFieldDef().getField();
    if (attribute.getDefaultValue() != null
        && attribute.getDefaultValue().length() > 0) {
      if (attribute.getDefaultValue().equalsIgnoreCase("TRUE")) {
        bool = true;
      } else {
        bool = false;
      }
    }
    ret.append("<input type=\"radio\" ").append(
        (bool != null && bool) ? "checked=\"checked\" " : "").append("name=\"")
        .append(field.getName()).append("\" ");
    ret.append("value=\"").append("TRUE").append("\"/>");
    ret.append(getTrue(field)).append("<br/>");
    ret.append("<input type=\"radio\" ").append(
        (bool != null && !bool) ? "checked=\"checked\" " : "")
        .append("name=\"").append(field.getName()).append("\" ").append(
            "value=\"").append("FALSE").append("\"/>").append(getFalse(field));
    return ret.toString();

  }

  @Override
  public String getEditHtml(final FieldValue _fieldValue) throws EFapsException {
    StringBuilder ret = new StringBuilder();
    Field field = _fieldValue.getFieldDef().getField();
    if (_fieldValue.getValue() instanceof Boolean) {
      boolean bool = (Boolean) _fieldValue.getValue();

      ret.append("<input type=\"radio\" ").append(
          bool ? "checked=\"checked\" " : "").append("name=\"").append(
          field.getName()).append("\" ").append("value=\"").append("TRUE")
          .append("\"/>").append(getTrue(field)).append("<br/>").append(
              "<input type=\"radio\" ").append(
              bool ? "" : "checked=\"checked\" ").append("name=\"").append(
              field.getName()).append("\" ").append("value=\"").append("FALSE")
          .append("\"/>").append(getFalse(field));
    }
    return ret.toString();
  }

  @Override
  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    String ret = null;
    Field _field = _fieldValue.getFieldDef().getField();
    if (_fieldValue.getValue() instanceof Boolean) {
      boolean bool = (Boolean) _fieldValue.getValue();
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

  @Override
  public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2) {
    String value = null;
    String value2 = null;
    if (_fieldValue.getValue() instanceof Boolean) {
      boolean bool = (Boolean) _fieldValue.getValue();
      if (bool) {
        value = getTrue(_fieldValue.getFieldDef().getField());
      } else {
        value = getFalse(_fieldValue.getFieldDef().getField());
      }
    }
    if (_fieldValue2.getValue() instanceof Boolean) {
      boolean bool = (Boolean) _fieldValue2.getValue();
      if (bool) {
        value2 = getTrue(_fieldValue2.getFieldDef().getField());
      } else {
        value2 = getFalse(_fieldValue2.getFieldDef().getField());
      }
    }

    return value.compareTo(value2);
  }
}

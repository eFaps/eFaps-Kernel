/*
 * Copyright 2003-2008 The eFaps Team
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
import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * A boolean value is shown in create mode with radio boxen which are only
 * preselected if a defaultvalue for the attribute was defined. In edit mode,
 * the user could select a value. The value is recived from the DBProperties
 * using the AttributeName and a parameter.<br>
 * e.g. <br>
 * Key = TypeName/AttributeName.false <br>
 * Value = inactive
 *
 * @author jmox
 * @version $Id$
 */
public class BooleanUI extends AbstractUI {
  @Override
  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    final StringBuilder ret = new StringBuilder();
    Boolean bool = null;
    final Attribute attribute = _fieldValue.getAttribute();
    final Field field = _fieldValue.getFieldDef().getField();
    if (attribute.getDefaultValue() != null
        && attribute.getDefaultValue().length() > 0) {
      if (attribute.getDefaultValue().equalsIgnoreCase("TRUE")) {
        bool = true;
      } else {
        bool = false;
      }
    }
    ret.append("<input type=\"radio\" ")
       .append((bool != null && bool) ? "checked=\"checked\" " : "")
       .append("name=\"").append(field.getName()).append("\" ")
       .append("value=\"").append("TRUE").append("\"/>")
       .append(getTrue(attribute)).append("<br/>");

    ret.append("<input type=\"radio\" ")
       .append((bool != null && !bool) ? "checked=\"checked\" " : "")
       .append("name=\"").append(field.getName()).append("\" ")
       .append("value=\"").append("FALSE").append("\"/>")
       .append(getFalse(attribute));
    return ret.toString();

  }

  @Override
  public String getEditHtml(final FieldValue _fieldValue) throws EFapsException {
    final StringBuilder ret = new StringBuilder();
    final Field field = _fieldValue.getFieldDef().getField();
    final Attribute attribute = _fieldValue.getAttribute();
    if (_fieldValue.getValue() instanceof Boolean) {
      final boolean bool = (Boolean) _fieldValue.getValue();

      ret.append("<input type=\"radio\" ")
         .append(bool ? "checked=\"checked\" " : "")
         .append("name=\"").append(field.getName()).append("\" ")
         .append("value=\"").append("TRUE").append("\"/>")
         .append(getTrue(attribute)).append("<br/>");

      ret.append("<input type=\"radio\" ")
         .append(bool ? "" : "checked=\"checked\" ")
         .append("name=\"").append(field.getName()).append("\" ")
         .append("value=\"").append("FALSE").append("\"/>")
         .append(getFalse(attribute));
    }
    return ret.toString();
  }
  @Override
  public String getSearchHtml(final FieldValue _fieldValue) {
    final StringBuilder ret = new StringBuilder();
    final Field field = _fieldValue.getFieldDef().getField();
    final Attribute attribute = _fieldValue.getAttribute();

    ret.append("<input type=\"radio\" ")
       .append("name=\"").append(field.getName()).append("\" ")
       .append("value=\"").append("TRUE").append("\"/>")
       .append(getTrue(attribute)).append("<br/>");

    ret.append("<input type=\"radio\" ")
       .append("name=\"").append(field.getName()).append("\" ")
       .append("value=\"").append("FALSE").append("\"/>")
       .append(getFalse(attribute));

    return ret.toString();
  }



  @Override
  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    String ret = null;
    final Attribute attribute = _fieldValue.getAttribute();
    if (_fieldValue.getValue() instanceof Boolean) {
      final boolean bool = (Boolean) _fieldValue.getValue();
      if (bool) {
        ret = this.getTrue(attribute);
      } else {
        ret = this.getFalse(attribute);
      }
    }
    return ret;
  }

  private String getFalse(final Attribute _attribute) {
    String ret;

    if (DBProperties.hasProperty(_attribute.getParent().getName() + "/"
        + _attribute.getName() + ".false")) {
      ret =
          DBProperties.getProperty(_attribute.getParent().getName() + "/"
              + _attribute.getName() + ".false");
    } else {
      ret = "FALSE";
    }
    return ret;
  }

  private String getTrue(final Attribute _attribute) {
    String ret;
    if (DBProperties.hasProperty(_attribute.getParent().getName() + "/"
        + _attribute.getName() + ".true")) {
      ret =
          DBProperties.getProperty(_attribute.getParent().getName() + "/"
              + _attribute.getName() + ".true");
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
      final boolean bool = (Boolean) _fieldValue.getValue();
      if (bool) {
        value = getTrue(_fieldValue.getAttribute());
      } else {
        value = getFalse(_fieldValue.getAttribute());
      }
    }
    if (_fieldValue2.getValue() instanceof Boolean) {
      final boolean bool = (Boolean) _fieldValue2.getValue();
      if (bool) {
        value2 = getTrue(_fieldValue.getAttribute());
      } else {
        value2 = getFalse(_fieldValue.getAttribute());
      }
    }

    return value.compareTo(value2);
  }
}

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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Field;

/**
 * A boolean value is shown in create mode with radio boxen which are only
 * preselected if a defaultvalue for the attribute was defined. In edit mode,
 * the user could select a value. The value is received from the DBProperties
 * using the AttributeName and a parameter.<br>
 * e.g. <br>
 * Key = TypeName/AttributeName.false <br>
 * Value = inactive
 *
 * @author jmox
 * @version $Id$
 */
public class BooleanUI extends AbstractUI {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Method to get the Value for creation in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return html with two radio buttons
   *
   */
  @Override
  public String getCreateHtml(final FieldValue _fieldValue) {
    final StringBuilder ret = new StringBuilder();
    Boolean bool = null;
    final Attribute attribute = _fieldValue.getAttribute();
    final Field field = _fieldValue.getField();
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

  /**
   * Method to get the Value for editing in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return html with two radio buttons
   *
   */
  @Override
  public String getEditHtml(final FieldValue _fieldValue) {
    final StringBuilder ret = new StringBuilder();
    final Field field = _fieldValue.getField();
    final Attribute attribute = _fieldValue.getAttribute();

    final Boolean bool;
    if (_fieldValue.getValue() instanceof Boolean) {
      bool = (Boolean) _fieldValue.getValue();
    } else {
      bool = null;
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

  /**
   * Method to get the Value for search in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return html with two radio buttons
   *
   */
  @Override
  public String getSearchHtml(final FieldValue _fieldValue) {
    final StringBuilder ret = new StringBuilder();
    final Field field = _fieldValue.getField();
    final Attribute attribute = _fieldValue.getAttribute();
    final Object value = _fieldValue.getValue();

    Boolean bool = null;
    if (value != null && value instanceof String
        && ((String) value).length() > 0) {
      if (((String) value).equalsIgnoreCase("TRUE")) {
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

  /**
   * Method to get the Value for search in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return html with two radio buttons
   *
   */
  @Override
  public String getViewHtml(final FieldValue _fieldValue) {
    String ret = null;
    final Attribute attribute = _fieldValue.getAttribute();
    if (_fieldValue.getValue() instanceof Boolean) {
      final boolean bool = (Boolean) _fieldValue.getValue();
      if (bool) {
        ret = getTrue(attribute);
      } else {
        ret = getFalse(attribute);
      }
    }
    return ret;
  }

  /**
   * Method to get the Object for use in case of comparison.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return String representation of the value
   */
  @Override
  public Object getObject4Compare(final FieldValue _fieldValue) {
    Object ret = null;
    if (_fieldValue.getValue() instanceof Boolean) {
      final boolean bool = (Boolean) _fieldValue.getValue();
      if (bool) {
        ret = getTrue(_fieldValue.getAttribute());
      } else {
        ret = getFalse(_fieldValue.getAttribute());
      }
    }
    return ret;
  }

  /**
   * Method to compare the values.
   *
   * @param _fieldValue first Value
   * @param _fieldValue2 second Value
   * @return 0 or -1
   */
  @Override
  public int compare(final FieldValue _fieldValue,
                     final FieldValue _fieldValue2) {
    String value = null;
    String value2 = null;
    // in case we have a boolean
    if (_fieldValue.getValue() instanceof Boolean
        && _fieldValue2.getValue() instanceof Boolean) {
      if ((Boolean) _fieldValue.getValue()) {
        value = getTrue(_fieldValue.getAttribute());
      } else {
        value = getFalse(_fieldValue.getAttribute());
      }
      if ((Boolean) _fieldValue2.getValue()) {
        value2 = getTrue(_fieldValue.getAttribute());
      } else {
        value2 = getFalse(_fieldValue.getAttribute());
      }
    }
    // in case we have allready a string
    if (_fieldValue.getValue() instanceof String
        && _fieldValue2.getValue() instanceof String) {
      value = (String) _fieldValue.getValue();
      value2 = (String) _fieldValue2.getValue();
    }
    return value.compareTo(value2);
  }

  /**
   * Method to evaluate a String representation for the boolean.
   *
   * @param _attribute  Attribute the String representation is wanted for
   * @return String representation, default "FALSE"
   */
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

  /**
   * Method to evaluate a String representation for the boolean.
   *
   * @param _attribute  Attribute the String representation is wanted for
   * @return String representation, default "TRUE"
   */
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
}

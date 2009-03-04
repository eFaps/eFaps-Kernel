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

import java.util.List;

import org.efaps.admin.ui.field.Field;

/**
 * Class to represent a String for the user interface.
 *
 * @author tmo
 * @version $Id$
 *
 */
public class StringUI extends AbstractUI {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Method to get the Value for viewing in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return value for field
   *
   */
  @Override
  public String getViewHtml(final FieldValue _fieldValue) {
    final StringBuilder ret = new StringBuilder();

    if (_fieldValue.getValue() != null) {

      if (_fieldValue.getValue() instanceof List) {
        final List<?> values = (List<?>) _fieldValue.getValue();
        boolean first = true;
        for (final Object value : values) {
          final String tmp = value.toString();
          if (tmp != null) {
            if (first) {
              first = false;
            } else {
              ret.append("<br/>");
            }
            ret.append(tmp.replaceAll("\\n", "<br/>").replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;"));
          }
        }
      } else {
        final String tmp = _fieldValue.getValue().toString();
        if (tmp != null) {
          ret.append(tmp.replaceAll("\\n", "<br/>").replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;"));
        }
      }
    }
    return ret.toString();
  }

  /**
   * Method to get the Value for editing in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return value for field
   *
   */
  @Override
  public String getEditHtml(final FieldValue _fieldValue) {
    String ret;
    final Field field = _fieldValue.getField();
    final Object value = _fieldValue.getValue();
    if (field.getRows() > 1) {
      ret =
          "<textarea " + "type=\"text\" " + "cols=\"" + field.getCols() + "\" "
              + "rows=\"" + field.getRows() + "\" " + "name=\""
              + field.getName() + "\"" + ">";
      if (value != null) {
        ret += value;
      }
      ret += "</textarea>";
    } else {
      ret =
          "<input type=\"text\" " + "size=\"" + field.getCols() + "\" "
              + "name=\"" + field.getName() + "\" " + "value=\""
              + (value != null ? value : "") + "\"" + "/>";
    }
    return ret;
  }

  /**
   * Method to get the Value for create in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return value for field
   *
   */
  @Override
  public String getCreateHtml(final FieldValue _fieldValue) {
    final StringBuffer ret = new StringBuffer();
    final Field field = _fieldValue.getField();
    final Object value = _fieldValue.getValue();

    if (field.getRows() > 1) {
      ret.append("<textarea " + "type=\"text\" " + "cols=\"").append(
          field.getCols()).append("\" " + "rows=\"").append(field.getRows())
          .append("\" " + "name=\"").append(field.getName()).append("\" ");

      ret.append(">");
      if (value != null) {
        ret.append(value);
      }
      ret.append("</textarea>");
    } else {
      ret.append("<input type=\"text\" size=\"").append(field.getCols())
          .append("\" name=\"").append(field.getName())
          .append("\" value=\"").append((value != null ? value : ""))
          .append("\" ");

      ret.append(">");
    }
    return ret.toString();
  }

  /**
   * Method to get the Value for viewing in an html document.
   *
   * @param _fieldValue Fieldvalue the representation is requested
   * @return value for field
   *
   */
  @Override
  public String getSearchHtml(final FieldValue _fieldValue) {
    final StringBuilder ret = new StringBuilder();
    final Field field = _fieldValue.getField();
    final Object value = _fieldValue.getValue();
    ret.append("<input type=\"text\"")
      .append(" size=\"").append(field.getCols())
      .append("\" name=\"").append(field.getName())
      .append("\" value=\"").append((value != null ? value : "*"))
      .append("\" />");
    return ret.toString();
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
    final String value = _fieldValue.getValue().toString();
    final String value2 = _fieldValue2.getValue().toString();
    return value.compareTo(value2);
  }
}

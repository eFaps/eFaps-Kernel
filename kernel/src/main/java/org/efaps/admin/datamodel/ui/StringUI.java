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

import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class StringUI extends AbstractUI {
  @Override
  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    String ret = null;

    if (_fieldValue.getValue() != null) {
      ret = _fieldValue.getValue().toString();
      if (ret != null) {
        ret =
            ret.replaceAll("\\n", "<br/>").replaceAll("<", "&lt;").replaceAll(
                ">", "&gt;");
      } else {
        ret = "";
      }
    } else {
      // throw new EFapsException();
    }
    return ret;
  }

  @Override
  public String getEditHtml(final FieldValue _fieldValue) throws EFapsException {
    String ret;
    Field field = _fieldValue.getFieldDef().getField();
    Object value = _fieldValue.getValue();
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

  @Override
  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    StringBuffer ret = new StringBuffer();
    Field field = _fieldValue.getFieldDef().getField();
    Object value = _fieldValue.getValue();

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
      ret.append("<input type=\"text\" " + "size=\"").append(field.getCols())
          .append("\" " + "name=\"").append(field.getName()).append(
              "\" " + "value=\"").append((value != null ? value : "")).append(
              "\" ");

      ret.append(">");
    }
    return ret.toString();
  }

  @Override
  public String getSearchHtml(final FieldValue _fieldValue)
      throws EFapsException {
    Field field = _fieldValue.getFieldDef().getField();
    return "<input type=\"text\" " + "size=\"" + field.getCols() + "\" "
        + "name=\"" + field.getName() + "\" " + "value=\"*\"" + "/>";
  }

  @Override
  public int compare(final FieldValue _fieldValue, final FieldValue _fieldValue2) {
    String value = _fieldValue.getValue().toString();
    String value2 = _fieldValue2.getValue().toString();
    return value.compareTo(value2);
  }

}

/*
 * Copyright 2003 - 2007 The eFaps Team
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

import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class LinkWithRangesUI implements UIInterface {

  public String getViewHtml(Context _context, Object _value, Field _field)
                                                                          throws EFapsException {
    String ret = null;

    if (_value != null) {
      String value = _value.toString();
      ret = "<select name=\"top5\" size=\"1\">" + "<option>Heino</option>"
          + "<option>Michael Jackson</option>" + "<option>Tom Waits</option>"
          + "<option>Nina Hagen</option>"
          + "<option>Marianne Rosenberg</option>" + "</select>";

//      ret = (value != null ? value.replaceAll("\\n", "<br/>") : "");
    } else {
      // throw new EFapsException();
    }
    return ret;
  }

  public String getEditHtml(Context _context, Object _value, Field _field)
                                                                          throws EFapsException {
    String ret;
    if (_field.getRows() > 1) {
      ret = "<textarea " + "type=\"text\" " + "cols=\"" + _field.getCols()
          + "\" " + "rows=\"" + _field.getRows() + "\" " + "name=\""
          + _field.getName() + "\"" + ">";
      if (_value != null) {
        ret += _value;
      }
      ret += "</textarea>";
    } else {
      ret = "<input type=\"text\" " + "size=\"" + _field.getCols() + "\" "
          + "name=\"" + _field.getName() + "\" " + "value=\""
          + (_value != null ? _value : "") + "\"" + "/>";
    }
    return ret;
  }

  public String getCreateHtml(Context _context, Object _value, Field _field)
                                                                            throws EFapsException {
    StringBuffer ret = new StringBuffer();
    if (_field.getRows() > 1) {
      ret.append("<textarea " + "type=\"text\" " + "cols=\"").append(
          _field.getCols()).append("\" " + "rows=\"").append(_field.getRows())
          .append("\" " + "name=\"").append(_field.getName()).append("\" ");
      /*
       * if (_field.getAttribute()!=null &&
       * _field.getAttribute().getUniqueKeys()!=null) {
       * ret.append("onChange=\"eFapsUniqueKeyValueChange(this);\" "); }
       */
      ret.append(">");
      if (_value != null) {
        ret.append(_value);
      }
      ret.append("</textarea>");
    } else {
      ret.append("<input type=\"text\" " + "size=\"").append(_field.getCols())
          .append("\" " + "name=\"").append(_field.getName()).append(
              "\" " + "value=\"").append((_value != null ? _value : ""))
          .append("\" ");
      /*
       * if (_field.getAttribute()!=null &&
       * _field.getAttribute().getUniqueKeys()!=null) {
       * ret.append("onChange=\"eFapsUniqueKeyValueChange(this);\" "); }
       */
      ret.append(">");
    }
    return ret.toString();
  }

  public String getSearchHtml(Context _context, Object _value, Field _field)
                                                                            throws EFapsException {
    return "<input type=\"text\" " + "size=\"" + _field.getCols() + "\" "
        + "name=\"" + _field.getName() + "\" " + "value=\"*\"" + "/>";
  }
}

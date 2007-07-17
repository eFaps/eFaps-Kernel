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

import org.efaps.admin.ui.Field;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class FileUI implements UIInterface {

  public String getViewHtml(final Object _value, final Field _field)
      throws EFapsException {

    return "view";
  }

  public String getEditHtml(final Object _value, final Field _field)
      throws EFapsException {
    StringBuffer ret = new StringBuffer();

    ret.append("<input name=\"").append(_field.getName()).append("\" ").append(
        "type=\"file\" ").append("size=\"").append(_field.getCols()).append(
        "\" ").append(">");

    return ret.toString();
  }

  public String getCreateHtml(final Object _value, final Field _field)
      throws EFapsException {
    StringBuffer ret = new StringBuffer();

    ret.append("<input name=\"").append(_field.getName()).append("\" ").append(
        "type=\"file\" ").append("size=\"").append(_field.getCols()).append(
        "\" ").append(">");
    /*
     * StringBuffer ret = new StringBuffer(); if (_field.getRows()>1) {
     * ret.append( "<textarea "+ "type=\"text\" "+
     * "cols=\"").append(_field.getCols()).append("\" "+
     * "rows=\"").append(_field.getRows()).append("\" "+
     * "name=\"").append(_field.getName()).append("\" " ); ret.append(">"); if
     * (_value!=null) { ret.append(_value); } ret.append("</textarea>"); } else {
     * ret.append("<input type=\"text\" "+
     * "size=\"").append(_field.getCols()).append("\" "+
     * "name=\"").append(_field.getName()).append("\" "+
     * "value=\"").append((_value!=null ? _value : "")).append("\" " );
     * ret.append(">"); } return ret.toString();
     */
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

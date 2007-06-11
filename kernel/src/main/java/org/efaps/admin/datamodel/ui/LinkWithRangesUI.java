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

import java.util.Iterator;
import java.util.Map;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.ReturnInterface.ReturnValues;
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
    String ret = "";
    Return values = null;
    if (_value != null) {
      if (_field.hasEvent()) {
        values = _field.executeEvent();

        ret = (String) ((Map) values.get(ReturnValues.VALUES)).get(_value
            .toString());

      }

    } else {
      // throw new EFapsException();
    }
    return ret;
  }

  public String getEditHtml(Context _context, Object _value, Field _field)
                                                                          throws EFapsException {
    StringBuilder ret = new StringBuilder();
    Return values = null;
    if (_value != null) {
      if (_field.hasEvent()) {
        values = _field.executeEvent();

        ret.append("<form><select name=\"" + _field.getName()
            + "\" size=\"1\">");

        Iterator iter = ((Map) values.get(ReturnValues.VALUES)).entrySet()
            .iterator();

        while (iter.hasNext()) {
          Map.Entry entry = (Map.Entry) iter.next();
          ret.append("<option value=\"");
          ret.append(entry.getKey());
          if (_value.toString().equals(entry.getKey())) {
            ret.append("\" selected=\"selected");
          }
          ret.append("\">");
          ret.append(entry.getValue());
          ret.append("</option>");
        }

        ret.append("</select></form>");
      }

    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  public String getCreateHtml(Context _context, Object _value, Field _field)
                                                                            throws EFapsException {
    StringBuilder ret = new StringBuilder();
    Return values = null;

    if (_field.hasEvent()) {
      values = _field.executeEvent();

      ret.append("<form><select name=\"" + _field.getName() + "\" size=\"1\">");

      Iterator iter = ((Map) values.get(ReturnValues.VALUES)).entrySet()
          .iterator();

      while (iter.hasNext()) {
        Map.Entry entry = (Map.Entry) iter.next();
        ret.append("<option value=\"");
        ret.append(entry.getKey());

        ret.append("\">");
        ret.append(entry.getValue());
        ret.append("</option>");
      }

      ret.append("</select></form>");

    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  public String getSearchHtml(Context _context, Object _value, Field _field)
                                                                            throws EFapsException {
    return "<input type=\"text\" " + "size=\"" + _field.getCols() + "\" "
        + "name=\"" + _field.getName() + "\" " + "value=\"*\"" + "/>";
  }
}

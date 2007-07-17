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

import java.util.Iterator;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Field;
import org.efaps.util.EFapsException;

/**
 * This Class is the representation of
 * {@link org.efaps.admin.datamodel.attributetype.LinkWithRanges} for the
 * Userinterface.<br>
 * Depending on the accessmode (e.g. edit) the Value of a Field is presented in
 * an editable or noneditable mode.
 * 
 * @author jmo
 * @version $Id$
 */
public class LinkWithRangesUI implements UIInterface {

  public String getViewHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    StringBuilder ret = new StringBuilder();

    if (_value != null) {
      if (_field.hasEvents()) {

        for (Return values : _field.executeEvents(EventType.RANGE_VALUE)) {
          ret.append((String) ((Map) values.get(ReturnValues.VALUES))
              .get(_value.toString()));
        }
      }

    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  public String getEditHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    StringBuilder ret = new StringBuilder();

    if (_value != null) {
      if (_field.hasEvents()) {
        for (Return values : _field.executeEvents(EventType.RANGE_VALUE)) {

          ret.append("<select name=\"").append(_field.getName()).append(
              "\" size=\"1\">");

          Iterator iter =
              ((Map) values.get(ReturnValues.VALUES)).entrySet().iterator();

          while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            ret.append("<option value=\"").append(entry.getKey());
            if (_value.toString().equals(entry.getKey())) {
              ret.append("\" selected=\"selected");
            }
            ret.append("\">").append(entry.getValue()).append("</option>");
          }

          ret.append("</select>");
        }
      }

    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  public String getCreateHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    StringBuilder ret = new StringBuilder();

    if (_field.hasEvents()) {
      for (Return values : _field.executeEvents(EventType.RANGE_VALUE)) {

        ret.append("<select name=\"").append(_field.getName()).append(
            "\" size=\"1\">");

        Iterator iter =
            ((Map) values.get(ReturnValues.VALUES)).entrySet().iterator();

        while (iter.hasNext()) {
          Map.Entry entry = (Map.Entry) iter.next();
          ret.append("<option value=\"").append(entry.getKey()).append("\">")
              .append(entry.getValue()).append("</option>");
        }

        ret.append("</select>");
      }
    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  public String getSearchHtml(final Object _value, final Field _field,
      final Attribute _attribute) throws EFapsException {
    return "<input type=\"text\" " + "size=\"" + _field.getCols() + "\" "
        + "name=\"" + _field.getName() + "\" " + "value=\"*\"" + "/>";
  }

  public int compareTo(UIInterface _uiinterface, UIInterface __uiinterface2) {
    // TODO Auto-generated method stub
    return 0;
  }

 
}

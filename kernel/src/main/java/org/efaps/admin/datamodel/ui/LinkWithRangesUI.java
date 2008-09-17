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

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.field.Field;
import org.efaps.util.EFapsException;

/**
 * This Class is the representation of
 * {@link org.efaps.admin.datamodel.attributetype.LinkWithRanges} for the
 * Userinterface.<br>
 * Depending on the accessmode (e.g. edit) the Value of a Field is presented in
 * an editable or noneditable mode.
 *
 * @author jmox
 * @version $Id$
 */
public class LinkWithRangesUI extends AbstractUI {
  @Override
  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    final StringBuilder ret = new StringBuilder();
    final Attribute attribute = _fieldValue.getAttribute();

    if (_fieldValue.getValue() != null) {
      if (attribute.hasEvents(EventType.RANGE_VALUE)) {

        for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE)) {
          final TreeMap<?, ?> treemap = ((TreeMap<?, ?>) values.get(ReturnValues.VALUES));
          for (final Entry<?,?> entry : treemap.entrySet()) {
            if (entry.getValue().equals(_fieldValue.getValue().toString())) {
              ret.append(entry.getKey().toString());
            }
          }
        }
      }
    } else {
//      throw new EFapsException();
    }
    return ret.toString();
  }

  @Override
  public String getEditHtml(final FieldValue _fieldValue) throws EFapsException {
    final StringBuilder ret = new StringBuilder();
    final Attribute attribute = _fieldValue.getAttribute();
    if (_fieldValue.getValue() != null) {
      if (attribute.hasEvents(EventType.RANGE_VALUE)) {
        for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE)) {

          ret.append("<select name=\"").append(
              _fieldValue.getFieldDef().getField().getName()).append(
              "\" size=\"1\">");

          final Iterator<?> iter =
              ((TreeMap<?, ?>) values.get(ReturnValues.VALUES)).entrySet()
                  .iterator();

          while (iter.hasNext()) {
            final Entry<?, ?> entry = (Entry<?, ?>) iter.next();
            ret.append("<option value=\"").append(entry.getValue());
            if (_fieldValue.getValue().toString().equals(entry.getValue())) {
              ret.append("\" selected=\"selected");
            }
            ret.append("\">").append(entry.getKey()).append("</option>");
          }

          ret.append("</select>");
        }
      }

    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  @Override
  public String getCreateHtml(final FieldValue _fieldValue)
      throws EFapsException {
    final StringBuilder ret = new StringBuilder();
    final Attribute attribute = _fieldValue.getAttribute();
    if (attribute.hasEvents(EventType.RANGE_VALUE)) {
      for (final Return values : attribute.executeEvents(EventType.RANGE_VALUE)) {

        ret.append("<select name=\"").append(
            _fieldValue.getFieldDef().getField().getName()).append(
            "\" size=\"1\">");

        final Iterator<?> iter =
            ((TreeMap<?, ?>) values.get(ReturnValues.VALUES)).entrySet().iterator();

        while (iter.hasNext()) {
          final Entry<?, ?> entry = (Entry<?, ?>) iter.next();
          ret.append("<option value=\"").append(entry.getValue()).append("\">")
              .append(entry.getKey()).append("</option>");
        }

        ret.append("</select>");
      }
    } else {
      // throw new EFapsException();
    }
    return ret.toString();
  }

  @Override
  public String getSearchHtml(final FieldValue _fieldValue)
      throws EFapsException {
    final Field field = _fieldValue.getFieldDef().getField();
    return "<input type=\"text\" " + "size=\"" + field.getCols() + "\" "
        + "name=\"" + field.getName() + "\" " + "value=\"*\"" + "/>";
  }

}

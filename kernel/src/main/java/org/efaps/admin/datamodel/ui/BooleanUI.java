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

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Field;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 * TODO order of the boolean must be corrected
 */
public class BooleanUI implements UIInterface {

  public int compareTo(UIInterface _uiinterface) {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getCreateHtml(Object _value, Field _field)
      throws EFapsException {
    StringBuilder ret = new StringBuilder();
    ret.append("<select name=\"").append(_field.getName()).append(
        "\" size=\"1\">");
    ret.append("<option value=\"").append("1").append("\">");
    ret.append(getTrue(_field)).append("</option>");

    ret.append("<option value=\"").append("0").append("\">");
    ret.append(getFalse(_field)).append("</option>");

    ret.append("</select>");
    return ret.toString();

  }

  public String getEditHtml(Object _value, Field _field) throws EFapsException {
    StringBuilder ret = new StringBuilder();
    ret.append("<select name=\"").append(_field.getName()).append(
        "\" size=\"1\">");
    ret.append("<option value=\"").append("1").append("\">");
    ret.append(getTrue(_field)).append("</option>");

    ret.append("<option value=\"").append("0").append("\">");
    ret.append(getFalse(_field)).append("</option>");

    ret.append("</select>");
    return ret.toString();
  }

  public String getSearchHtml(Object _value, Field _field)
      throws EFapsException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getViewHtml(Object _value, Field _field) throws EFapsException {
    String ret = null;
    
    if (_value instanceof Boolean) {
      boolean bool = (Boolean) _value;
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
  // true=1,false=0
}

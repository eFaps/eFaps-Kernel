/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

public class FileUI implements UIInterface  {

  /**
   * @param _locale   locale object
   */
  public String getViewHtml(Context _context, Object _value, Field _field) throws EFapsException  {
/*    String ret = null;

    if (_value!=null)  {
      String value = _value.toString();
      ret = (value!=null ? value.replaceAll("\\n", "<br/>") : "");
    } else  {
// throw new EFapsException();
    }
    return ret;
*/
return "view";
  }

  /**
   * @param _locale   locale object
   */
  public String getEditHtml(Context _context, Object _value, Field _field) throws EFapsException  {
    StringBuffer ret = new StringBuffer();

    ret.append("<input name=\"").append(_field.getName()).append("\" ").
        append("type=\"file\" ").
        append("size=\"").append(_field.getCols()).append("\" ").
        append(">");

//        append("maxlength=\"100000\" ").
//        append("accept=\"text/*\"").
    return ret.toString();
  }

  /**
   * @param _locale   locale object
   */
  public String getCreateHtml(Context _context, Object _value, Field _field) throws EFapsException  {
    StringBuffer ret = new StringBuffer();

    ret.append("<input name=\"").append(_field.getName()).append("\" ").
        append("type=\"file\" ").
        append("size=\"").append(_field.getCols()).append("\" ").
        append(">");
/*
    StringBuffer ret = new StringBuffer();
    if (_field.getRows()>1)  {
      ret.append(
          "<textarea "+
              "type=\"text\" "+
              "cols=\"").append(_field.getCols()).append("\" "+
              "rows=\"").append(_field.getRows()).append("\" "+
              "name=\"").append(_field.getName()).append("\" "
      );
      ret.append(">");
      if (_value!=null)  {
        ret.append(_value);
      }
      ret.append("</textarea>");
    } else  {
      ret.append("<input type=\"text\" "+
          "size=\"").append(_field.getCols()).append("\" "+
          "name=\"").append(_field.getName()).append("\" "+
          "value=\"").append((_value!=null ? _value : "")).append("\" "
      );
      ret.append(">");
    }
    return ret.toString();
*/
    return ret.toString();
  }

  /**
   * @param _locale   locale object
   */
  public String getSearchHtml(Context _context, Object _value, Field _field) throws EFapsException  {
return "search";
  }
}
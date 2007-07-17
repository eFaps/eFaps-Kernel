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

import java.text.DateFormat;
import java.util.Date;

import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class DateTimeUI implements UIInterface {

  public String getViewHtml(final Object _value, final Field _field)
      throws EFapsException {
    String ret = null;

    if (_value instanceof Date) {
      Date value = (Date) _value;

      if (value != null) {
        DateFormat format =
            DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                DateFormat.DEFAULT, Context.getThreadContext().getLocale());
        ret = format.format(value);
      }
    } else {
      // throw new EFapsException(null, ret, null);
    }
    return ret;
  }

  public String getEditHtml(final Object _value, final Field _field)
      throws EFapsException {
    return "edit";
  }

  public String getCreateHtml(final Object _value, final Field _field)
      throws EFapsException {
    return "create";
  }

  public String getSearchHtml(final Object _value, final Field _field)
      throws EFapsException {
    return "search";
  }

  public int compareTo(final UIInterface _uiinterface) {

    return 0;
  }
}

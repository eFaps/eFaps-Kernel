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

import java.text.DateFormat;
import java.util.Date;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class DateTimeUI extends AbstractUI {
  @Override
  public String getViewHtml(final FieldValue _fieldValue) throws EFapsException {
    String ret = null;

    if (_fieldValue.getValue() instanceof Date) {
      final Date value = (Date) _fieldValue.getValue();

      if (value != null) {
        final DateFormat format =
            DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                DateFormat.DEFAULT, Context.getThreadContext().getLocale());
        ret = format.format(value);
      }
    } else {
      // throw new EFapsException(null, ret, null);
    }
    return ret;
  }

}

/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.util;

import org.apache.wicket.datetime.StyleDateConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class DateUtil {
  public static DateTime getDateFromParameter(final String _value)
      throws EFapsException {
    final StyleDateConverter styledate = new StyleDateConverter(false);
    final DateTimeFormatter fmt = DateTimeFormat.forPattern(styledate
        .getDatePattern());
    fmt.withLocale(Context.getThreadContext().getLocale());
    final DateTime dt = fmt.parseDateTime(_value);
    return dt;
  }
}

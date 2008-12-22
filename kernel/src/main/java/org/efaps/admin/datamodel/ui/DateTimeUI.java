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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class DateTimeUI extends AbstractUI {


  /**
   * @param _fieldValue  Feildvalue the view must be evaluated for
   * @throws EFapsException on error
   * @return STring with the value for the field
   */
  @Override
  public String getViewHtml(final FieldValue _fieldValue)
        throws EFapsException {
    String ret = null;

    if (_fieldValue.getValue() instanceof DateTime) {
      final DateTime datetime = (DateTime) _fieldValue.getValue();
      if (datetime != null) {
        final DateTimeFormatter formatter = DateTimeFormat.mediumDateTime();
        //format the Date with the Locale and Chronology from the user context
        ret = datetime.withChronology(
            Context.getThreadContext().getChronology()).toString(
            formatter.withLocale(Context.getThreadContext().getLocale()));
      }
    } else {
      //TODO throw new EFapsException
    }
    return ret;
  }

}

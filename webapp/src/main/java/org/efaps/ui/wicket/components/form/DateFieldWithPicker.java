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

package org.efaps.ui.wicket.components.form;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public class DateFieldWithPicker extends DateTextField {

  private static final long serialVersionUID = 1L;
  private final String inputName;

  /**
   * @param id
   * @param model
   * @param converter
   */
  public DateFieldWithPicker(final String id, final IModel<Date> model,
      final DateConverter converter, final String _inputName) {
    super(id, model, converter);
    this.inputName = _inputName;
    this.add(new DatePicker(){
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean enableMonthYearSelection() {
        return true;
      }
    });
  }

  public String getDateAsString(final String _value) {
    String ret = null;
    final SimpleDateFormat format = new SimpleDateFormat(this.getTextFormat(),getLocale());
    try {
      final Date orginalDate = format.parse(_value);

      final DateFormat format2 =
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
            DateFormat.DEFAULT, getLocale());
      ret = format2.format(orginalDate);
    } catch (final ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;
  }


  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    _tag.setName("input");
    super.onComponentTag(_tag);
  }

  @Override
  public String getInputName() {
    return this.inputName;
  }


}

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

package org.efaps.ui.wicket.components.form;

import java.util.Date;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public class DateFieldWithPicker extends DateTextField {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Name of the inputfield.
   */
  private final String inputName;

  /**
   * @param _wicketId   wicketId of the Component
   * @param _model      model for the Component
   * @param _converter  converter for the Component
   * @param _inputName  name of the input field
   */
  public DateFieldWithPicker(final String _wicketId,
                             final IModel<Date> _model,
                             final DateConverter _converter,
                             final String _inputName) {

    super(_wicketId, _model, _converter);
    this.inputName = _inputName;
    this.add(new DatePicker() {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean enableMonthYearSelection() {
        return true;
      }
    });
  }

  public String getDateAsString(final String _value) {
    String ret = null;

    final DateTimeFormatter fmt =
                              DateTimeFormat.forPattern(getTextFormat());
    fmt.withLocale(getLocale());
    final DateTime dt = fmt.parseDateTime(_value);
    final DateTimeFormatter isofmt = ISODateTimeFormat.dateTime();

    ret = dt.toString(isofmt);

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

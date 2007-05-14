/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 * 
 */
public class DateTimeType extends AbstractType {

  public void update(Context _context, final PreparedStatement _stmt,
                     final int _index) throws SQLException {
    _stmt.setTimestamp(_index, getValue());
  }

  /**
   * 
   * 
   * @todo test that only one value is given for indexes
   */
  public Object readValue(Context _context, CachedResult _rs,
                          ArrayList<Integer> _indexes) {
    setValue(_rs.getTimestamp(_indexes.get(0).intValue()));
    return getValue();
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * 
   * @param _context
   *          context for this request
   * @param _value
   *          new value to set
   */
  public void set(final Context _context, final Object _value) {
    if (_value instanceof Date) {
      setValue(new Timestamp((((Date) _value)).getTime()));
    }
  }

  /**
   * The method returns a string as the viewable value of the attribute type.
   * Here, the date time value is converted to a localised viewing string.
   * 
   * @param _locale
   *          locale object
   */
  public String getViewableString(Locale _locale) {
    String ret = null;
    if (getValue() != null) {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
          DateFormat.DEFAULT, _locale);
      ret = format.format(getValue());
    } else {
      ret = "";
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @param _context
   *          context for this request
   * @param _name
   *          name of the field
   * @param _columns
   *          columns
   * @param _rows
   *          rows
   */
  public String getSearchHtml(Locale _locale, Field _field) {
    return "";
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * 
   * 
   * @see #getValue
   * @see #setValue
   */
  private Timestamp value = null;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   * 
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(final Timestamp _value) {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   * 
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public Timestamp getValue() {
    return this.value;
  }

  public String toString() {
    return "" + getValue();
  }
}
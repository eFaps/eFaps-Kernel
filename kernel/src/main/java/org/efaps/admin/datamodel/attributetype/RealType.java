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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public class RealType extends AbstractType {

  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    _stmt.setDouble(_indexes.get(0), getValue());
  }

  /**
   * @todo test that only one value is given for indexes
   */
  public Object readValue(CachedResult _rs, List<Integer> _indexes) {
    setValue(_rs.getDouble(_indexes.get(0).intValue()));
    return _rs.getDouble(_indexes.get(0).intValue());
  }

  // //////////////////////////////////////////////////////////////////////////7

  /**
   * The instance method sets {@link #value} by parsing the parameter <i>_value</i>.
   * 
   * @param _context
   *          context for this request
   * @param _value
   *          new value to set
   */
  public void set(String _value) throws NumberFormatException {
    if (_value != null) {
      setValue(Double.parseDouble(_value));
    }
  }

  public void set(final Context _context, final Object _value) {
    if (_value != null) {
      if ((_value instanceof String) && (((String) _value).length() > 0)) {
        setValue(Double.parseDouble((String) _value));
      } else if (_value instanceof Number) {
        setValue(((Number) _value).doubleValue());
      }
    }
  }

  /**
   * The method returns a string as the viewable value of the attribute type.
   * Here, the double value is converted to a localised viewing string.
   * 
   * @param _locale
   *          locale object
   */
  public String getViewableString(Locale _locale) {
    NumberFormat format = NumberFormat.getNumberInstance(_locale);
    return format.format(getValue());
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance method stores the value for this real type.
   * 
   * @see #getValue
   * @see #setValue
   */
  private double value = 0;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   * 
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(double _value) {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   * 
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public double getValue() {
    return this.value;
  }

  public String toString() {
    return "" + getValue();
  }

  @Override
  public void set(Object _value) {
    if (_value != null) {
      if ((_value instanceof String) && (((String) _value).length() > 0)) {
        setValue(Double.parseDouble((String) _value));
      } else if (_value instanceof Number) {
        setValue(((Number) _value).doubleValue());
      }
    }

  }

}

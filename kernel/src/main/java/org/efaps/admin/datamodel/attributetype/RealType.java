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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 * 
 */
public class RealType extends AbstractType {

  public void update(Context _context, PreparedStatement _stmt, int _index)
                                                                           throws SQLException {
    _stmt.setDouble(_index, getValue());
  }

  /**
   * 
   * 
   * @todo test that only one value is given for indexes
   */
  public Object readValue(Context _context, CachedResult _rs,
                          ArrayList<Integer> _indexes) {
    // setValue(_rs.getDouble(_index));
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
  public void set(Context _context, String _value) throws NumberFormatException {
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

  // ///////////////////////////////////////////////////////////////////////////
  // methods of interface Comparable

  /**
   * Compares this object with the specified object for order. Returns a
   * negative integer, zero, or a positive integer as this object is less than,
   * equal to, or greater than the specified object.<br/> The method makes an
   * real compare if the specified object is also an instance of IntegerType,
   * otherwise the default implementation from {@link AbstractType#compareTo} is
   * used.
   * 
   * @param _locale
   *          locale object
   * @param _object
   *          the Object to be compared.
   * @return a negative integer, zero, or a positive integer as this object is
   *         less than, equal to, or greater than the specified object.
   */
  public int compareTo(Locale _locale, AttributeTypeInterface _object) {
    int ret;
    if (_object instanceof RealType) {
      double tmp = getValue() - ((RealType) _object).getValue();
      ret = (tmp < 0.0 ? -1 : (tmp > 0.0 ? 1 : 0));
    } else {
      ret = super.compareTo(_locale, _object);
    }
    return ret;
  }

  public String toString() {
    return "" + getValue();
  }
}
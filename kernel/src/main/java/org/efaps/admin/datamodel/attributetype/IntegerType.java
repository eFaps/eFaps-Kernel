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
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public class IntegerType extends AbstractType {

  @Override
  public void update(Object _object, PreparedStatement _stmt,
      List<Integer> _index) throws SQLException {
    _stmt.setInt(_index.get(0), getValue());
  }

  /**
   * @todo test that only one value is given for indexes
   */
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes) {

    setValue(_rs.getLong(_indexes.get(0).intValue()).intValue());
    return _rs.getLong(_indexes.get(0).intValue());
  }

  // //////////////////////////////////////////////////////////////////////////7

  public void set(final Object _value) {
    if (_value != null) {
      if ((_value instanceof String) && (((String) _value).length() > 0)) {
        setValue(Integer.parseInt((String) _value));
      } else if (_value instanceof Number) {
        setValue(((Number) _value).intValue());
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @see #getValue
   * @see #setValue
   */
  private int value = 0;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   * 
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(final int _value) {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   * 
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public int getValue() {
    return this.value;
  }

  public String toString() {
    return "" + getValue();
  }

}

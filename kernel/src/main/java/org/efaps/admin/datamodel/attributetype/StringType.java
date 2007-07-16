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
public class StringType extends AbstractType {

  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    _stmt.setString(_indexes.get(0), getValue());
  }

  /**
   * @todo test that only one value is given for indexes
   */
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes) {

    setValue(_rs.getString(_indexes.get(0).intValue()));
    String ret = _rs.getString(_indexes.get(0).intValue());
    if (ret != null) {
      ret = ret.trim();
    }
    return ret;
  }

  // //////////////////////////////////////////////////////////////////////////7

  /**
   * The localised string and the internal string value are equal. So the
   * internal value can be set directly with method {@link #setValue}.
   * 
   * @param _context
   *          context for this request
   * @param _value
   *          new value to set
   */
  public void set(final Object _value) {
    if (_value instanceof String) {
      setValue((String) _value);
    } else if (_value != null) {
      setValue(_value.toString());
    }
  }

  

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @see #getValue
   * @see #setValue
   */
  private String value = null;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   * 
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(String _value) {
    this.value = (_value != null ? _value.trim() : null);
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   * 
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public String getValue() {
    return this.value;
  }

  public String toString() {
    return "" + getValue();
  }
}

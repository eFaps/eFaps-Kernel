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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class LongType extends AbstractType {

  @Override
  public void update(final Object _Object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    _stmt.setLong(_indexes.get(0), getValue());
  }

  /**
   * @todo test that only one value is given for indexes
   */
  @Override
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes) {
    final Long value = _rs.getLong(_indexes.get(0).intValue());
    setValue(value != null ? value.longValue() : 0);
    return getValue();
  }

  // //////////////////////////////////////////////////////////////////////////7

  /**
   * @param _context
   *          context for this request
   * @param _value
   *          new value to set
   */
  @Override
  public void set(final Object _value) {
    if (_value != null) {
      if ((_value instanceof String) && (((String) _value).length() > 0)) {
        setValue(Long.parseLong((String) _value));
      } else if (_value instanceof Number) {
        setValue(((Number) _value).longValue());
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @see #getValue
   * @see #setValue
   */
  private long value = 0;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(final long _value) {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public long getValue() {
    return this.value;
  }


  @Override
  public String toString() {
    return "" + getValue();
  }

  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.AttributeTypeInterface#get()
   */
  public Object get() {
    return value;
  }
}

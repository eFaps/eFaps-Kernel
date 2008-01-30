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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public class DateTimeType extends AbstractType {

  /**
   * @todo test that only one value is given for indexes
   */
  public Object readValue(CachedResult _rs, List<Integer> _indexes) {
    setValue(_rs.getTimestamp(_indexes.get(0).intValue()));
    return getValue();
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @param _context
   *          context for this request
   * @param _value
   *          new value to set
   */
  public void set(final Object _value) {
    if (_value instanceof Date) {
      setValue(new Timestamp((((Date) _value)).getTime()));
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  // ///////////////////////////////////////////////////////////////////////////

  @Override
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _index) throws SQLException {
    _stmt.setTimestamp(_index.get(0), getValue());
  }

  /**
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

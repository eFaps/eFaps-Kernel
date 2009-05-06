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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public class DecimalType extends AbstractType {

  @Override
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _index) throws SQLException {
    _stmt.setBigDecimal(_index.get(0), getValue());
  }

  /**
   * @todo test that only one value is given for indexes
   */
  @Override
  public Object readValue(final CachedResult _rs,
                          final List<Integer> _indexes) {

    final BigDecimal val = _rs.getDecimal(_indexes.get(0).intValue());
    this.value = (val != null) ? val : new BigDecimal(0);
    return this.value;
  }

  // //////////////////////////////////////////////////////////////////////////7

  @Override
  public void set(final Object _value) {
    if (_value != null) {
      if ((_value instanceof String) && (((String) _value).length() > 0)) {
        setValue(new BigDecimal((String) _value));
      } else if (_value instanceof BigDecimal) {
        setValue((BigDecimal) _value);
      } else if (_value instanceof Number) {
        setValue(new BigDecimal(((Number) _value).toString()));
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @see #getValue
   * @see #setValue
   */
  private BigDecimal value = new BigDecimal(0);

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(final BigDecimal _value) {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public BigDecimal getValue() {
    return this.value;
  }
  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.AttributeTypeInterface#get()
   */
  public Object get() {
    return this.value;
  }

  @Override
  public String toString() {
    return "" + getValue();
  }

}

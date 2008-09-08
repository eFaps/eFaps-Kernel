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
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public class TypeType extends AbstractType {

  @Override
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    throw new SQLException("Update value for Type not allowed!!!");
  }

  /**
   * The method reads from a SQL result set the value for the type. If no type
   * sql column is given in the type description, the value is read directly
   * from the attribute.
   */
  @Override
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
      throws Exception {
    Type value;

    if (getAttribute().getSqlColNames().size() > 0) {
      value = Type.get(_rs.getLong(_indexes.get(0).intValue()));
    } else {
      value = getAttribute().getParent();
    }
    setValue(value);
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @todo must an exception thrown?
   */
  @Override
  public void set(final Object _value) {
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The value stores the instance of {@link org.efaps.admin.datamodel.Type}
   * which represents current value.
   *
   * @see #setType
   * @see #getType
   */
  private Type value = null;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value
   *          new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(final Type _value) {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public Type getValue() {
    return this.value;
  }

  /* (non-Javadoc)
   * @see org.efaps.admin.datamodel.AttributeTypeInterface#get()
   */
  public Object get() {
    return value;
  }


  @Override
  public String toString() {
    return "" + getValue();
  }
}

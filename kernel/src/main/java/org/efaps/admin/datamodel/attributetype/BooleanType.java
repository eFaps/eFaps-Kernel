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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 *
 */
public class BooleanType extends AbstractType  {

  /////////////////////////////////////////////////////////////////////////////
  // interface to the data base

  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException  {
    if (getValue())  {
      _stmt.setInt(_index, 1);
    } else  {
      _stmt.setNull(_index, Types.INTEGER);
    }
  }

  /**
   *
   *
   * @todo test that only one value is given for indexes
   */
  public Object readValue(Context _context, CachedResult _rs, ArrayList<Integer> _indexes) throws SQLException  {
    Boolean value = false;
    Long longValue = _rs.getLong(_indexes.get(0).intValue());
    if ((longValue != null) && (longValue != 0))  {
      value = true;
    }
    setValue(value);
    return value;
  }

  /////////////////////////////////////////////////////////////////////////////
  // interface to the user interface

   /**
   *
   * @param _context  context for this request
   * @param _value    new value to set
   */
  public void set(Context _context, String _value)  {
    if (_value!=null && _value.equalsIgnoreCase("TRUE"))  {
      setValue(true);
    } else  {
      setValue(false);
    }
  }

  /**
   * The method returns a string as the viewable value of the attribute type.
   *
   * @param _locale locale object
   */
  public String getViewableString(Locale _locale)  {
    return ""+getValue();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   *
   * @see #getValue
   * @see #setValue
   */
  private boolean value = false;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(boolean _value)  {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public boolean getValue()  {
    return this.value;
  }

}
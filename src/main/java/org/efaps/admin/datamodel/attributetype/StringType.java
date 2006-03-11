/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.admin.datamodel.attributetype;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.db.Context;
import org.efaps.admin.ui.Field;

/**
 *
 */
public class StringType extends AbstractType  {

  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException  {
    _stmt.setString(_index, getValue());
  }

  /**
   *
   *
   * @todo test that only one value is given for indexes
   */
  public Object readValue(Context _context, ResultSet _rs, ArrayList<Integer> _indexes) throws SQLException  {
//    setValue(_rs.getString(_index));
setValue(_rs.getString(_indexes.get(0).intValue()));
    String ret = _rs.getString(_indexes.get(0).intValue());
    if (ret!=null)  {
      ret = ret.trim();
    }
    return ret;
  }

  ////////////////////////////////////////////////////////////////////////////7

  /**
   * The localised string and the internal string value are equal. So the
   * internal value can be set directly with method {@link #setValue}.
   *
   * @param _context  context for this request
   * @param _value    new value to set
   */
  public void set(Context _context, String _value)  {
    setValue(_value);
  }

  /**
   * The method returns a string as the viewable value of the attribute type.
   * Here, the original value of the string is returned.
   *
   * @param _locale locale object
   */
  public String getViewableString(Locale _locale)  {
    return (getValue()!=null ? getValue() : "");
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   *
   * @see #getValue
   * @see #setValue
   */
  private String value = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(String _value)  {
    this.value = (_value!=null ? _value.trim() : null);
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public String getValue()  {
    return this.value;
  }

}
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.db.Context;
import org.efaps.admin.ui.Field;
import org.efaps.db.query.CachedResult;

/**
 *
 */
public abstract class AbstractLinkType extends AbstractType  {

  /**
   *
   * @param _context  context for this request
   * @param _value    new value to set
   */
  public void set(Context _context, String _value)  {
    if (_value!=null && _value.length()>0)  {
      setValue(Long.parseLong(_value));
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Updates the value in the database with the stored value in the cache. If
   * the value is '0', the value in the database is set to <i>NULL</i> (a zero
   * in the cache means no link!).
   *
   */
  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException  {
    if (getValue()==0)  {
      _stmt.setNull(_index, java.sql.Types.INTEGER);
    } else  {
      _stmt.setLong(_index, getValue());
    }
  }

  /**
   *
   * @param _rs
   * @param _index
   * @todo test that only one value is given for indexes
   */
  public Object readValue(Context _context, CachedResult _rs, ArrayList<Integer> _indexes)  {
//    setValue(_rs.getLong(_index));
setValue(_rs.getLong(_indexes.get(0)));
return getValue();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   *
   * @see #getValue
   * @see #setValue
   */
  private long value = 0;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #value}.
   *
   * @param _value new value for instance variable {@link #value}
   * @see #value
   * @see #getValue
   */
  public void setValue(long _value)  {
    this.value = _value;
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   *
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public long getValue()  {
    return this.value;
  }
}
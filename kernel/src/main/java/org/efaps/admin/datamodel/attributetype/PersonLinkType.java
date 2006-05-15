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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 *
 */
public class PersonLinkType extends AbstractLinkType  {

  /**
   *
   * @param _rs
   * @param _index
   */
  public Object readValue(Context _context, CachedResult _rs, ArrayList<Integer> _indexes)  {
    Object ret = null;

    Long userId = (Long) super.readValue(_context, _rs, _indexes);
    if (userId != null)  {
try {
    ret = Person.get(getValue());
    if (ret==null)  {
      ret = Role.get(getValue());
    }
} catch (Exception e)  {
  e.printStackTrace();
}
    }

    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * @param _locale locale object
   * @todo exception handling
   */
  public String getViewableString(Locale _locale)  {
    String ret = "";
    return ret;
  }
}
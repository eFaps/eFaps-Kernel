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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.admin.user;

import org.efaps.admin.AdminObject;
import org.efaps.db.Context;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class UserObject extends AdminObject  {

  /**
   * Constructor to set the id and name of the user object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  protected UserObject(final long _id, final String _name)  {
    super(_id, _name);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Checks, if the given person is assigned to this user object. The method
   * must be overwritten by the special implementations.
   *
   * @param _person person to test
   * @return <i>true</i> if the person is assigned to this user object,
   *         otherwise <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  abstract public boolean hasChildPerson(final Person _person);

  /**
   * Checks, if the context user is assigned to this user object. The instance
   * method uses {@link #hasChildPerson} to test this.
   *
   * @param _context context for this request
   * @see #hasChildPerson
   */
  public boolean isAssigned(final Context _context)  {
    return hasChildPerson(_context.getPerson());
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link UserObject}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link UserObject}
   */
  static public UserObject getUserObject(final Context _context,
                                         final long _id) throws Exception  {
    UserObject ret = Role.get(_id);
    if (ret == null)  {
      ret = Person.get(_id);
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link UserObject}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link UserObject}
   */
  static public UserObject getUserObject(final Context _context,
                                         final String _name) throws Exception  {
    UserObject ret = Role.get(_name);
    if (ret == null)  {
      ret = Person.get(_name);
    }
    return ret;
  }
}

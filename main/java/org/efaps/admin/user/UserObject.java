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

package org.efaps.admin.user;

import org.efaps.admin.AdminObject;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;

public abstract class UserObject extends AdminObject implements CacheInterface  {

  /**
   * Constructor to set the id and name of the user object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  protected UserObject(long _id, String _name)  {
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
  abstract public boolean hasChildPerson(Person _person);

  /**
   * Checks, if the context user is assigned to this user object. The instance
   * method uses {@link #hasChildPerson} to test this.
   *
   * @param _context context for this request
   * @see #hasChildPerson
   */
  public boolean isAssigned(Context _context)  {
    return hasChildPerson(_context.getPerson());
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Role}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Role}
   * @see #getCache
   */
  static public UserObject getUserObject(Context _context, long _id)  {
    return getUserObjectCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Role}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Role}
   * @see #getCache
   */
  static public UserObject getUserObject(Context _context, String _name)  {
    return getUserObjectCache().get(_name);
  }

  /**
   * The static method adds a user object to the cache.
   *
   * @param _userObject user object to add to the user object cache.
   */
  static protected void addUserObject(UserObject _userObject)  {
    getUserObjectCache().add(_userObject);
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static private UserObjectCache getUserObjectCache()  {
    return cache;
  }

  /**
   * Static setter method for the cache variable {@link #cache}.
   *
   * @param _cache new value of static variable {@link #cache}
   */
  static private void setUserObjectCache(UserObjectCache _cache)  {
    cache = _cache;
  }

  /**
   * Stores all instances of this class {@link UserObject}.
   *
   * @see #getCache
   */
  static private UserObjectCache cache = new UserObjectCache();

  /////////////////////////////////////////////////////////////////////////////

  static protected class UserObjectCache extends Cache<UserObject>  {
  }
}

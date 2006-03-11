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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.efaps.db.Cache;
import org.efaps.db.Context;

public class Role extends UserObject  {

  /**
   * Create a new role instance. The method is used from the static method
   * {@link #readRoles} to read all roles from the database.
   *
   * @param _id
   */
  private Role(long _id, String _name)  {
    super(_id, _name);
  }

  /**
   * Returns the viewable name of the role. The method {@link #getName} is
   * used for the viewing name.
   *
   * @param _context context for this request
   * @see #getName
   */
  public String getViewableName(Context _context)  {
    return getName();
  }

  /**
   * Checks, if the given person is assigned to this role.
   *
   * @param _person person to test
   * @return <i>true</i> if the person is assigned to this role, otherwise
   *         <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  public boolean hasChildPerson(Person _person) {
    boolean ret = false;

    if (getPersons().contains(_person))  {
      ret = true;
    }
    return ret;
  }

  /**
   * Assign a new person to this role.
   *
   * @param _person person to add
   * @see #persons
   * @see #isChildPerson
   */
  void add(Person _person)  {
    getPersons().add(_person);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Instance variable to hold all assigned persons.
   *
   * @see #getPersons
   * @see #add(Person)
   * @see #isChildPerson
   */
  private Set<Person> persons = new HashSet<Person>();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Getter method for instance variable {@link #persons}.
   *
   * @return value of instance variable {@link #persons}
   * @see #persons
   * @see #add(Person)
   * @see #isChildPerson
   */
  public Set<Person> getPersons()  {
    return this.persons;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Role}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Role}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  static public Role get(long _id) throws Exception  {
    Role ret = getCache().get(_id);
    if (ret==null)  {
      Context context = new Context();
      try  {
        ret = getCache().readRole(context,
            "select "+
              "USERABSTRACT.ID,"+
              "USERABSTRACT.NAME "+
            "from USERABSTRACT "+
            "where USERABSTRACT.TYPEID=11000 and "+
                  "USERABSTRACT.ID="+_id
        );
      } catch (Throwable e)  {
        throw new Exception(e);
      } finally  {
        context.close();
      }
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Role}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Role}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  static public Role get(String _name) throws Exception  {
    Role ret = getCache().get(_name);
    if (ret==null)  {
      Context context = new Context();
      try  {
        ret = getCache().readRole(context,
            "select "+
              "USERABSTRACT.ID,"+
              "USERABSTRACT.NAME "+
            "from USERABSTRACT "+
            "where USERABSTRACT.TYPEID=11000 and "+
                  "USERABSTRACT.NAME='"+_name+"'"
        );
      } catch (Throwable e)  {
        throw new Exception(e);
      } finally  {
        context.close();
      }
    }
    return ret;
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static public RoleCache getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link Role}.
   *
   * @see #getCache
   */
  static private RoleCache cache = new RoleCache();

  /////////////////////////////////////////////////////////////////////////////

  static protected class RoleCache extends Cache<Role>  {

    private Role readRole(Context _context, String _sql) throws Exception  {
      Statement stmt = _context.getConnection().createStatement();
      Role ret = null;
      try  {
        ResultSet rs = stmt.executeQuery(_sql);
        if (rs.next())  {
          long id =     rs.getLong(1);
          String name = rs.getString(2);
          Role role = new Role(id, name);
          this.add(role);
          UserObject.addUserObject(role);
        }
        rs.close();
      } catch (Exception e)  {
e.printStackTrace();
        throw e;
      } finally  {
        stmt.close();
      }
      return ret;
    }
  }
}

/*
 * Copyright 2003-2007 The eFaps Team
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

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.AdminObject;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id: JAASSystem.java 661 2007-02-06 22:14:33 +0000 (Tue, 06 Feb
 *          2007) tmo $
 * @todo description
 */
public class JAASSystem extends AdminObject {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log                   LOG                      = LogFactory
                                                                          .getLog(JAASSystem.class);

  /**
   * This is the sql select statement to select all JAAS systems from the
   * database.
   */
  private static final String                SQL_SELECT               = "select "
                                                                          + "ID,"
                                                                          + "NAME,"
                                                                          + "CLASSNAMEPERSON,"
                                                                          + "METHODPERSONKEY,"
                                                                          + "METHODPERSONNAME,"
                                                                          + "METHODPERSONFIRSTNAME,"
                                                                          + "METHODPERSONLASTNAME,"
                                                                          + "METHODPERSONEMAIL,"
                                                                          + "METHODPERSONORG,"
                                                                          + "METHODPERSONURL,"
                                                                          + "METHODPERSONPHONE,"
                                                                          + "METHODPERSONMOBILE,"
                                                                          + "METHODPERSONFAX,"
                                                                          + "CLASSNAMEROLE,"
                                                                          + "METHODROLEKEY,"
                                                                          + "CLASSNAMEGROUP,"
                                                                          + "METHODGROUPKEY "
                                                                          + "from V_USERJAASSYSTEM";

  /**
   * Stores all instances of class {@link JAASSystem}.
   */
  private static final JAASSystemCache       cache                    = new JAASSystemCache();

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The class used as princple for persons for this JAAS system is stored in
   * this instance variable.
   * 
   * @see #getPersonJAASPrincipleClass
   */
  private Class                              personJAASPrincipleClass = null;

  /**
   * @see #getPersonMethodKey
   */
  private Method                             personMethodKey          = null;

  /**
   * @see #getPersonMethodName
   */
  private Method                             personMethodName         = null;

  /**
   * 
   */
  private final Map<Person.AttrName, Method> personMethodAttributes   = new HashMap<Person.AttrName, Method>();

  /**
   * The class used as princple for roles for this JAAS system is stored in this
   * instance variable.
   * 
   * @see #getRoleJAASPrincipleClass
   */
  private Class                              roleJAASPrincipleClass   = null;

  /**
   * @see #getRoleMethodKey
   */
  private Method                             roleMethodKey            = null;

  /**
   * The class used as princple for groups for this JAAS system is stored in
   * this instance variable.
   * 
   * @see #getGroupJAASPrincipleClass
   */
  private Class                              groupJAASPrincipleClass  = null;

  /**
   * @see #getGroupMethodKey
   */
  private Method                             groupMethodKey           = null;

  /**
   * Constructor to set the id and name of the user object.
   * 
   * @param _id
   *          id to set
   * @param _name
   *          name to set
   */
  private JAASSystem(final long _id, final String _name) {
    super(_id, null, _name);
  }

  // TODO: this is needed anymore??
  public String getViewableName(final Context _context) {
    return getName();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * This is the getter method for instance variable
   * {@link #personJAASPrincipleClass}.
   * 
   * @return the value of the instance variable
   *         {@link #personJAASPrincipleClass}.
   * @see #personJAASPrincipleClass
   */
  public Class getPersonJAASPrincipleClass() {
    return this.personJAASPrincipleClass;
  }

  /**
   * This is the getter method for instance variable {@link #personMethodKey}.
   * 
   * @return the value of the instance variable {@link #personMethodKey}.
   * @see #personMethodKey
   */
  public Method getPersonMethodKey() {
    return this.personMethodKey;
  }

  /**
   * This is the getter method for instance variable {@link #personMethodName}.
   * 
   * @return the value of the instance variable {@link #personMethodName}.
   * @see #personMethodName
   */
  public Method getPersonMethodName() {
    return this.personMethodName;
  }

  /**
   * This is the getter method for instance variable
   * {@link #personMethodAttributes}.
   * 
   * @return the value of the instance variable {@link #personMethodAttributes}.
   * @see #personMethodAttributes
   */
  public Map<Person.AttrName, Method> getPersonMethodAttributes() {
    return this.personMethodAttributes;
  }

  /**
   * This is the getter method for instance variable
   * {@link #roleJAASPrincipleClass}.
   * 
   * @return the value of the instance variable {@link #roleJAASPrincipleClass}.
   * @see #roleJAASPrincipleClass
   */
  public Class getRoleJAASPrincipleClass() {
    return this.roleJAASPrincipleClass;
  }

  /**
   * This is the getter method for instance variable {@link #roleMethodKey}.
   * 
   * @return the value of the instance variable {@link #roleMethodKey}.
   * @see #roleMethodKey
   */
  public Method getRoleMethodKey() {
    return this.roleMethodKey;
  }

  /**
   * This is the getter method for instance variable
   * {@link #groupJAASPrincipleClass}.
   * 
   * @return the value of the instance variable {@link #groupJAASPrincipleClass}.
   * @see #groupJAASPrincipleClass
   */
  public Class getGroupJAASPrincipleClass() {
    return this.groupJAASPrincipleClass;
  }

  /**
   * This is the getter method for instance variable {@link #groupMethodKey}.
   * 
   * @return the value of the instance variable {@link #groupMethodKey}.
   * @see #groupMethodKey
   */
  public Method getGroupMethodKey() {
    return this.groupMethodKey;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Initialise the cache of JAAS systems.
   * 
   * @param _context
   *          eFaps context for this request
   * @see #getMethod
   */
  public static void initialise() throws CacheReloadException {
    ConnectionResource con = null;
    try {
      con = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;
      try {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next()) {
          long id = rs.getLong(1);
          String name = rs.getString(2).trim();
          String personClassName = rs.getString(3);
          String personMethodKey = rs.getString(4);
          String personMethodName = rs.getString(5);
          String personMethodFirstName = rs.getString(6);
          String personMethodLastName = rs.getString(7);
          String personMethodEmail = rs.getString(8);
          String personMethodOrg = rs.getString(9);
          String personMethodUrl = rs.getString(10);
          String personMethodPhone = rs.getString(11);
          String personMethodMobile = rs.getString(12);
          String personMethodFax = rs.getString(13);
          String roleClassName = rs.getString(14);
          String roleMethodKey = rs.getString(15);
          String groupClassName = rs.getString(16);
          String groupMethodKey = rs.getString(17);

          LOG.debug("read JAAS System '" + name + "' (id = " + id + ")");

          try {
            JAASSystem system = new JAASSystem(id, name);
            system.personJAASPrincipleClass = Class.forName(personClassName
                .trim());
            system.personMethodKey = getMethod(system.personJAASPrincipleClass,
                personMethodKey, "person key", name, id);
            system.personMethodName = getMethod(
                system.personJAASPrincipleClass, personMethodName,
                "person name", name, id);

            Method method = getMethod(system.personJAASPrincipleClass,
                personMethodFirstName, "person first name", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.FirstName,
                  method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodLastName, "person last name", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.LastName,
                  method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodEmail, "person email", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.Email, method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodOrg, "person organisation", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.Organisation,
                  method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodUrl, "person URL", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.URL, method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodPhone, "person phone number", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.Phone, method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodMobile, "person mobile number", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.Mobile, method);
            }
            method = getMethod(system.personJAASPrincipleClass,
                personMethodFax, "person fax number", name, id);
            if (method != null) {
              system.personMethodAttributes.put(Person.AttrName.Fax, method);
            }

            if ((roleClassName != null) && (roleClassName.trim().length() > 0)) {
              system.roleJAASPrincipleClass = Class.forName(roleClassName
                  .trim());
              system.roleMethodKey = getMethod(system.roleJAASPrincipleClass,
                  roleMethodKey, "role key", name, id);
            }
            if ((groupClassName != null)
                && (groupClassName.trim().length() > 0)) {
              system.groupJAASPrincipleClass = Class.forName(groupClassName
                  .trim());
              system.groupMethodKey = getMethod(system.groupJAASPrincipleClass,
                  groupMethodKey, "group key", name, id);
            }

            if ((system.personMethodKey != null)
                && (system.personMethodName != null)
                && ((system.roleJAASPrincipleClass == null) || ((system.roleJAASPrincipleClass != null) && (system.roleMethodKey != null)))
                && ((system.groupJAASPrincipleClass == null) || ((system.groupJAASPrincipleClass != null) && (system.groupMethodKey != null)))) {

              cache.add(system);
            }
          } catch (ClassNotFoundException e) {
            LOG.error("could not get a class for JAAS System '" + name + "' "
                + "(id = " + id + ")", e);
          }
        }
        rs.close();

      }
      finally {
        if (stmt != null) {
          stmt.close();
        }
      }

      con.commit();

    } catch (SQLException e) {
      throw new CacheReloadException("could not read roles", e);
    } catch (EFapsException e) {
      throw new CacheReloadException("could not read roles", e);
    }
    finally {
      if ((con != null) && con.isOpened()) {
        try {
          con.abort();
        } catch (EFapsException e) {
          throw new CacheReloadException("could not read roles", e);
        }
      }
    }
  }

  /**
   * Returns for the given method name the method found in the given class. The
   * found method is tested, if the method is returning string and has no
   * parameters.<br/> If the checkes fails or the method is not found, an error
   * log is written and <code>null</code> is returned.
   * 
   * @param _class
   *          class on which the method is searched
   * @param _method
   *          method name
   * @param _type
   *          text string for which the method is searched
   * @param _jaasName
   *          name of the JAAS system
   * @param _jaasId
   *          id of the JAAS system
   * @return found method, or <code>null</null> if no method found
   * @see #initialise
   */
  private static Method getMethod(final Class _class, final String _method,
                                  final String _type, final String _jaasName,
                                  final long _jaasId) {
    Method ret = null;

    if ((_method != null) && (_method.trim().length() > 0)) {
      try {
        ret = _class.getMethod(_method.trim(), new Class[] {});
      } catch (NoSuchMethodException e) {
        LOG.error("could not get a " + _type + " method for " + "JAAS System '"
            + _jaasName + "' (id = " + _jaasId + ")", e);
      } catch (SecurityException e) {
        LOG.error("could not get a " + _type + " method for " + "JAAS System '"
            + _jaasName + "' (id = " + _jaasId + ")", e);
      }
      if (!ret.getReturnType().equals(String.class)) {
        LOG.error("could not get a " + _type + " method returning "
            + "java.lang.String for JAAS System '" + _jaasName + "' "
            + "(id = " + _jaasId + ")");
        ret = null;
      } else if ((ret.getParameterTypes() != null)
          && (ret.getParameterTypes().length > 0)) {
        LOG.error("could not get a " + _type + " method returning "
            + "java.lang.String for JAAS System '" + _jaasName + "' "
            + "(id = " + _jaasId + ")");
        ret = null;
      }
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link JAASSystem}.
   * 
   * @param _id
   *          id to search in the cache
   * @return instance of class {@link JAASSystem}
   */
  public static JAASSystem getJAASSystem(final long _id) {
    return cache.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link JAASSystem}.
   * 
   * @param _name
   *          name to search in the cache
   * @return instance of class {@link JAASSystem}
   */
  public static JAASSystem getJAASSystem(final String _name) {
    return cache.get(_name);
  }

  /**
   * Returns all cached JAAS system in a set.
   * 
   * @return set of all loaded and cached JAAS systems
   */
  public static Set<JAASSystem> getAllJAASSystems() {
    return cache.getAllJAASSystems();
  }

  // ///////////////////////////////////////////////////////////////////////////

  private final static class JAASSystemCache extends Cache<JAASSystem> {

    JAASSystemCache() {
      super(new CacheReloadInterface() {
        public int priority() {
          return CacheReloadInterface.Priority.JAASSystem.number;
        };

        public void reloadCache() throws CacheReloadException {
          JAASSystem.initialise();
        };
      });
    }

    /**
     * Returns all cached JAAS system in a set.
     * 
     * @return set of all loaded and cached JAAS systems
     */
    public Set<JAASSystem> getAllJAASSystems() {
      Set<JAASSystem> ret = new HashSet<JAASSystem>();
      for (Map.Entry<Long, JAASSystem> entry : getCache4Id().entrySet()) {
        ret.add(entry.getValue());
      }
      return ret;
    }

  };

}

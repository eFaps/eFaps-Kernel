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

package org.efaps.admin.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class Person extends UserObject implements CacheInterface  {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(Person.class);

  /**
   * The constructor creates a new instance of class {@link Person} and sets
   * the {@link #name} and {@link #id}.
   *
   * @param _id     id of the person to set
   * @param _name   name of the person to set
   */
  private Person(final long _id, final String _name)  {
    super(_id, _name);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Checks, if the given person is assigned to this user object. Here it is
   * only test if the person is the same as the user of the parameter.
   *
   * @param _person person to test
   * @return <i>true</i> if the person is the same person as this person,
   *         otherwise <i>false</i>
   */
  public boolean hasChildPerson(final Person _person)  {
    return (_person.getId() == getId());
  }

  public String getViewableName(final Context _context)  {
    return getName();
  }

  public String toString()  {
    return "[" + super.toString()
      + " id=" + getId() + "]";
  }

  /**
   * Add a role to this person.
   *
   * @param _role
   * @see #roles
   * @see #getRoles
   */
  private void add(final Role _role)  {
    getRoles().add(_role);
  }

  /**
   * Tests, if the given role is assigned to this person.
   *
   * @param _role role to test
   * @return <i>true</i> if role is assigned to this person, otherwise
   *         <i>false</i>
   */
  public boolean isAssigned(final Role _role)  {
    return getRoles().contains(_role);
  }

  /**
   * The instance method checks if the given password is the same password as
   * the password in the database.
   *
   * @param _context  context for this request
   * @param _passwd   password to check for this person
   * @return <i>true</i> if password is correct, otherwise <i>false</i>
   */
  public boolean checkPassword(final Context _context, final String _passwd) throws Exception  {
    boolean ret = false;

    Type type = Type.get("Admin_User_Person");

    Attribute attrPass = type.getAttribute("Password");
    AttributeTypeInterface val = attrPass.newInstance();
    val.set(_context, _passwd);
    String encrPass = val.getViewableString(null);

    PreparedStatement stmt = null;
    try  {
      stmt = _context.getConnection().prepareStatement(
          "select count(*) "+
              "from V_USERPERSON "+
              "where NAME=? and PASSWORD=?");
      stmt.setString(1, getName());
      stmt.setString(2, encrPass);
      ResultSet rs = stmt.executeQuery();
      if (rs.next() && (rs.getLong(1) == 1))  {
        ret = true;
      }
      rs.close();
    } catch (Exception e)  {
e.printStackTrace();
      throw e;
    } finally  {
      if (stmt != null)  {
        stmt.close();
      }
    }

    return ret;
  }

  /**
   * The instance method sets the new password for the current context user.
   * Before the new password is set, some checks are made.
   *
   * @param _context    context for this request
   * @param _newPasswd  new password to set for this user
   */
  public void setPassword(final Context _context, final String _newPasswd) throws Exception  {
    Type type = Type.get("Admin_User_Person");

    if (_newPasswd.length() == 0)  {
      throw new EFapsException(getClass(), "PassWordLength", 1, _newPasswd.length());
    }
    Attribute attrPass = type.getAttribute("Password");
    Update update = new Update(_context, type, "" + getId());
    update.add(_context, attrPass, _newPasswd);
    update.execute(_context);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method reads all information from the database.
   *
   * @param _context  context for this request
   * @see #readRoles4Persons
   */
  protected void readFromDB(final Context _context) throws SQLException  {
    readFromDBAttributes(_context);
//    readFromDBRoles(_context);
  }

  private void readFromDBAttributes(final Context _context) throws SQLException  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select " +
              "V_USERPERSON.EMAIL," +
              "V_USERPERSON.FIRSTNAME," +
              "V_USERPERSON.LASTNAME," +
              "V_USERPERSON.ORG," +
              "V_USERPERSON.URL," +
              "V_USERPERSON.PHONE," +
              "V_USERPERSON.FAX " +
          "from V_USERPERSON " +
          "where V_USERPERSON.ID=" + getId()
      );
      if (rs.next())  {
        setEmail(rs.getString(1));
        setFirstName(rs.getString(2));
        setLastName(rs.getString(3));
        setOrganisation(rs.getString(4));
        setURL(rs.getString(5));
        setPhone(rs.getString(6));
        setFAX(rs.getString(7));
      }
    } finally  {
      stmt.close();
    }
  }

/*  private void readFromDBRoles(final Context _context) throws SQLException  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select " +
              "V_USERPERSON2ROLE.USERABSTRACTTO " +
          "from V_USERPERSON2ROLE " +
          "where V_USERPERSON2ROLE.USERABSTRACTFROM=" + getId()
      );
      while (rs.next())  {
        Role role = Role.get(rs.getLong(1));
        add(role);
      }
      rs.close();
    } finally  {
      stmt.close();
    }
  }
*/

  /**
   * The method reads directly from the database all stores roles for the this
   * person. The found roles are returned as instance of {@link java.util.Set}.
   *
   * @param _context    eFaps context for this request
   * @return set of all found roles for all JAAS systems
   * @see #getRolesFromDB(Context,JAASSystem);
   */
  public Set < Role > getRolesFromDB(
                        final Context _context) throws EFapsException  {

    return getRolesFromDB(_context, null);
  }

  /**
   * The method reads directly from the database all stores roles for the this
   * person. The found roles are returned as instance of {@link java.util.Set}.
   *
   * @param _context    eFaps context for this request
   * @param _jaasSystem JAAS system for which the roles must get from database
   *                    (if value is null, all roles independed from the
   *                    related JAAS system are returned)
   * @return set of all found roles for given JAAS system
   */
  public Set < Role > getRolesFromDB(
                        final Context _context,
                        final JAASSystem _jaasSystem) throws EFapsException  {

    Set < Role > ret = new HashSet < Role > ();
    ConnectionResource rsrc = null;
    try  {
      rsrc = _context.getConnectionResource();

      Statement stmt = null;

      try  {
        StringBuilder cmd = new StringBuilder();
        cmd.append("select ")
           .append(   "USERABSTRACTTO ")
           .append(   "from V_USERPERSON2ROLE ")
           .append(   "where USERABSTRACTFROM=").append(getId());

        if (_jaasSystem != null)  {
          cmd.append(" and JAASSYSID=").append(_jaasSystem.getId());
        }

        stmt = rsrc.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(cmd.toString());
        while (rs.next())  {
          ret.add(Role.get(rs.getLong(1)));
        }
        rs.close();

      } catch (SQLException e)  {
        throw new EFapsException(getClass(), "getRolesFromDB.SQLException",
                                                                e, getName());
      } finally  {
        try  {
          stmt.close();
        } catch (SQLException e)  {
        }
      }

      rsrc.commit();
    } finally  {
      if ((rsrc != null) && rsrc.isOpened())  {
        rsrc.abort();
      }
    }
    return ret;
  }

// TODO: update database with depending roles
public void setRoles(final Context _context, final JAASSystem _jaasSystem, final Set < Role > _roles)  {
  for (Role role : _roles)  {
    add(role);
  }
}

  /////////////////////////////////////////////////////////////////////////////

  /**
   * @see #getEmail
   * @see #setEmail
   */
  private String  email;

  /**
   * The instance variable stores the first name of the person.
   *
   * @see #getFirstName
   * @see #setFirstName
   */
  private String firstName = null;

  /**
   * The instance variable stores the last name of the person.
   *
   * @see #getLastName
   * @see #setLastName
   */
  private String lastName = null;

  /**
   * @see #getOrganisation
   * @see #setOrganisation
   */
  private String  org;

  /**
   * @see #getPhone
   * @see #setPhone
   */
  private String  phone;

  /**
   * @see #getFAX
   * @see #setFAX
   */
  private String  fax;

  /**
   * @see #getURL
   * @see #setURL
   */
  private String  url;

  /**
   * HashSet instance variale to hold all roles for this person.
   *
   * @see #getRoles
   * @see #add(Role)
   */
  private Set < Role > roles = new HashSet < Role > ();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #email}.
   *
   * @return the value of the instance variable {@link #email}.
   * @see #email
   * @see #setEmail
   */
  public String getEmail()  {
    return this.email;
  }

  private void setEmail(final String _email)  {
    this.email = _email;
  }

  /**
   * This is the getter method for instance variable {@link #firstName}.
   *
   * @return the value of the instance variable {@link #firstName}.
   * @see #firstName
   * @see #setFirstName
   */
  public String getFirstName()  {
    return this.firstName;
  }

  /**
   * This is the setter method for instance variable {@link #firstName}.
   *
   * @param _firstName new value for instance variable {@link #firstName}.
   * @see #firstName
   * @see #getFirstName
   */
  private void setFirstName(final String _firstName)  {
    this.firstName = _firstName;
  }

  /**
   * This is the getter method for instance variable {@link #lastName}.
   *
   * @return the value of the instance variable {@link #lastName}.
   * @see #lastName
   * @see #setLastName
   */
  public String getLastName()  {
    return this.lastName;
  }

  /**
   * This is the setter method for instance variable {@link #lastName}.
   *
   * @param _lastName new value for instance variable {@link #lastName}.
   * @see #lastName
   * @see #getLastName
   */
  private void setLastName(final String _lastName)  {
    this.lastName = _lastName;
  }

  /**
   * This is the getter method for instance variable {@link #org}.
   *
   * @return the value of the instance variable {@link #org}.
   * @see #org
   * @see #setOrganisation
   */
  public String getOrganisation()  {
    return this.org;
  }

  private void setOrganisation(final String _org)  {
    this.org = _org;
  }

  /**
   * This is the getter method for instance variable {@link #phone}.
   *
   * @return the value of the instance variable {@link #phone}.
   * @see #phone
   * @see #setPhone
   */
  public String getPhone()  {
    return this.phone;
  }

  private void setPhone(final String _phone)  {
    this.phone = _phone;
  }

  /**
   * This is the getter method for instance variable {@link #fax}.
   *
   * @return the value of the instance variable {@link #fax}.
   * @see #fax
   * @see #setFax
   */
  public String getFAX()  {
    return this.fax;
  }

  private void setFAX(final String _fax)  {
    this.fax = _fax;
  }

  /**
   * This is the getter method for instance variable {@link #url}.
   *
   * @return the value of the instance variable {@link #url}.
   * @see #url
   * @see #setUrl
   */
  public String getURL()  {
    return this.url;
  }

  private void setURL(final String _url)  {
    this.url = _url;
  }

  /**
   * Getter method for HashSet instance variable {@link #roles}.
   *
   * @return value of instance variable {@link #roles}
   * @see #roles
   * @see #add(Role)
   */
  public Set < Role > getRoles()  {
    return this.roles;
  }

  /////////////////////////////////////////////////////////////////////////////

 /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Person}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Person}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  static public Person get(final long _id) throws EFapsException  {
    Person ret = getCache().get(_id);
    if (ret == null)  {
      Context context = new Context();
      try  {
        ret = getCache().readPerson(context,
            "select " +
              "V_USERPERSON.ID," +
              "V_USERPERSON.NAME " +
            "from V_USERPERSON " +
            "where V_USERPERSON.ID=" + _id
        );
      } finally  {
        context.close();
      }
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Person}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Person}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  static public Person get(final String _name) throws EFapsException  {
    Person ret = getCache().get(_name);
    if (ret == null)  {
      Context context = new Context();
      try  {
        ret = getCache().readPerson(context,
            "select " +
              "V_USERPERSON.ID," +
              "V_USERPERSON.NAME " +
            "from V_USERPERSON " +
            "where V_USERPERSON.NAME='" + _name + "'"
        );
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
  static public PersonCache getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link Person}.
   *
   * @see #getCache
   */
  static private PersonCache cache = new PersonCache();

  /////////////////////////////////////////////////////////////////////////////

  static protected class PersonCache extends Cache < Person >  {

    private Person readPerson(final Context _context, final String _sql) throws EFapsException  {
      Statement stmt = null;
      Person ret = null;
      try  {
        stmt = _context.getConnection().createStatement();;
        ResultSet rs = stmt.executeQuery(_sql);
        if (rs.next())  {
          long id =     rs.getLong(1);
          String name = rs.getString(2);
          ret = new Person(id, name.trim());
          this.add(ret);
          ret.readFromDB(_context);
        }
        rs.close();
      } catch (SQLException e)  {
// TODO: throw of EFapsException
        LOG.warn("close of SQL statement not possible", e);
      } finally  {
        if (stmt != null)  {
          try  {
            stmt.close();
          } catch (SQLException e)  {
            LOG.warn("close of SQL statement not possible", e);
          }
        }
      }
      return ret;
    }
  }
}

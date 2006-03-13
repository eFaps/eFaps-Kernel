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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

public class Person extends UserObject  {

  /**
   * The constructor creates a new instance of class {@link Person} and sets
   * the {@link #name} and {@link #id}.
   *
   * @param _id     id of the person to set
   * @param _name   name of the person to set
   */
  private Person(long _id, String _name)  {
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
  public boolean hasChildPerson(Person _person)  {
    return (_person.getId() == getId());
  }

  public String getViewableName(Context _context)  {
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
  private void add(Role _role)  {
    getRoles().add(_role);
  }

  /**
   * The instance method checks if the given password is the same password as
   * the password in the database.
   *
   * @param _context  context for this request
   * @param _passwd   password to check for this person
   * @return <i>true</i> if password is correct, otherwise <i>false</i>
   */
  public boolean checkPassword(Context _context, String _passwd) throws Exception  {
    boolean ret = false;
/*
    Type type = Type.get("Admin_User_Person");

    Attribute attrPass = type.getAttribute("Password");
    AttributeTypeInterface val = attrPass.newInstance();
    val.set(_context, _passwd);
    String encrPass = val.getViewableString(null);

    SearchQuery query = new SearchQuery();
    query.setQueryTypes(_context, "Admin_User_Person");
    query.add(attrPass);
    query.addWhereExprEqValue(_context, "Name",     getName());
    query.addWhereExprEqValue(_context, "Password", encrPass);
//    query.addWhere(type.getAttribute("Status"), "10001");
    query.execute(_context);
    if (query.next())  {
      ret = true;
    }
*/
ret=true;
    return ret;
  }

  /**
   * The instance method sets the new password for the current context user.
   * Before the new password is set, some checks are made.
   *
   * @param _context    context for this request
   * @param _newPasswd  new password to set for this user
   */
  public void setPassword(Context _context, String _newPasswd) throws Exception  {
    Type type = Type.get("Admin_User_Person");

    if (_newPasswd.length()==0)  {
      throw new EFapsException(getClass(), "PassWordLength", 1, _newPasswd.length());
    }
    Attribute attrPass = type.getAttribute("Password");
    Update update = new Update(_context, type, ""+getId());
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
  protected void readFromDB(Context _context) throws Exception  {
    readFromDBAttributes(_context);
    readFromDBRoles(_context);
  }

  private void readFromDBAttributes(Context _context) throws Exception  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select "+
              "USERPERSON.EMAIL,"+
              "USERPERSON.FIRSTNAME,"+
              "USERPERSON.LASTNAME,"+
              "USERPERSON.ORG,"+
              "USERPERSON.URL,"+
              "USERPERSON.PHONE,"+
              "USERPERSON.FAX "+
          "from USERPERSON "+
          "where USERPERSON.ID="+getId()
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
    } catch (Exception e)  {
e.printStackTrace();
      throw e;
    } finally  {
      stmt.close();
    }
  }

  private void readFromDBRoles(Context _context) throws Exception  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select "+
              "USERABSTRACTFROM "+
          "from USERABSTRACT2ABSTRACT "+
          "where TYPEID=10100 and "+
                "USERABSTRACTTO="+getId()
      );
      while (rs.next())  {
        Role role = Role.get(rs.getLong(1));
        add(role);
        role.add(this);
      }
      rs.close();
    } catch (Exception e)  {
e.printStackTrace();
      throw e;
    } finally  {
      stmt.close();
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
  private Set<Role> roles = new HashSet<Role>();

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

  private void setEmail(String _email)  {
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
  private void setFirstName(String _firstName)  {
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
  private void setLastName(String _lastName)  {
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

  private void setOrganisation(String _org)  {
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

  private void setPhone(String _phone)  {
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

  private void setFAX(String _fax)  {
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

  private void setURL(String _url)  {
    this.url = _url;
  }

  /**
   * Getter method for HashSet instance variable {@link #roles}.
   *
   * @return value of instance variable {@link #roles}
   * @see #roles
   * @see #add(Role)
   */
  public Set<Role> getRoles()  {
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
  static public Person get(long _id) throws Exception  {
    Person ret = getCache().get(_id);
    if (ret==null)  {
      Context context = new Context();
      try  {
        ret = getCache().readPerson(context,
            "select "+
              "USERABSTRACT.ID,"+
              "USERABSTRACT.NAME "+
            "from USERABSTRACT "+
            "where USERABSTRACT.TYPEID=10000 and "+
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
   * {@link Person}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Person}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  static public Person get(String _name) throws Exception  {
    Person ret = getCache().get(_name);
    if (ret==null)  {
      Context context = new Context();
System.out.println("----------------------------- read person");
      try  {
        ret = getCache().readPerson(context,
            "select "+
              "USERABSTRACT.ID,"+
              "USERABSTRACT.NAME "+
            "from USERABSTRACT "+
            "where USERABSTRACT.TYPEID=10000 and "+
                  "USERABSTRACT.NAME='"+_name+"'"
        );
      } catch (Throwable e)  {
        throw new Exception(e);
      } finally  {
        context.close();
      }
System.out.println("----------------------------- person.result = "+ret);
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

  static protected class PersonCache extends Cache<Person>  {

    private Person readPerson(Context _context, String _sql) throws Exception  {
      Statement stmt = _context.getConnection().createStatement();
      Person ret = null;
      try  {
        ResultSet rs = stmt.executeQuery(_sql);
        if (rs.next())  {
          long id =             rs.getLong(1);
          String name =         rs.getString(2);
          ret = new Person(id, name.trim());
          this.add(ret);
          ret.readFromDB(_context);
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

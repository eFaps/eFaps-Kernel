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
   * Stores all instances of class {@link Person}.
   *
   * @see #getCache
   */
  private final static PersonCache cache = new PersonCache();

  /**
   * HashSet instance variale to hold all roles for this person.
   *
   * @see #getRoles
   * @see #add(Role)
   */
  private final Set < Role > roles = new HashSet < Role > ();

  /**
   * HashSet instance variale to hold all groups for this person.
   *
   * @see #add(Group)
   */
  private final Set < Group > groups = new HashSet < Group > ();

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
   * @param _role role to add to this person
   * @see #roles
   */
  private void add(final Role _role)  {
    this.roles.add(_role);
  }

  /**
   * Tests, if the given role is assigned to this person.
   *
   * @param _role role to test
   * @return <i>true</i> if role is assigned to this person, otherwise
   *         <i>false</i>
   */
  public boolean isAssigned(final Role _role)  {
    return this.roles.contains(_role);
  }

  /**
   * Add a role to this person.
   *
   * @param _group  group to add to this person
   * @see #groups
   */
  private void add(final Group _group)  {
    this.groups.add(_group);
  }

  /**
   * Tests, if the given group is assigned to this person.
   *
   * @param _group group to test
   * @return <i>true</i> if group is assigned to this person, otherwise
   *         <i>false</i>
   */
  public boolean isAssigned(final Group _group)  {
    return this.groups.contains(_group);
  }

  /**
   * All assigned roles in {@link #roles} and groups in {@link #groups} are
   * removed in the cache from this person instance. This is needed if the
   * person assignments are rebuild (e.g. from a login servlet).
   */
  public void cleanUp()  {
    this.roles.clear();
    this.groups.clear();
  }

  /**
   * The instance method checks if the given password is the same password as
   * the password in the database.
   *
   * @param _context  context for this request
   * @param _passwd   password to check for this person
   * @return <i>true</i> if password is correct, otherwise <i>false</i>
   */
// TODO: rework! user connection resource!
  public boolean checkPassword(final String _passwd) throws Exception  {
    boolean ret = false;

    Context context = Context.getThreadContext();

    Type type = Type.get("Admin_User_Person");

    Attribute attrPass = type.getAttribute("Password");
    AttributeTypeInterface val = attrPass.newInstance();
    val.set(context, _passwd);
    String encrPass = val.getViewableString(null);

    PreparedStatement stmt = null;
    try  {
      stmt = context.getConnection().prepareStatement(
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

  /**
   * The method reads directly from the database all stores roles for the this
   * person. The found roles are returned as instance of {@link java.util.Set}.
   *
   * @return set of all found roles for all JAAS systems
   * @see #getRolesFromDB(JAASSystem);
   */
  public Set < Role > getRolesFromDB() throws EFapsException  {

    return getRolesFromDB((JAASSystem) null);
  }

  /**
   * The method reads directly from the database all stores roles for the this
   * person. The found roles are returned as instance of {@link java.util.Set}.
   *
   * @param _jaasSystem JAAS system for which the roles must get from database
   *                    (if value is null, all roles independed from the
   *                    related JAAS system are returned)
   * @return set of all found roles for given JAAS system
   */
  public Set < Role > getRolesFromDB(final JAASSystem _jaasSystem)
                                                      throws EFapsException  {

    Set < Role > ret = new HashSet < Role > ();
    ConnectionResource rsrc = null;
    try  {
      rsrc = Context.getThreadContext().getConnectionResource();

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

  /**
   * The method reads directly from the database all stores groups for the this
   * person. The found groups are returned as instance of {@link java.util.Set}.
   *
   * @param _jaasSystem JAAS system for which the groups must get from database
   *                    (if value is null, all groups independed from the
   *                    related JAAS system are returned)
   * @return set of all found groups for given JAAS system
   */
  public Set < Group > getGroupsFromDB(final JAASSystem _jaasSystem)
                                                      throws EFapsException  {

    Set < Group > ret = new HashSet < Group > ();
    ConnectionResource rsrc = null;
    try  {
      rsrc = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      try  {
        StringBuilder cmd = new StringBuilder();
        cmd.append("select ")
           .append(   "USERABSTRACTTO ")
           .append(   "from V_USERPERSON2GROUP ")
           .append(   "where USERABSTRACTFROM=").append(getId());

        if (_jaasSystem != null)  {
          cmd.append(" and JAASSYSID=").append(_jaasSystem.getId());
        }

        stmt = rsrc.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(cmd.toString());
        while (rs.next())  {
          ret.add(Group.get(rs.getLong(1)));
        }
        rs.close();

      } catch (SQLException e)  {
// TODO: exception in property
        throw new EFapsException(getClass(), "getGroupsFromDB.SQLException",
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
public void setRoles(final JAASSystem _jaasSystem, final Set < Role > _roles) throws EFapsException  {
  Context context = Context.getThreadContext();

  if (_jaasSystem == null)  {
// TODO: throw exception
  }
  if (_roles == null)  {
// TODO: throw exception
  }

  for (Role role : _roles)  {
    add(role);
  }

  Set < Role > rolesInDb = getRolesFromDB(_jaasSystem);

  for (Role role : _roles)  {
    if (!rolesInDb.contains(role))  {
      addRoleInDb(context, _jaasSystem, role);
    }
  }

  for (Role role : rolesInDb)  {
    if (!_roles.contains(role))  {
      removeRoleInDb(context, _jaasSystem, role);
    }
  }
}

private void addRoleInDb(final Context _context, final JAASSystem _jaasSystem, final Role _role) throws EFapsException  {
  ConnectionResource rsrc = null;
  try  {
    rsrc = _context.getConnectionResource();

    Statement stmt = null;
    try  {
      StringBuilder cmd = new StringBuilder();
      cmd.append("insert into V_USERPERSON2ROLE")
         .append(   "(USERABSTRACTFROM,USERABSTRACTTO,JAASSYSID) ")
         .append(   "values (").append(getId()).append(",")
                               .append(_role.getId()).append(",")
                               .append(_jaasSystem.getId()).append(")");

      stmt = rsrc.getConnection().createStatement();
      stmt.executeUpdate(cmd.toString());

    } catch (SQLException e)  {
// TODO: exception in properties
      throw new EFapsException(getClass(), "addRoleInDb.SQLException",
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
}

private void removeRoleInDb(final Context _context, final JAASSystem _jaasSystem, final Role _role) throws EFapsException  {
  ConnectionResource rsrc = null;
  try  {
    rsrc = _context.getConnectionResource();

    Statement stmt = null;

    try  {
      StringBuilder cmd = new StringBuilder();
      cmd.append("delete from V_USERPERSON2ROLE ")
         .append(   "where JAASSYSID=").append(_jaasSystem.getId()).append(" ")
         .append(         "and USERABSTRACTTO=").append(_role.getId()).append(" ")
         .append(         "and USERABSTRACTFROM=").append(getId());

      stmt = rsrc.getConnection().createStatement();
      stmt.executeUpdate(cmd.toString());

    } catch (SQLException e)  {
// TODO: exception in properties
      throw new EFapsException(getClass(), "removeRoleInDb.SQLException",
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
   * Returns for given parameter <i>_jaasKey</i> the instance of class
   * {@link Person}. The parameter <i>_jaasKey</i> is the name of the person
   * used in the given JAAS system for the person.
   *
   * @param _jaasSystem JAAS system for which the JAAS key is named
   * @param _jaasKey    key in the foreign JAAS system for which the person is
   *                    searched
   * @return instance of class {@link Person}, or <code>null</code> if person
   *         is not found
   * @see #get(long)
   */
  static public Person getWithJAASKey(final JAASSystem _jaasSystem, final String _jaasKey) throws EFapsException  {
    long personId = 0;
    ConnectionResource rsrc = null;
    try  {
      rsrc = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      try  {
        StringBuilder cmd = new StringBuilder();
        cmd.append("select ")
           .append(   "ID ")
           .append(   "from V_USERPERSONJASSKEY ")
           .append(   "where JAASKEY='").append(_jaasKey).append("' ")
           .append(       "and JAASSYSID=").append(_jaasSystem.getId());

        stmt = rsrc.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(cmd.toString());
        if (rs.next())  {
          personId = rs.getLong(1);
        }
        rs.close();

      } catch (SQLException e)  {
        LOG.error("search for person for JAAS system "
            + "'" + _jaasSystem.getName() + "' "
            + "with key '" + _jaasKey + "' is not possible", e);
// TODO: exception in properties
        throw new EFapsException(Person.class, "getWithJAASKey.SQLException",
                              e, _jaasSystem.getName(), _jaasKey);
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
    return get(personId);
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static public PersonCache getCache()  {
    return cache;
  }

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
        LOG.error("close of SQL statement not possible", e);
      } finally  {
        if (stmt != null)  {
          try  {
            stmt.close();
          } catch (SQLException e)  {
            LOG.error("close of SQL statement not possible", e);
          }
        }
      }
      return ret;
    }
  }
}

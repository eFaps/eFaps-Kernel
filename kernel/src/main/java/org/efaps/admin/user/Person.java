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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.Update;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class Person extends UserObject implements CacheInterface  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(Person.class);

  /**
   * Stores all instances of class {@link Person}.
   *
   * @see #getCache
   */
  private final static Cache < Person > cache = new Cache < Person > ();

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

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
   * The email adresse of this person is stored in this instance variable.
   *
   * @see #getEmail
   * @see #setEmail
   */
  private String  email = null;

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
   * The organisation of this person is store in this instance variable.
   *
   * @see #getOrganisation
   * @see #setOrganisation
   */
  private String organisation = null;

  /**
   * The office phone number of this person is stored in this instance
   * variable.
   *
   * @see #getPhone
   * @see #setPhone
   */
  private String phone = null;

  /**
   * The office fax number of this person is stored in this instance variable.
   *
   * @see #getFAX
   * @see #setFAX
   */
  private String fax = null;

  /**
   * The url of this person is stored in this instance variable.
   *
   * @see #getURL
   * @see #setURL
   */
  private String url = null;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

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
  // methods

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
  public boolean checkPassword(final String _passwd) throws EFapsException  {
    boolean ret = false;
    ConnectionResource rsrc = null;
    try  {
      Context context = Context.getThreadContext();
      rsrc = context.getConnectionResource();

      PreparedStatement stmt = null;

      Type type = Type.get(EFapsClassName.USER_PERSON.name);

      Attribute attrPass = type.getAttribute("Password");
      AttributeTypeInterface val = attrPass.newInstance();
      val.set(context, _passwd);
      String encrPass = val.getViewableString(null);

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
      } catch (SQLException e)  {
        LOG.error("password check failed for person '" + getName() + "'", e);
// TODO: Exception in properties
        throw new EFapsException(getClass(), "checkPassword.SQLException",
                                                                e, getName());
      } finally  {
        try  {
          if (stmt != null)  {
            stmt.close();
          }
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
   * The instance method sets the new password for the current context user.
   * Before the new password is set, some checks are made.
   *
   * @param _context    context for this request
   * @param _newPasswd  new password to set for this user
   */
  public void setPassword(final Context _context, final String _newPasswd) throws Exception  {
    Type type = Type.get(EFapsClassName.USER_PERSON.name);

    if (_newPasswd.length() == 0)  {
      throw new EFapsException(getClass(), "PassWordLength", 1, _newPasswd.length());
    }
    Attribute attrPass = type.getAttribute("Password");
    Update update = new Update(_context, type, "" + getId());
    update.add(_context, attrPass, _newPasswd);
    update.execute(_context);
  }

  /**
   * Assign this person to the given JAAS system under the given JAAS key.
   *
   * @param _jaasSystem   JAAS system to which the person is assigned
   * @param _jaasKey      key under which the person is know in the JAAS system
   */
  public void assignToJAASSystem(final JAASSystem _jaasSystem,
                                 final String _jaasKey) throws EFapsException  {

    ConnectionResource rsrc = null;
    try  {
      Context context = Context.getThreadContext();
      rsrc = context.getConnectionResource();
      Type keyType = Type.get(EFapsClassName.USER_JAASKEY.name);

      PreparedStatement stmt = null;
      try  {
        StringBuilder cmd = new StringBuilder();

        long keyId = 0;
        if (!rsrc.getConnection().getMetaData().supportsGetGeneratedKeys())  {
          keyId = context.getDbType().getNewId(rsrc.getConnection(),
                          keyType.getMainTable().getSqlTable(), "ID");
          cmd.append("insert into ").append(keyType.getMainTable().getSqlTable())
             .append(   "(ID,KEY,CREATOR,CREATED,MODIFIER,MODIFIED,USERABSTRACT,USERJAASSYSTEM) ")
             .append(   "values (").append(keyId).append(",");
        } else  {
          cmd.append("insert into ").append(keyType.getMainTable().getSqlTable())
             .append(   "(KEY,CREATOR,CREATED,MODIFIER,MODIFIED,USERABSTRACT,USERJAASSYSTEM) ")
             .append(   "values (");
        }
        cmd
           .append("'").append(_jaasKey).append("',")
           .append("1,")
           .append(Context.getDbType().getCurrentTimeStamp()).append(",")
           .append("1,")
           .append(Context.getDbType().getCurrentTimeStamp()).append(",")
           .append(getId()).append(",")
           .append(_jaasSystem.getId()).append(")");
        stmt = rsrc.getConnection().prepareStatement(cmd.toString());
        int rows = stmt.executeUpdate();
        if (rows == 0)  {
// TODO: exception in properties
          LOG.error("could not execute '" + cmd.toString() + "' "
                  + "for JAAS system " + "'" + _jaasSystem.getName() + "' "
                  + "person with key '" + _jaasKey + "' and "
                  + "user name '" + getName() + "'");
          throw new EFapsException(Person.class,
                  "assignToJAASSystem.NotInserted",
                  _jaasSystem.getName(), _jaasKey, getName());
        }
      } catch (SQLException e)  {
  // TODO: exception in properties
        LOG.error("could not assign for JAAS system "
                + "'" + _jaasSystem.getName() + "' person with key "
                + "'" + _jaasKey + "' and user name '" + getName() + "'", e);
        throw new EFapsException(Person.class,
                "assignToJAASSystem.SQLException",
                e, _jaasSystem.getName(), _jaasKey, getName());
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
   * The instance method reads all information from the database.
   *
   * @see #readFromDBAttributes
   */
  protected void readFromDB() throws EFapsException  {
    readFromDBAttributes();
  }

  /**
   * All attributes from this person are read from the database.
   *
   * @throws EFapsException if the attributes for this person could not be
   *         read
   */
  private void readFromDBAttributes() throws EFapsException  {
    ConnectionResource rsrc = null;
    try  {
      rsrc = Context.getThreadContext().getConnectionResource();
      Statement stmt = null;
      try  {
        stmt = rsrc.getConnection().createStatement();
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
        rs.close();
      } catch (SQLException e)  {
        LOG.error("read attributes for person with SQL statement is not "
                  + "possible", e);
// TODO: exception in properties
        throw new EFapsException(Person.class,
                "readFromDBAttributes.SQLException", e, getName(), getId());
      } finally  {
        try  {
          if (stmt != null)  {
            stmt.close();
          }
        } catch (SQLException e)  {
          LOG.error("close of SQL statement is not possible", e);
        }
      }
      rsrc.commit();
    } finally  {
      if ((rsrc != null) && rsrc.isOpened())  {
        rsrc.abort();
      }
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

// TODO: Description
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

// TODO: Description
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

// TODO: Description
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

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

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

  /**
   * This is the setter method for instance variable {@link #email}.
   *
   * @param _lastName new value for instance variable {@link #email}.
   * @see #email
   * @see #getEmail
   */
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
   * This is the getter method for instance variable {@link #organisation}.
   *
   * @return the value of the instance variable {@link #organisation}.
   * @see #organisation
   * @see #setOrganisation
   */
  public String getOrganisation()  {
    return this.organisation;
  }

  /**
   * This is the setter method for instance variable {@link #organisation}.
   *
   * @param _lastName new value for instance variable {@link #organisation}.
   * @see #organisation
   * @see #getOrganisation
   */
  private void setOrganisation(final String _organisation)  {
    this.organisation = _organisation;
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

  /**
   * This is the setter method for instance variable {@link #phone}.
   *
   * @param _lastName new value for instance variable {@link #phone}.
   * @see #phone
   * @see #getPhone
   */
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

  /**
   * This is the setter method for instance variable {@link #fax}.
   *
   * @param _lastName new value for instance variable {@link #fax}.
   * @see #fax
   * @see #getFAX
   */
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

  /**
   * This is the setter method for instance variable {@link #url}.
   *
   * @param _lastName new value for instance variable {@link #url}.
   * @see #url
   * @see #getURL
   */
  private void setURL(final String _url)  {
    this.url = _url;
  }

  /**
   * Returns a string representation of this person.
   *
   * @return string representation of this person
   */
  public String toString()  {
    return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("email",        this.email)
        .append("first name",   this.firstName)
        .append("last name",    this.lastName)
        .append("organisation", this.organisation)
        .append("phone",        this.phone)
        .append("fax",          this.fax)
        .append("url",          this.url)
        .append("roles",        this.roles)
        .append("groups",       this.groups)
        .toString();
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Person}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Person}
   * @see #cache
   * @see #getFromDB
   */
  public static Person get(final long _id) throws EFapsException  {
    Person ret = getCache().get(_id);
    if (ret == null)  {
      ret = getFromDB(
          "select " +
            "V_USERPERSON.ID," +
            "V_USERPERSON.NAME " +
          "from V_USERPERSON " +
          "where V_USERPERSON.ID=" + _id
      );
    }
    return ret;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Person}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Person}
   * @see #cache
   * @see #getFromDB
   */
  public static Person get(final String _name) throws EFapsException  {
    Person ret = getCache().get(_name);
    if (ret == null)  {
      ret = getFromDB(
          "select " +
            "V_USERPERSON.ID," +
            "V_USERPERSON.NAME " +
          "from V_USERPERSON " +
          "where V_USERPERSON.NAME='" + _name + "'"
      );
    }
    return ret;
  }

  /**
   * The static method reads with the help of given sql statement the id and
   * name of the person, creates a new person instance, adds the instance to
   * the cache and read all related information for the person instance with
   * {@link #readFromDB}.
   *
   * @param _sql  sql statement used to get the person from database
   * @return person instance with the found values from database
   * @see #get(long)
   * @see #get(String)
   * @see #readFromDB
   */
  private static Person getFromDB(final String _sql) throws EFapsException  {
    Person ret = null;
    ConnectionResource rsrc = null;
    try  {
      rsrc = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      try  {
        stmt = rsrc.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(_sql);
        if (rs.next())  {
          long id =     rs.getLong(1);
          String name = rs.getString(2);
          ret = new Person(id, name.trim());
          getCache().add(ret);
        }
        rs.close();
      } catch (SQLException e)  {
        LOG.error("search for person with SQL statement "
                + "'" + _sql + "' is not possible", e);
// TODO: exception in properties
        throw new EFapsException(Person.class,
                "getFromDB.SQLException", e, _sql);
      } finally  {
        try  {
          if (stmt != null)  {
            stmt.close();
          }
        } catch (SQLException e)  {
          LOG.error("close of SQL statement is not possible", e);
        }
      }
      rsrc.commit();
    } finally  {
      if ((rsrc != null) && rsrc.isOpened())  {
        rsrc.abort();
      }
    }
    if (ret != null)  {
      ret.readFromDB();
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
  public static Person getWithJAASKey(final JAASSystem _jaasSystem, final String _jaasKey) throws EFapsException  {
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

// TODO: Description
  /**
   *
   * @param _jaasSystem   JAAS system which want to creaet a new person in
   *                      eFaps
   * @param _jaasKey      key of the person in the JAAS system
   * @param _userName     name in the eFaps system (used as proposal, it's
   *                      tested for uniqueness and changed if needed!)
   * @return new created person
   * @throws EFapsException if person not creatable
   * @see #assignToJAASSystem
   */
  public static Person createPerson(final JAASSystem _jaasSystem,
                                    final String _jaasKey,
                                    final String _userName) throws EFapsException  {

    long persId = 0;
    Type persType = Type.get(EFapsClassName.USER_PERSON.name);
    ConnectionResource rsrc = null;
    try  {
      Context context = Context.getThreadContext();

      rsrc = context.getConnectionResource();

      PreparedStatement stmt = null;
      try  {
        StringBuilder cmd = new StringBuilder();

// TODO: check for uniqueness!
// TODO: hard coded mofifier and creator
        if (!rsrc.getConnection().getMetaData().supportsGetGeneratedKeys())  {
          persId = context.getDbType().getNewId(rsrc.getConnection(),
                          persType.getMainTable().getSqlTable(), "ID");
          cmd.append("insert into ").append(persType.getMainTable().getSqlTable())
             .append(   "(ID,TYPEID,NAME,CREATOR,CREATED,MODIFIER,MODIFIED) ")
             .append(   "values (").append(persId).append(",");
        } else  {
          cmd.append("insert into ").append(persType.getMainTable().getSqlTable())
             .append(   "(TYPEID,NAME,CREATOR,CREATED,MODIFIER,MODIFIED) ")
             .append(   "values (");
        }
        cmd
           .append(persType.getId()).append(",")
           .append("'").append(_userName).append("',")
           .append("1,")
           .append(Context.getDbType().getCurrentTimeStamp()).append(",")
           .append("1,")
           .append(Context.getDbType().getCurrentTimeStamp()).append(")");

        stmt = rsrc.getConnection().prepareStatement(cmd.toString());
        int rows = stmt.executeUpdate();
        if (rows == 0)  {
// TODO: exception in properties
          LOG.error("could not execute '" + cmd.toString() + "' "
                  + "for JAAS system " + "'" + _jaasSystem.getName() + "' "
                  + "person with key '" + _jaasKey + "' and "
                  + "user name '" + _userName + "'");
          throw new EFapsException(Person.class, "createPerson.NotInserted",
                                   _jaasSystem.getName(), _jaasKey, _userName);
        }
        if (persId == 0)  {
          ResultSet rs = stmt.getGeneratedKeys();
          if (rs.next())  {
            persId = rs.getLong(1);
          }
        }

        stmt.close();

        cmd = new StringBuilder();
        cmd.append("insert into USERPERSON")
           .append(   "(ID,FIRSTNAME,LASTNAME,EMAIL) ")
           .append(   "values (").append(persId).append(",'-','-','-')");
        stmt = rsrc.getConnection().prepareStatement(cmd.toString());
        rows = stmt.executeUpdate();
        if (rows == 0)  {
          LOG.error("could not execute '" + cmd.toString() + "' "
                  + "for JAAS system " + "'" + _jaasSystem.getName() + "' "
                  + "person with key '" + _jaasKey + "' and "
                  + "user name '" + _userName + "'");
          throw new EFapsException(Person.class, "createPerson.NotInserted",
                                   _jaasSystem.getName(), _jaasKey, _userName);
        }

      } catch (SQLException e)  {
// TODO: exception in properties
        LOG.error("could not create for JAAS system "
                + "'" + _jaasSystem.getName() + "' person with key "
                + "'" + _jaasKey + "' and user name '" + _userName + "'", e);
        throw new EFapsException(Person.class, "createPerson.SQLException",
                          e, _jaasSystem.getName(), _jaasKey, _userName);
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

    Person ret = get(persId);
    ret.assignToJAASSystem(_jaasSystem, _jaasKey);
    return ret;
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  public static Cache < Person > getCache()  {
    return cache;
  }
}

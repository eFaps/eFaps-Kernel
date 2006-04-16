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

package org.efaps.admin.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.efaps.admin.AdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.UserObject;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public abstract class UserInterfaceObject extends AdminObject  {


  /**
   * Constructor to set the id and name of the user interface object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  protected UserInterfaceObject(final long _id, final String _name)  {
    super(_id, _name);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method reads all needed information for this user interface
   * object. Here, only the properties are read from the database
   *
   * @param _context  context for this request
   * @see #readFromDB4Properties
   * @see #readFromDB4Links
   * @see #readFromDB4Access
   */
  protected void readFromDB(final Context _context) throws Exception  {
    readFromDB4Properties(_context);
    readFromDB4Links(_context);
    readFromDB4Access(_context);
  }

  /**
   * Reads all links for this user interface object. Each found link property
   * is set with instance method {@link AdminObject.setLinkProperty}.
   *
   * @param _context eFaps context for this request
   * @see AdminObject.setLinkProperty
   */
  private void readFromDB4Links(final Context _context) throws Exception  {
// folgende aktion funktionier irgendwie nicht unter oracle...
/*    SearchQuery query = new SearchQuery();
    query.setQueryTypes(_context, "Admin_UI_Link");
    query.addWhereExprEqValue(_context, "From", getId());
    query.setExpandChildTypes(true);
    query.addSelect(_context, "To");
    query.addSelect(_context, "To.Type");
    query.addSelect(_context, "To.Name");
    query.addSelect(_context, "Type");
    query.execute(_context);

    while (query.next())  {
      Type   type   = (Type)query.get(_context, "Type");
      long   toId   = (Long)query.get(_context, "To");
      String toName = (String)query.get(_context, "To.Name");
      Type   toType = (Type)query.get(_context, "To.Type");

System.out.println("type="+type);
System.out.println("toId="+toId);
System.out.println("toName="+toName);
System.out.println("toType="+toType);

      setLinkProperty(_context, EFapsClassName.getEnum(type.getName()), toId, EFapsClassName.getEnum(toType.getName()), toName);
    }
*/
Statement stmt = _context.getConnection().createStatement();
try  {
  ResultSet rs = stmt.executeQuery(
      "select "
          + "UIABSTRACT2UIABSTRACT.TYPEID,"
          + "UIABSTRACT2UIABSTRACT.TOID,"
          + "ABSTRACT.TYPEID,"
          + "ABSTRACT.NAME "
      + "from UIABSTRACT2UIABSTRACT, ABSTRACT "
      + "where UIABSTRACT2UIABSTRACT.FROMID=" + getId() + " and UIABSTRACT2UIABSTRACT.TOID=ABSTRACT.ID"
  );
  while (rs.next())  {
    long conTypeId  = rs.getLong(1);
    long toId       = rs.getLong(2);
    long toTypeId   = rs.getLong(3);
    String toName   = rs.getString(4);
    Type conType    = Type.get(conTypeId);
    Type toType     = Type.get(toTypeId);
    if (EFapsClassName.getEnum(conType.getName()) != null)  {
        setLinkProperty(_context,
            EFapsClassName.getEnum(conType.getName()), toId,
            EFapsClassName.getEnum(toType.getName()), toName);
    }
  }
  rs.close();
} catch (Exception e)  {
e.printStackTrace();
  throw e;
} finally  {
  stmt.close();
}
  }

  /**
   * The instance method reads the properties for this user interface object.
   * Each found property is set with instance method {@link AdminObject.setProperty}.
   *
   * @param _context eFaps context for this request
   * @see AdminObject.setProperty
   */
  private void readFromDB4Properties(final Context _context) throws Exception  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select "
              + "PROPERTY.NAME,"
              + "PROPERTY.VALUE "
          + "from PROPERTY "
          + "where PROPERTY.ABSTRACT=" + getId()
      );
      while (rs.next())  {
        String name =   rs.getString(1).trim();
        String value =  rs.getString(2).trim();
        setProperty(_context, name, value);
      }
      rs.close();
    } catch (Exception e)  {
e.printStackTrace();
      throw e;
    } finally  {
      stmt.close();
    }
  }

  /**
   * The instance method reads the access for this user interface object.
   *
   * @param _context for this request
   */
  private void readFromDB4Access(final Context _context) throws Exception  {
    Statement stmt = _context.getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select "
              + "UIACCESS.USERABSTRACT "
          + "from UIACCESS "
          + "where UIACCESS.UIABSTRACT=" + getId()
      );
      while (rs.next())  {
        long userId = rs.getLong(1);
        UserObject userObject = UserObject.getUserObject(_context, userId);
if (userObject == null)  {
throw new Exception("user " + userId + " does not exists!");
} else  {
        getAccess().add(userObject);
}
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
   * Check, if the user of the context has access to this user interface
   * object. If no access user is assigned to this user interface object, all
   * user have access. Otherwise check if the context person is assigned to
   * one of the user objects.
   *
   * @param _context  context for this request (including the person)
   * @return  <i>true</i>if context user has access, otherwise <i>false</i> is
   *          returned
   */
  public boolean hasAccess(final Context _context)  {
    boolean ret = false;

    if (getAccess().isEmpty())  {
      ret = true;
    } else  {
      for (UserObject userObject : getAccess())  {
        if (userObject.isAssigned(_context))  {
          ret = true;
          break;
        }
      }
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable is an Access HashSet to store all users (person,
   * group or role) who have access to this user interface object.
   *
   * @see #getAccess
   */
  private Set < UserObject > access = new HashSet < UserObject >();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Getter method for the HashSet instance variable {@link #access}.
   *
   * @return value of the HashSet instance variable {@link #access}
   * @see #access
   * @see #add(Role)
   */
  protected Set < UserObject > getAccess()  {
    return this.access;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of commands.
   *
   * @param _context  eFaps context for this request
   */
  public static void initialise(final Context _context) throws Exception  {
    Command.getCache().initialise(_context);
    Menu.getCache().initialise(_context);
    Search.getCache().initialise(_context);
    Form.getCache().initialise(_context);
    Table.getCache().initialise(_context);
    for (Command command : Command.getCache().getCache4Id().values())  {
      command.readFromDB(_context);
    }
    for (Menu command : Menu.getCache().getCache4Id().values())  {
      command.readFromDB(_context);
    }
    for (Search command : Search.getCache().getCache4Id().values())  {
      command.readFromDB(_context);
    }
    for (Form command : Form.getCache().getCache4Id().values())  {
      command.readFromDB(_context);
    }
    for (Table command : Table.getCache().getCache4Id().values())  {
      command.readFromDB(_context);
    }
  }


  static protected class UserInterfaceObjectCache < UIObj extends UserInterfaceObject > extends Cache < UIObj >  {

    protected UserInterfaceObjectCache(final Class < UIObj > _callerClass)  {
      this.callerClass = _callerClass;
    }

    /**
     * Initialise the cache of a specific user interface object type.
     * Initialise means, that all all objects of this user interface type are
     * read from the database and stored in the cache. If the eFaps admin type
     * itself is not defined, that initialiase does nothing (this could happen
     * in the create phase).
     *
     * @param _context  eFaps context for this request
     */
    protected void initialise(final Context _context) throws Exception  {
      Class < UIObj > uiObjClass = getCallerClass();
      if (Type.get(getEFapsClassName().name) != null)  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes(_context, getEFapsClassName().name);
        query.addSelect(_context, "ID");
        query.addSelect(_context, "Name");
        query.execute(_context);
        while (query.next())  {
          long id     = (Long) query.get(_context, "ID");
          String name = (String) query.get(_context, "Name");
          UIObj uiObj = uiObjClass.getConstructor(Long.class, String.class).newInstance(id, name);
          add(uiObj);
        }
      }
    }

    protected UIObj read(final Context _context, final long _id) throws EFapsException  {
      try  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes(_context, getEFapsClassName().name);
        query.addWhereExprEqValue(_context, "ID", _id);
        query.addSelect(_context, "ID");
        query.addSelect(_context, "Name");
        return(read(_context, query));
      } catch (EFapsException e)  {
        throw e;
      } catch (Throwable e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.Throwable4Id", e, _id);
      }
    }


    protected UIObj read(final Context _context, final String _name) throws EFapsException  {
      try  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes(_context, getEFapsClassName().name);
        query.addWhereExprEqValue(_context, "Name", _name);
        query.addSelect(_context, "ID");
        query.addSelect(_context, "Name");
        return(read(_context, query));
      } catch (EFapsException e)  {
        throw e;
      } catch (Throwable e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.Throwable4Name", e, _name);
      }
    }

    private EFapsClassName getEFapsClassName() throws EFapsException  {
      Class < UIObj > uiObjClass = getCallerClass();
      try  {
        return ((EFapsClassName)uiObjClass.getField("EFAPS_CLASSNAME").get(null));
      } catch (NoSuchFieldException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "getEFapsClassName.EFapsClassNameNotExist", e, uiObjClass.getName());
      } catch (IllegalAccessException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "getEFapsClassName.EFapsClassNameNotAccessable", e, uiObjClass.getName());
      }
    }

    private UIObj read(final Context _context, final SearchQuery _query) throws EFapsException  {
      UIObj uiObj = null;
      Class < UIObj > uiObjClass = getCallerClass();
      try  {
        _query.execute(_context);
        if (_query.next())  {
          long id     = (Long)_query.get(_context, "ID");
          String name = (String)_query.get(_context, "Name");
          uiObj = uiObjClass.getConstructor(Long.class, String.class).newInstance(id, name);
          add(uiObj);
          uiObj.readFromDB(_context);
        }
      } catch (NoSuchMethodException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.ConstructorNotFound", e, uiObjClass.getName());
      } catch (SecurityException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.ConstructorNotAccessable", e, uiObjClass.getName());
      } catch (IllegalArgumentException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.ConstructorWithCorrectArgumentsNotExists", e, uiObjClass.getName());
      } catch (InstantiationException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.ClassIsNotClass", e, uiObjClass.getName());
      } catch (IllegalAccessException e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.ConstructorNotPublic", e, uiObjClass.getName());
      } catch (InvocationTargetException e)  {
        Throwable t = e.getCause();
        if (t instanceof EFapsException)  {
          throw (EFapsException)t;
        } else  {
          throw new EFapsException(UserInterfaceObjectCache.class, "read.UIObjectNotInstanceable", t, uiObjClass.getName());
        }
      } catch (EFapsException e)  {
        throw e;
      } catch (Throwable e)  {
        throw new EFapsException(UserInterfaceObjectCache.class, "read.Throwable", e);
      } finally  {
        try {_query.close();} catch(Exception e) {}
      }
      return uiObj;
    }

    ///////////////////////////////////////////////////////////////////////////
    // instance variables

    private final Class < UIObj > callerClass;

    ///////////////////////////////////////////////////////////////////////////
    // getter and setter methods

    /**
     *
     */
    private  Class < UIObj > getCallerClass() throws EFapsException  {
      return this.callerClass;
    }

  }
}
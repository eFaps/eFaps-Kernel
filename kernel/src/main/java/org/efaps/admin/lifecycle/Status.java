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

package org.efaps.admin.lifecycle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.user.Role;
import org.efaps.admin.access.AccessType;
import org.efaps.db.Context;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Status extends LifeCycleObject  {

  /**
   * This is the constructor.
   *
   * @param _id     id of the status
   * @param _name   name of the status
   * @param _policy policy of this status
   */
  private Status(long _id, String _name, Policy _policy)  {
    super(_id, _name);
    setPolicy(_policy);
    getPolicy().addStatus(this);
  }

  /**
   * Returns the name of the status.
   *
   * @param _context
   */
  public String getViewableName(Context _context)  {
    return getName();
  }

private void addAccess(Role _role, AccessType _accessType)  {
  Set<AccessType> userAccess = this.access.get(_role);
  if (userAccess == null)  {
    userAccess = new HashSet<AccessType>();
    this.access.put(_role, userAccess);
  }
  userAccess.add(_accessType);
}

Map<Role,Set<AccessType>> access = new Hashtable<Role,Set<AccessType>>();

public boolean checkAccess(Context _context, long _accessType) throws Exception  {
  return checkAccess(_context, AccessType.getAccessType(_accessType));
}

public boolean checkAccess(Context _context, AccessType _accessType)  {
  boolean ret = false;

  for (Map.Entry<Role,Set<AccessType>> entry : this.access.entrySet())   {
    Role role = entry.getKey();
    if (role.isAssigned(_context))  {
      Set<AccessType> userAccess = entry.getValue();
      if (userAccess.contains(_accessType))  {
        ret = true;
        break;
      }
    }
  }

  return ret;
}

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Status}.
   *
   * @return instance of class {@link Status}
   */
  public static Status get(final long _id) throws Exception  {
    Status status = (Status)getCache().get(_id);
    if (status == null)  {
      status = getCache().readStatus(_id);
    }
    return status;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class {@link Status}.
   *
   * @return instance of class {@link Status}
   */
  public static Status get(final String _name) throws Exception  {
    Status status = (Status)getCache().get(_name);
    if (status == null)  {
      status = getCache().readStatus(_name);
    }
    return status;
  }

  /**
   * Static getter method for the status hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static public StatusCache getCache()  {
    return cache;
  }

  /**
   * Stores all instances of status.
   *
   * @see #get
   */
  static private StatusCache cache = new StatusCache();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method reads the access rights for this status.
   *
   */
  private void readDBAccess() throws Exception  {
    Statement stmt = Context.getThreadContext().getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select "+
            "USERABSTRACT,"+
            "LCACCESSTYPE "+
          "from LCSTATUSACCESS "+
          "where LCSTATUS="+getId()
      );
      while (rs.next())  {
        long userId =       rs.getLong(1);
        long accessTypeId = rs.getLong(2);
        addAccess(Role.get(userId), AccessType.getAccessType(accessTypeId));
      }
      rs.close();
    } catch (Exception e)  {
e.printStackTrace();
    } finally  {
      stmt.close();
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  private Policy policy = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Setter method for instance variable {@link #policy}.
   *
   * @see #name
   * @see #getName
   */
  private void setPolicy(final Policy _policy)  {
    this.policy = _policy;
  }

  /**
   * Getter method for instance variable {@link #policy}.
   *
   * @see #policy
   * @see #setPolicy
   */
  public Policy getPolicy()  {
    return this.policy;
  }

  /////////////////////////////////////////////////////////////////////////////

/*  static protected class StatusAccess  {

    StatusAccess(Role _role, AccessType _accessType)  {
      setRole(_role);
      setAccessType(_accessType);
    }

    Role role;
    AccessType accessType;

    void setRole(Role _role)  {
      this.role = _role;
    }

    void setAccessType(AccessType _accessType)  {
      this.accessType = _accessType;
    }
  }
*/
  /////////////////////////////////////////////////////////////////////////////

  static protected class StatusCache extends Cache < Status >  {

    protected StatusCache()  {
      super(new CacheReloadInterface()  {
        public int priority()  {return 1200;};
        public void reloadCache() throws CacheReloadException  {
        };
      });
    }

    private Status readStatus(final long _id) throws Exception  {
      return readStatus4Statement(
          "select "+
            "ID,"+
            "NAME,"+
            "LCPOLICY "+
          "from LCSTATUS "+
          "where ID="+_id
      );
    }

    private Status readStatus(final String _name) throws Exception  {
      return readStatus4Statement(
          "select "+
            "ID,"+
            "NAME,"+
            "LCPOLICY "+
          "from LCSTATUS "+
          "where NAME='"+_name+"'"
      );
    }

    private Status readStatus4Statement(final String _statement) throws Exception  {
      Status ret = null;
      Statement stmt = Context.getThreadContext().getConnection().createStatement();
      try  {
        ResultSet rs = stmt.executeQuery(_statement);
        if (rs.next())  {
          long id =       rs.getLong(1);
          String name =   rs.getString(2);
          long policyId = rs.getLong(3);
          ret = new Status(id, name, Policy.get(policyId));
          add(ret);
          ret.readDBAccess();
        }
        rs.close();
      } catch (Exception e)  {
e.printStackTrace();
      } finally  {
        stmt.close();
      }
      return ret;
    }
  }
}

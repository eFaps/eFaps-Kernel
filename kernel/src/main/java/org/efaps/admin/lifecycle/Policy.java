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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.efaps.db.Context;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Policy extends LifeCycleObject  {

  /**
   *
   */
  private Policy(long _id, String _name)  {
    super(_id, _name);
  }

  /**
   * Returns the name of the policy.
   *
   * @param _context
   */
  public String getViewableName(Context _context)  {
    return getName();
  }

  /**
   * Add a new instance of class {@link Status} to the policy.
   *
   * @param _status new status to add
   * @see #status
   * @see #getStatus
   */
  public void addStatus(Status _status)  {
    getStatus().add(_status);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Policy}.
   *
   * @return instance of class {@link Policy}
   */
  public static Policy get(final long _id) throws Exception  {
    Policy policy = (Policy) getCache().get(_id);
    if (policy == null)  {
      policy = getCache().readPolicy(_id);
    }
    return policy;
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class {@link Policy}.
   *
   * @return instance of class {@link Policy}
   */
  public static Policy get(final String _name) throws Exception  {
    Policy policy = (Policy) getCache().get(_name);
    if (policy == null)  {
      policy = getCache().readPolicy(_name);
    }
    return policy;
  }

  /**
   * Static getter method for the policy hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static public PolicyCache getCache()  {
    return cache;
  }

  /**
   * Stores all instances of policy.
   *
   * @see #get
   */
  static private PolicyCache cache = new PolicyCache();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method reads all status for this policy.
   *
   * @param _context  context for this request
   */
  private void readDBStatus() throws Exception  {
    Statement stmt = Context.getThreadContext().getConnection().createStatement();
    try  {
      ResultSet rs = stmt.executeQuery(
          "select "+
            "ID "+
          "from LCSTATUS "+
          "where LCPOLICY="+getId()
      );
      while (rs.next())  {
        Status.get(rs.getLong(1));
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
   * Instance variable for the list of status.
   *
   * @see #getStatus
   * @see #addStatus
   */
  private List < Status > status = new ArrayList < Status > ();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Getter method for instance variable {@link #status}.
   *
   * @see #status
   * @see #addStatus
   */
  public List < Status > getStatus()  {
    return this.status;
  }

  /////////////////////////////////////////////////////////////////////////////

  static protected class PolicyCache extends Cache < Policy >  {

    private PolicyCache()  {
      super(new CacheReloadInterface()  {
          public int priority()  {
            return 1200;
          };
          public void reloadCache() throws CacheReloadException  {
          };
      });
    }

    private Policy readPolicy(final long _id) throws Exception  {
      return readPolicy4Statement(
          "select "+
            "ID,"+
            "NAME "+
          "from LCPOLICY "+
          "where ID="+_id
      );
    }

    private Policy readPolicy(final String _name) throws Exception  {
      return readPolicy4Statement(
          "select "+
            "ID,"+
            "NAME "+
          "from LCPOLICY "+
          "where NAME='"+_name+"'"
      );
    }

    private Policy readPolicy4Statement(final String _statement) throws Exception  {
      Policy ret = null;
      Statement stmt = Context.getThreadContext().getConnection().createStatement();
      try  {
        ResultSet rs = stmt.executeQuery(_statement);
        if (rs.next())  {
          long id =       rs.getLong(1);
          String name =   rs.getString(2);
          ret = new Policy(id, name);
          add(ret);
          ret.readDBStatus();
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

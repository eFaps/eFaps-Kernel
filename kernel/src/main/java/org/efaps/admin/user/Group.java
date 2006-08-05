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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class Group extends UserObject implements CacheInterface  {

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Group.class);

  /**
   * This is the sql select statement to select all groups from the database.
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "NAME "
                                              + "from V_USERGROUP";

  /**
   * Stores all instances of class {@link Group}.
   *
   * @see #getCache
   */
  private static final Cache <Group > cache = new Cache < Group > ();

  /**
   * Create a new group instance. The method is used from the static method
   * {@link #initialise} to read all groups from the database.
   *
   * @param _id
   */
  private Group(final long _id, final String _name)  {
    super(_id, _name);
  }

  /**
   * Returns the viewable name of the group. The method {@link #getName} is
   * used for the viewing name.
   *
   * @param _context context for this request
   * @see #getName
   */
  public String getViewableName(final Context _context)  {
    return getName();
  }

  /**
   * Checks, if the given person is assigned to this group.
   *
   * @param _person person to test
   * @return <i>true</i> if the person is assigned to this group, otherwise
   *         <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  public boolean hasChildPerson(final Person _person) {
// TODO: child groups
    return _person.isAssigned(this);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of JAAS systems.
   *
   * @param _context  eFaps context for this request
   */
  public static void initialise(final Context _context) throws Exception  {
    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      Statement stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id =             rs.getLong(1);
          String name =         rs.getString(2).trim();

          LOG.debug("read group '" + name + "' (id = " + id + ")");
          cache.add(new Group(id, name));
        }
        rs.close();

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }

      con.commit();

    } finally  {
      if ((con != null) && con.isOpened())  {
        con.abort();
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Group}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Group}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Group get(final long _id)  {
    return cache.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Group}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Group}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Group get(final String _name)  {
    return cache.get(_name);
  }

  /**
   * Static getter method for the group {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   * @see #cache
   */
  static public Cache < Group > getCache()  {
    return cache;
  }

  /**
   * Returns for given parameter <i>_jaasKey</i> the instance of class
   * {@link Group}. The parameter <i>_jaasKey</i> is the name of the group
   * used in the given JAAS system for the group.
   *
   * @param _jaasSystem JAAS system for which the JAAS key is named
   * @param _jaasKey    key in the foreign JAAS system for which the group is
   *                    searched
   * @return instance of class {@link Group}, or <code>null</code> if group
   *         is not found
   * @see #get(long)
   */
  static public Group getWithJAASKey(final JAASSystem _jaasSystem, final String _jaasKey) throws EFapsException  {
    long groupId = 0;
    ConnectionResource rsrc = null;
    try  {
      rsrc = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      try  {
        StringBuilder cmd = new StringBuilder();
        cmd.append("select ")
           .append(   "ID ")
           .append(   "from V_USERGROUPJASSKEY ")
           .append(   "where JAASKEY='").append(_jaasKey).append("' ")
           .append(       "and JAASSYSID=").append(_jaasSystem.getId());

        stmt = rsrc.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(cmd.toString());
        if (rs.next())  {
          groupId = rs.getLong(1);
        }
        rs.close();

      } catch (SQLException e)  {
        LOG.error("search for group for JAAS system "
            + "'" + _jaasSystem.getName() + "' "
            + "with key '" + _jaasKey + "' is not possible", e);
// TODO: exception in properties
        throw new EFapsException(Group.class, "getWithJAASKey.SQLException",
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
    return get(groupId);
  }
}

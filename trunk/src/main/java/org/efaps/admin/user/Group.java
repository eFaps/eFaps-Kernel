/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Group extends AbstractUserObject {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Group.class);

  /**
   * This is the sql select statement to select all groups from the database.
   */
  private static final String SQL_SELECT =
      "select ID, NAME, STATUS from V_USERGROUP";

  private static GroupCache CACHE = new GroupCache();

  /**
   * Create a new group instance. The method is used from the static method
   * {@link #initialise} to read all groups from the database.
   *
   * @param _id
   */
  private Group(final long _id, final String _name, final boolean _status) {
    super(_id, null, _name, _status);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Returns the viewable name of the group. The method {@link #getName} is used
   * for the viewing name.
   *
   * @param _context
   *                context for this request
   * @see #getName
   */
  public String getViewableName(final Context _context) {
    return getName();
  }

  /**
   * Checks, if the given person is assigned to this group.
   *
   * @param _person
   *                person to test
   * @return <i>true</i> if the person is assigned to this group, otherwise
   *         <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  @Override
  public boolean hasChildPerson(final Person _person) {
    // TODO: child groups
    return _person.isAssigned(this);
  }

  /**
   * Method to initialize the Cache of this CacheObjectInterface.
   */
  public static void initialize() {
    CACHE.initialize(Group.class);
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Group}.
   *
   * @param _id
   *                id to search in the cache
   * @return instance of class {@link Group}
   * @throws CacheReloadException
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Group get(final long _id) throws CacheReloadException {
    return CACHE.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Group}.
   *
   * @param _name
   *                name to search in the cache
   * @return instance of class {@link Group}
   * @throws CacheReloadException
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Group get(final String _name) throws CacheReloadException {
    return CACHE.get(_name);
  }

  /**
   * Returns for given parameter <i>_jaasKey</i> the instance of class
   * {@link Group}. The parameter <i>_jaasKey</i> is the name of the group
   * used in the given JAAS system for the group.
   *
   * @param _jaasSystem
   *                JAAS system for which the JAAS key is named
   * @param _jaasKey
   *                key in the foreign JAAS system for which the group is
   *                searched
   * @return instance of class {@link Group}, or <code>null</code> if group
   *         is not found
   * @see #get(long)
   */
  static public Group getWithJAASKey(final JAASSystem _jaasSystem,
                                     final String _jaasKey)
                                                           throws EFapsException {
    long groupId = 0;
    ConnectionResource rsrc = null;
    try {
      rsrc = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      try {
      final  StringBuilder cmd = new StringBuilder();
        cmd.append("select ").append("ID ").append("from V_USERGROUPJASSKEY ")
            .append("where JAASKEY='").append(_jaasKey).append("' ").append(
                "and JAASSYSID=").append(_jaasSystem.getId());

        stmt = rsrc.getConnection().createStatement();
       final ResultSet resultset = stmt.executeQuery(cmd.toString());
        if (resultset.next()) {
          groupId = resultset.getLong(1);
        }
        resultset.close();

      } catch (final SQLException e) {
        LOG.error("search for group for JAAS system "
            + "'"
            + _jaasSystem.getName()
            + "' "
            + "with key '"
            + _jaasKey
            + "' is not possible", e);
        // TODO: exception in properties
        throw new EFapsException(Group.class, "getWithJAASKey.SQLException", e,
            _jaasSystem.getName(), _jaasKey);
      }
      finally {
        try {
          stmt.close();
        } catch (final SQLException e) {
        }
      }
      rsrc.commit();
    }
    finally {
      if ((rsrc != null) && rsrc.isOpened()) {
        rsrc.abort();
      }
    }
    return get(groupId);
  }

  private static class GroupCache extends Cache<Group> {


    @Override
    protected void readCache(final Map<Long, Group> cache4Id,
        final Map<String, Group> cache4Name, final Map<UUID, Group> cache4UUID)
        throws CacheReloadException {
      ConnectionResource con = null;
      try {
        con = Context.getThreadContext().getConnectionResource();

        Statement stmt = null;
        try {

          stmt = con.getConnection().createStatement();

         final ResultSet resultset = stmt.executeQuery(SQL_SELECT);
          while (resultset.next()) {
           final long id = resultset.getLong(1);
           final String name = resultset.getString(2).trim();
           final boolean status = resultset.getBoolean(3);
            LOG.debug("read group '" + name + "' (id = " + id + ")");
            final Group group = new Group(id, name, status);
            cache4Id.put(group.getId(), group);
            cache4Name.put(group.getName(), group);
            cache4UUID.put(group.getUUID(), group);
          }
          resultset.close();

        }
        finally {
          if (stmt != null) {
            stmt.close();
          }
        }

        con.commit();
      } catch (final SQLException e) {
        throw new CacheReloadException("could not read groups", e);
      } catch (final EFapsException e) {
        throw new CacheReloadException("could not read groups", e);
      }
      finally {
        if ((con != null) && con.isOpened()) {
          try {
            con.abort();
          } catch (final EFapsException e) {
            throw new CacheReloadException("could not read groups", e);
          }
        }
      }

    }

  }
}

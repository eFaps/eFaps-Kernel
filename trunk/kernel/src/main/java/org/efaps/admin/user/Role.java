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
 * Class represents the instance of a role in eFaps.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 * @todo description
 */
public final class Role extends AbstractUserObject {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Role.class);

  /**
   * This is the sql select statement to select all roles from the database.
   */
  private static final String SQL_SELECT =
      "select ID, UUID, NAME, STATUS from V_USERROLE";

  /**
   * Stores all instances of class {@link Role}.
   *
   * @see #getCache
   */
  private static final RoleCache CACHE = new RoleCache();

  /**
   * Create a new role instance. The method is used from the static method
   * {@link #initialise} to read all roles from the database.
   *
   * @param _id       id of the role
   * @param _uuid     uuid of the role
   * @param _name     name of the role
   * @param _status   status of the role
   */
  private Role(final long _id, final String _uuid, final String _name,
               final boolean _status) {
    super(_id, _uuid, _name, _status);
  }

  /**
   * Checks, if the given person is assigned to this role.
   *
   * @param _person
   *                person to test
   * @return <i>true</i> if the person is assigned to this role, otherwise
   *         <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  @Override
  public boolean hasChildPerson(final Person _person) {
    // TODO: child roles
    return _person.isAssigned(this);
  }

  /**
   * Method to initialize the Cache of this CacheObjectInterface.
   */
  public static void initialize() {
    CACHE.initialize(Role.class);
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Role}.
   *
   * @param _id  id to search in the cache
   * @return instance of class {@link Role}
   * @throws CacheReloadException on error
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Role get(final long _id) throws CacheReloadException {
    return CACHE.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Role}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Role}
   * @throws CacheReloadException on error
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Role get(final String _name) throws CacheReloadException {
    return CACHE.get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link Role}.
   * @param _uuid UUI to search for
   * @return instance of class {@link Role}
   * @throws CacheReloadException on error
   */
  public static Role get(final UUID _uuid) throws CacheReloadException {
    return CACHE.get(_uuid);
  }

  /**
   * Returns for given parameter <i>_jaasKey</i> the instance of class
   * {@link Role}. The parameter <i>_jaasKey</i> is the name of the role used
   * in the given JAAS system for the role.
   *
   * @param _jaasSystem   JAAS system for which the JAAS key is named
   * @param _jaasKey      key in the foreign JAAS system for which the role is
   *                      searched
   * @throws EFapsException on error
   * @return instance of class {@link Role}, or <code>null</code> if role is
   *         not found
   * @see #get(long)
   */
  public static Role getWithJAASKey(final JAASSystem _jaasSystem,
                                    final String _jaasKey)
      throws EFapsException {
    long roleId = 0;
    ConnectionResource rsrc = null;
    try {
      rsrc = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      try {
   final     StringBuilder cmd = new StringBuilder();
        cmd.append("select ").append("ID ").append("from V_USERROLEJASSKEY ")
            .append("where JAASKEY='").append(_jaasKey).append("' ").append(
                "and JAASSYSID=").append(_jaasSystem.getId());

        stmt = rsrc.getConnection().createStatement();
        final ResultSet resultset = stmt.executeQuery(cmd.toString());
        if (resultset.next()) {
          roleId = resultset.getLong(1);
        }
        resultset.close();

      } catch (final SQLException e) {
        LOG.warn("search for role for JAAS system '" + _jaasSystem.getName()
            + "' with key '" + _jaasKey + "' is not possible", e);
        throw new EFapsException(Role.class, "getWithJAASKey.SQLException", e,
            _jaasSystem.getName(), _jaasKey);
      } finally {
        try {
          stmt.close();
        } catch (final SQLException e) {
          LOG.warn("Catched SQLException in class " + Role.class);
        }
      }
      rsrc.commit();
    } finally {
      if ((rsrc != null) && rsrc.isOpened()) {
        rsrc.abort();
      }
    }
    return get(roleId);
  }

  /**
   * Method to get the Cache for Roles.
   * @return Cache
   */
  public static Cache<Role> getCache() {
    return CACHE;
  }

  /**
   * Class used as the Cahce for Roles.
   */
  private static final class RoleCache extends Cache<Role> {

    /**
     * Method to read the data into the cache.
     * @param _cache4Id   cache with id as key
     * @param _cache4Name cache with name as key
     * @param _cache4UUID cache with uuid as key
     * @throws CacheReloadException on erro during reading the date
     */
    @Override
    protected void readCache(final Map<Long, Role> _cache4Id,
                             final Map<String, Role> _cache4Name,
                             final Map<UUID, Role> _cache4UUID)
        throws CacheReloadException {
      ConnectionResource con = null;
      try {
        con = Context.getThreadContext().getConnectionResource();

        Statement stmt = null;
        try {

          stmt = con.getConnection().createStatement();

          final ResultSet resulset = stmt.executeQuery(SQL_SELECT);
          while (resulset.next()) {
            final long id = resulset.getLong(1);
            final String uuid = resulset.getString(2);
            final String name = resulset.getString(3).trim();
            final boolean status = resulset.getBoolean(4);

            LOG.debug("read role '" + name + "' (id = " + id + ")");
            final Role role = new Role(id, uuid, name, status);
            _cache4Id.put(role.getId(), role);
            _cache4Name.put(role.getName(), role);
            _cache4UUID.put(role.getUUID(), role);
          }
          resulset.close();
        } finally {
          if (stmt != null) {
            stmt.close();
          }
        }
        con.commit();
      } catch (final SQLException e) {
        throw new CacheReloadException("could not read roles", e);
      } catch (final EFapsException e) {
        throw new CacheReloadException("could not read roles", e);
      } finally {
        if ((con != null) && con.isOpened()) {
          try {
            con.abort();
          } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
          }
        }
      }
    }
  }
}

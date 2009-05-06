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

package org.efaps.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.AbstractAdminObject;
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
public class AccessType extends AbstractAdminObject  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AccessType.class);

  /**
   * This is the sql select statement to select all access types from the
   * database.
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "UUID,"
                                                + "NAME "
                                              + "from T_ACCESSTYPE";

  /**
   * Stores all instances of class {@link AccessType}.
   *
   * @see #getAccessType(long)
   * @see #getAccessType(String)
   * @see #getAccessType(UUID)
   */
  private static final AccessTypeCache CACHE = new AccessTypeCache();

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * This is the constructor.
   *
   * @param _id     id of this access type
   * @param _uuid   universal unique identifier of this access type
   * @param _name   name of this access type
   */
  private AccessType(final long _id,
                     final String _uuid,
                     final String _name)  {
    super(_id, _uuid, _name);
  }

  /**
   * The method checks, if the given object represents the same access type as
   * this instance. Equals means, that the object to compare is not null,
   * an instance of this class {@link AccessType} and both id's are the same.
   *
   * @param _toCompare  object used to compare
   * @return <i>true</i> if equals, otherwise <i>false</i>
   */
  @Override
  public boolean equals(final Object _toCompare)  {
    return (_toCompare != null)
            && (_toCompare instanceof AccessType)
            && (((AccessType) _toCompare).getId() == getId());
  }

  /**
   * Method to initialize the Cache of this CacheObjectInterface.
   */
  public static void initialize() {
    CACHE.initialize(AccessType.class);
  }

  /**
   * Returns for given identifier in  <i>_id</i> the cached instance of class
   * AccessType.
   *
   * @return instance of class AccessType
   * @throws CacheReloadException
   */
  static public AccessType getAccessType(final long _id) {
    return CACHE.get(_id);
  }

  /**
   * Returns for given name in <i>_name</i> the cached instance of class
   * AccessType.
   *
   * @return instance of class AccessType
   * @throws CacheReloadException
   */
  static public AccessType getAccessType(final String _name)  {
    return CACHE.get(_name);
  }

  /**
   * Returns for given universal unique identifier in <i>_uuid</i> the cached
   * instance of class AccessType.
   *
   * @return instance of class AccessType
   * @throws CacheReloadException
   */
  static public AccessType getAccessType(final UUID _uuid)  {
    return CACHE.get(_uuid);
  }

  private static class AccessTypeCache extends Cache<AccessType> {


    @Override
    protected void readCache(final Map<Long, AccessType> _newCache4Id,
                             final Map<String, AccessType> _newCache4Name,
                             final Map<UUID, AccessType> _newCache4UUID)
        throws CacheReloadException {
      ConnectionResource con = null;
      try  {
        con = Context.getThreadContext().getConnectionResource();

        Statement stmt = null;
        try  {

          stmt = con.getConnection().createStatement();

          final ResultSet rs = stmt.executeQuery(SQL_SELECT);
          while (rs.next())  {
            final long id                       = rs.getLong(1);
            final String uuid                   = rs.getString(2);
            final String name                   = rs.getString(3);

            LOG.debug("read access type '" + name + "' "
                      + "(id = " + id + ", uuid = " + uuid + ")");

            final AccessType accessType = new AccessType(id, uuid, name);
            _newCache4Id.put(accessType.getId(), accessType);
            _newCache4Name.put(accessType.getName(), accessType);
            _newCache4UUID.put(accessType.getUUID(), accessType);
          }
          rs.close();

        } finally  {
          if (stmt != null)  {
            stmt.close();
          }
        }

        con.commit();

      } catch (final SQLException e)  {
        throw new CacheReloadException("could not read access types", e);
      } catch (final EFapsException e)  {
        throw new CacheReloadException("could not read access types", e);
      } finally  {
        if ((con != null) && con.isOpened())  {
          try  {
            con.abort();
          } catch (final EFapsException e)  {
            throw new CacheReloadException("could not abort transaction "
                                           + "while reading access types", e);
          }
        }
      }
    }
  }
}

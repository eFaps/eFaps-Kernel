/*
 * Copyright 2003-2007 The eFaps Team
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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.AdminObject;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class AccessType extends AdminObject  {

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
  private static final Cache < AccessType > cache 
                                                = new Cache < AccessType > (
    new CacheReloadInterface()  {
        public int priority()  {
          return CacheReloadInterface.Priority.AccessType.number;
        };
        public void reloadCache() throws CacheReloadException  {
          AccessType.initialise();
        };
    }
  );

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

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The method checks, if the given object represents the same access type as
   * this instance. Equals means, that the object to compare is not null,
   * an instance of this class {@link AccessType} and both id's are the same.
   *
   * @param _toCompare  object used to compare
   * @return <i>true</i> if equals, otherwise <i>false</i>
   */
  public boolean equals(final Object _toCompare)  {
    return (_toCompare != null)
            && (_toCompare instanceof AccessType)
            && (((AccessType) _toCompare).getId() == getId());
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Initialise the cache of JAAS systems. All access types are read from the
   * database.
   *
   * @param _context  eFaps context for this request
   * @see #getMethod
   */
  public static void initialise() throws CacheReloadException  {
    ConnectionResource con = null;
    try  {
      con = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id                       = rs.getLong(1);
          String uuid                   = rs.getString(2);
          String name                   = rs.getString(3);

          LOG.debug("read access type '" + name + "' "
                    + "(id = " + id + ", uuid = " + uuid + ")");

          cache.add(new AccessType(id, uuid, name));
        }
        rs.close();

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }

      con.commit();

    } catch (SQLException e)  {
      throw new CacheReloadException("could not read access types", e);
    } catch (EFapsException e)  {
      throw new CacheReloadException("could not read access types", e);
    } finally  {
      if ((con != null) && con.isOpened())  {
        try  {
          con.abort();
        } catch (EFapsException e)  {
          throw new CacheReloadException("could not abort transaction "
                                         + "while reading access types", e);
        }
      }
    }
  }

  /**
   * Returns for given identifier in  <i>_id</i> the cached instance of class 
   * AccessType.
   *
   * @return instance of class AccessType
   */
  static public AccessType getAccessType(final long _id)  {
    return cache.get(_id);
  }

  /**
   * Returns for given name in <i>_name</i> the cached instance of class 
   * AccessType.
   *
   * @return instance of class AccessType
   */
  static public AccessType getAccessType(final String _name)  {
    return cache.get(_name);
  }

  /**
   * Returns for given universal unique identifier in <i>_uuid</i> the cached 
   * instance of class AccessType.
   *
   * @return instance of class AccessType
   */
  static public AccessType getAccessType(final UUID _uuid)  {
    return cache.get(_uuid);
  }
}

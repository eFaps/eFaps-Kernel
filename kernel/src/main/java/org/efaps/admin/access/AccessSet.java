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

package org.efaps.admin.access;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.AdminObject;
import org.efaps.db.Cache;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class AccessSet extends AdminObject  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(AccessSet.class);

  /**
   * This is the sql select statement to select all access types from the
   * database.
   *
   * @see #initialise
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "UUID,"
                                                + "NAME "
                                              + "from ACCESSSET";

  /**
   * This is the sql select statement to select the links from all access sets
   * to all access types in the database.
   *
   * @see #initialise
   */
 private static final String SQL_SET2TYPE = "select "
                                                + "ACCESSSET,"
                                                + "ACCESSTYPE "
                                              + "from ACCESSSET2TYPE";

  /**
   * Stores all instances of class {@link AccessSet}.
   *
   * @see #getAccessSet(long)
   * @see #getAccessSet(String)
   * @see #getAccessSet(UUID)
   */
  private static final Cache < AccessSet > cache = new Cache < AccessSet > ();

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All related access types of this access set are referenced in this 
   * instance variable.
   *
   * @see #getAccessTypes
   */
  private final Set < AccessType > accessTypes = new HashSet < AccessType > ();

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * This is the constructor.
   *
   * @param _id     id of this access type
   * @param _uuid   universal unique identifier of this access type
   * @param _name   name of this access type
   */
  private AccessSet(final long _id,
                    final String _uuid,
                    final String _name)  {
    super(_id, _uuid, _name);
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods

  /**
   * This is the getter method for instance variable {@link #accessTypes}.
   *
   * @return the value of the instance variable {@link #accessTypes}.
   * @see #accessTypes
   */
  public Set < AccessType > getAccessTypes()  {
    return this.accessTypes;
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Initialise the cache of JAAS systems. All access sets and their links to
   * the access types are read from the database.
   *
   * @param _context  eFaps context for this request
   * @see #SQL_SELECT
   * @see #SQL_SET2TYPE
   */
  public static void initialise(final Context _context) throws Exception  {
    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      // read access sets
      Statement stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id                       = rs.getLong(1);
          String uuid                   = rs.getString(2);
          String name                   = rs.getString(3);

          LOG.debug("read access set '" + name + "' "
                    + "(id = " + id + ", uuid = " + uuid + ")");

          cache.add(new AccessSet(id, uuid, name));
        }
        rs.close();

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }

      // read links from access sets to access types
      stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SET2TYPE);
        while (rs.next())  {
          long accessSetId              = rs.getLong(1);
          long accessTypeId             = rs.getLong(2);

          AccessSet accessSet   = AccessSet.getAccessSet(accessSetId);
          AccessType accessType = AccessType.getAccessType(accessTypeId);
          if (accessSet == null)  {
            LOG.error("could not found access set with id "
                                                  + "'" + accessSetId + "'");
          } else if (accessType == null)  {
            LOG.error("could not found access type with id "
                                                  + "'" + accessTypeId + "'");
          } else  {
            LOG.debug("read link from "
                      + "access set '" + accessSet.getName() + "' "
                      + "(id = " + accessSet.getId() + ", "
                          + "uuid = " + accessSet.getUUID() + ") to "
                      + "access type '" + accessType.getName() + "' "
                      + "(id = " + accessType.getId() + ", "
                          + "uuid = " + accessType.getUUID() + ")");
            accessSet.getAccessTypes().add(accessType);
          }
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
   * Returns for given identifier in  <i>_id</i> the cached instance of class 
   * AccessSet.
   *
   * @return instance of class AccessSet
   */
  static public AccessSet getAccessSet(final long _id)  {
    return cache.get(_id);
  }

  /**
   * Returns for given name in <i>_name</i> the cached instance of class 
   * AccessSet.
   *
   * @return instance of class AccessSet
   */
  static public AccessSet getAccessSet(final String _name)  {
    return cache.get(_name);
  }

  /**
   * Returns for given universal unique identifier in <i>_uuid</i> the cached 
   * instance of class AccessSet.
   *
   * @return instance of class AccessSet
   */
  static public AccessSet getAccessSet(final UUID _uuid)  {
    return cache.get(_uuid);
  }
}

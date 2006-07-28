/*
 * Copyright 2005 The eFaps Team
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
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.AdminObject;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;

/**
 * @author tmo
 * @version $Id$
 */
public class JAASSystem extends AdminObject implements CacheInterface  {

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(JAASSystem.class);

  /**
   * This is the sql select statement to select all JAAS systems from the
   * database.
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "NAME "
                                              + "from V_USERJAASSYSTEM";

  /**
   * Stores all instances of class {@link JAASSystem}.
   *
   * @see #getCache
   */
  private static final Cache < JAASSystem > cache = new Cache < JAASSystem > ();

  /**
   * Constructor to set the id and name of the user object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  private JAASSystem(final long _id, final String _name)  {
    super(_id, _name);
  }

// TODO: this is needed anymore??
public String getViewableName(final Context _context)  {
return getName();
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

          LOG.debug("read JAAS System '" + name + "' (id = " + id + ")");

          cache.add(new JAASSystem(id, name));
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
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link JAASSystem}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link JAASSystem}
   */
  static public JAASSystem getJAASSystem(final long _id)  {
    return cache.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link JAASSystem}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link JAASSystem}
   */
  static public JAASSystem getJAASSystem(final String _name)  {
    return cache.get(_name);
  }
}

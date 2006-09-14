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
 */

package org.efaps.admin.lifecycle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.efaps.db.Cache;
import org.efaps.db.Context;

/**
 */
public class AccessType extends LifeCycleObject  {

  /**
   * This is the constructor.
   *
   * @param _id     id of the AccessType
   * @param _name   name of the AccessType
   * @param _policy policy of this AccessType
   */
  private AccessType(long _id, String _name)  {
    super(_id, _name);
  }

  /**
   * Returns the name of the AccessType.
   *
   * @param _context
   */
  public String getViewableName(Context _context)  {
    return getName();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class AccessType.
   *
   * @return instance of class AccessType
   */
  static public AccessType get(Context _context, long _id) throws Exception  {
    AccessType ret = getCache().get(_id);
    if (ret==null)  {
      ret = getCache().read(_context, _id);
    }
    return ret;
  }

  /**
   * Static getter method for the AccessType Cache {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static public AccessTypeCache getCache()  {
    return cache;
  }

  /**
   * Stores all instances of AccessType.
   *
   * @see #get
   */
  static private AccessTypeCache cache = new AccessTypeCache();

  /////////////////////////////////////////////////////////////////////////////

  static protected class AccessTypeCache extends Cache<AccessType>  {

    private AccessType read(Context _context, long _id) throws Exception  {
      AccessType ret = null;
      Statement stmt = _context.getConnection().createStatement();
      try  {
        ResultSet rs = stmt.executeQuery(
            "select "+
              "ID,"+
              "NAME "+
            "from ACCESSTYPE "+
            "where ID="+_id
        );
        if (rs.next())  {
          long id =       rs.getLong(1);
          String name =   rs.getString(2);
          ret = new AccessType(id, name);
          add(ret);
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

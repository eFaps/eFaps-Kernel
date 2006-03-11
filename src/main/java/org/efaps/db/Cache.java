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

package org.efaps.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;

/**
 */
public class Cache<K extends CacheInterface>  {

  public Cache()  {
caches.add(this);
  }

/*  public Cache(Connection _con, String _tableName, String _cacheExpr) throws SQLException  {
    Statement stmt = _con.createStatement();
System.out.println("cacheexpression = select ID,"+_cacheExpr+" from "+_tableName);
    ResultSet rs = stmt.executeQuery(
        "select "+
          "ID,"+
          _cacheExpr+" "+
        "from "+_tableName
    );
    while (rs.next())  {
      long id =       rs.getLong(1);
      String name =   rs.getString(2);
      add(new CacheObject(id, name));
    }
    rs.close();
  }
*/
  public K get(long _id)  {
    return getCache4Id().get(new Long(_id));
  }

  public K get(String _name)  {
    return getCache4Name().get(_name);
  }

  /**
   * Add a new object implements the {@link CacheInterface} to the hashtable.
   * This is used from method {@link #get(long)} and {@link #get(String) to
   * return the cache object for an id or a string out of the cache.
   *
   * @param _cacheObj cache object to add
   * @see #get
   */
  public void add(K _cacheObj)  {
    getCache4Id().put(new Long(_cacheObj.getId()), _cacheObj);
    getCache4Name().put(_cacheObj.getName(), _cacheObj);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  private Hashtable<Long,K> cache4Id = new Hashtable<Long,K>();

  private Hashtable<String,K> cache4Name = new Hashtable<String,K>();

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   *
   */
  protected Map<Long,K> getCache4Id()  {
    return this.cache4Id;
  }

  /**
   *
   */
  protected Map<String,K> getCache4Name()  {
    return this.cache4Name;
  }



/*  class CacheObject implements CacheInterface  {
    CacheObject(long _id, String _name)  {
      this.id = _id;
      this.name = _name;
    }
    String name;
    long id;
    public String getViewableName(Context _context)  {
      return this.name;
    }
    public String getName()  {
      return this.name;
    }
    public long getId()  {
      return this.id;
    }
  }
*/

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  public static Set<Cache> caches = Collections.synchronizedSet(new HashSet<Cache>());

  /**
   * The static method removes all values in the caches. The datamodel cache
   * is initialised automatically.
   */
  public static void reloadCache(Context _context) throws Exception {
    synchronized(caches)  {
      for (Cache cache : caches)  {
        cache.getCache4Id().clear();
        cache.getCache4Name().clear();
      }
      AttributeType.initialise(_context);
      SQLTable.initialise(_context);
      Type.initialise(_context);
      Attribute.initialise(_context);
    }
  }

}

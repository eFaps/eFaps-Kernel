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

package org.efaps.util.cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.ui.UserInterfaceObject;
import org.efaps.admin.user.Group;
import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Role;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Cache < K extends CacheObjectInterface >  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   *
   * @see #get(Long)
   */
  private final Map < Long, K > cache4Id = new Hashtable < Long, K > ();

  /**
   *
   * @see #get(String)
   */
  private final Map < String, K > cache4Name = new Hashtable < String, K > ();

  /**
   * @see #get(UUID)
   */
  private final Map < UUID, K > cache4UUID = new Hashtable < UUID, K > ();

  /**
   *
   */
  private final CacheReloadInterface reloadInstance;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  public Cache(final CacheReloadInterface _reloadInstance)  {
    this.reloadInstance = _reloadInstance;
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

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
   * @see #cache4Id
   */
  public K get(final long _id)  {
    return getCache4Id().get(new Long(_id));
  }

  /**
   *
   * @see #cache4Name
   */
  public K get(final String _name)  {
    return getCache4Name().get(_name);
  }

  /**
   * @see #cache4UUID
   */
  public K get(final UUID _uuid)  {
    return this.cache4UUID.get(_uuid);
  }

  /**
   * Add a new object implements the {@link CacheInterface} to the hashtable.
   * This is used from method {@link #get(long)} and {@link #get(String) to
   * return the cache object for an id or a string out of the cache.
   *
   * @param _cacheObj cache object to add
   * @see #get
   */
  public void add(final K _cacheObj)  {
    getCache4Id().put(new Long(_cacheObj.getId()), _cacheObj);
    getCache4Name().put(_cacheObj.getName(), _cacheObj);
    if (_cacheObj.getUUID() != null)  {
      this.cache4UUID.put(_cacheObj.getUUID(), _cacheObj);
    }
  }

  /**
   * The method tests, if the cache has stored some entries.
   *
   * @return <i>true</i> if the cache has some entries, otherwise <i>false</i>
   */
  public boolean hasEntries()  {
    return (getCache4Id().size() > 0) 
            || (getCache4Name().size() > 0) 
            || (this.cache4UUID.size() > 0);
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   *
   */
  protected Map < Long, K > getCache4Id()  {
    return this.cache4Id;
  }

  /**
   *
   */
  protected Map < String, K > getCache4Name()  {
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
  public static Set < Cache > caches 
                          = Collections.synchronizedSet(new HashSet<Cache>());

  /**
   * The static method removes all values in the caches. The datamodel cache
   * is initialised automatically.
   */
  public static void reloadCache() throws Exception {
    synchronized(caches)  {
      for (Cache cache : caches)  {
        cache.cache4Id.clear();
        cache.cache4Name.clear();
        cache.cache4UUID.clear();
      }
      JAASSystem.initialise();
      Role.initialise();
      Group.initialise();
      AttributeType.initialise();
      SQLTable.initialise();
      Type.initialise();
      Attribute.initialise();
      AccessType.initialise();
      AccessSet.initialise();
      UserInterfaceObject.initialise();
      EventDefinition.initialise();
/*      JAASSystem.initialise(_context);
      Role.initialise(_context);
      Group.initialise(_context);
      AttributeType.initialise(_context);
      SQLTable.initialise(_context);
      Type.initialise(_context);
      Attribute.initialise(_context);
      AccessType.initialise(_context);
      AccessSet.initialise(_context);
      UserInterfaceObject.initialise(_context);
      EventDefinition.initialise(_context);
*/
    }
  }

  /**
   * The static method removes all values in the caches. The datamodel cache
   * is initialised automatically.
   */
  public static void cleanCache() throws Exception {
    synchronized(caches)  {
      for (Cache cache : caches)  {
        cache.cache4Id.clear();
        cache.cache4Name.clear();
        cache.cache4UUID.clear();
      }
    }
  }
}

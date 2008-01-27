/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.admin.program.bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.CacheReloadInterface;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public final class BundleMaker {

  public static UUID STATICCOMPILED =
      UUID.fromString("76fb464e-1d14-4437-ad23-092ab12669dd");

  /**
   * Map is used to map a List of Names representing StaticSources from the
   * eFaps-DataBase, to a Key for a Bundle
   *
   * @see #BUNDLES
   */
  private static final Map<List<String>, String> BUNDLEMAPPER =
      new HashMap<List<String>, String>();

  /**
   * Map is used to store the relation between a Key and a Bundle
   *
   * @see #BUNDLEMAPPER
   */
  private static final Map<String, BundleInterface> BUNDLES =
      new HashMap<String, BundleInterface>();

  /**
   * this Cache is used to store the Instances of StaticCompiledSource
   * representing a Source from the eFaps-DataBase. It is used used to provide
   * the most rapid access to a Name-OID relation.
   */
  private static final Cache<StaticCompiledSource> CACHE =
      new Cache<StaticCompiledSource>(new CacheReloadInterface() {

        public int priority() {
          return 20000;
        };

        public void reloadCache() throws CacheReloadException {
          BundleMaker.loadCache();
        };
      });

  /**
   * a private Constructor is used to make a singelton
   */
  private BundleMaker() {
  }

  /**
   * this static method is used to get from a List of Names representing
   * StaticSources the a key to Bundle. The method first checks if the a key for
   * the List allready exist and if exists returns the key. Otherwise a new Key
   * will be created using the method {@link #createNewKey(List, Class)}
   *
   * @param _names
   *                List of Names representing StaticSources
   * @param _bundleclass
   *                The Class that will be instandiated in the case that the key
   *                did not allready exist
   * @return the Key to a Bundle
   */
  public static String getBundleKey(final List<String> _names,
                                    final Class<?> _bundleclass) {
    mergeList(_names);
    String key;
    synchronized (BUNDLEMAPPER) {
      if (BUNDLEMAPPER.containsKey(_names)) {
        key = BUNDLEMAPPER.get(_names);
      } else {
        key = createNewKey(_names, _bundleclass);
        BUNDLEMAPPER.put(_names, key);
      }
    }
    return key;
  }

  /**
   * This method removes all duplicated entries from the List from the end. <br>
   * e.g. a List like ABAACD will lead to a list like BACD
   *
   * @param _names
   *                List of Names representing StaticSources
   */
  private static void mergeList(final List<String> _names) {
    final Set<String> compare = new HashSet<String>();

    for (int i = _names.size() - 1; i > -1; i--) {
      if (compare.contains(_names.get(i))) {
        _names.remove(i);
      } else {
        compare.add(_names.get(i));
      }
    }
  }

  /**
   * creates a new Key and instanciates the BundleInterface
   *
   * @param _names
   *                List of Names representing StaticSources
   * @param _bundleclass
   *                The Class to be instanciated
   * @return the Key to a Bundle
   */
  private static String createNewKey(final List<String> _names,
                                     Class<?> _bundleclass) {
    final StringBuilder builder = new StringBuilder();
    final List<String> oids = new ArrayList<String>();
    if (!CACHE.hasEntries()) {
      loadCache();
    }
    for (final String name : _names) {
      if (builder.length() > 0) {
        builder.append("-");
      }
      final String oid = CACHE.get(name).getOid();
      builder.append(oid);
      oids.add(oid);
    }
    final String ret = builder.toString();
    try {
      final BundleInterface bundle =
          (BundleInterface) _bundleclass.newInstance();
      bundle.setKey(ret, oids);
      BUNDLES.put(ret, bundle);
    } catch (final InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;
  }

  /**
   * does a Bundle for the Key exist
   *
   * @param _key
   *                key to search for
   * @return true if found, else false
   */
  public static boolean containsKey(final String _key) {
    return BUNDLES.containsKey(_key);
  }

  /**
   * get a Bundle for a specific key
   *
   * @param _key
   *                key to get the Bundle for
   * @return the bundle if exist, else null
   */
  public static BundleInterface getBundle(final String _key) {
    return BUNDLES.get(_key);
  }

  /**
   * method to load the StaticCompiledSources into the Cache
   */
  private static void loadCache() {
    try {
      synchronized (CACHE) {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(Type.get(STATICCOMPILED).getName());
        query.setExpandChildTypes(true);
        query.addSelect("OID");
        query.addSelect("Name");
        query.execute();
        while (query.next()) {
          final String name = (String) query.get("Name");
          final String oid = (String) query.get("OID");
          CACHE.add(new StaticCompiledSource(oid, name));
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * This class represents one StaticCompiledSource from the eFaps-DataBase
   *
   * @author jmox
   * @version $Id$
   */
  private static class StaticCompiledSource implements CacheObjectInterface {

    /**
     * the Name for the StaticCompiledSource
     */
    private final String name;

    /**
     * the oid for this StaticCompiledSource
     */
    private final String oid;

    public StaticCompiledSource(final String _oid, final String _name) {
      this.name = _name;
      this.oid = _oid;
    }

    public long getId() {
      // not needed here
      return 0;
    }

    public String getName() {
      return this.name;
    }

    public UUID getUUID() {
      // not needed here
      return null;
    }

    /**
     * This is the getter method for the instance variable {@link #oid}.
     *
     * @return value of instance variable {@link #oid}
     */
    public String getOid() {
      return this.oid;
    }

  }

}

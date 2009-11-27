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

package org.efaps.admin.program.bundle;

import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_STATICCOMPILED;

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
import org.efaps.util.cache.AutomaticCache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO description.
 *
 * @author jmox
 * @version $Id$
 */
public final class BundleMaker {

  /**
   * Map is used to map a List of Names representing StaticSources from the
   * eFaps-DataBase, to a Key for a Bundle.
   *
   * @see #BUNDLES
   */
  private static final Map<List<String>, String> BUNDLEMAPPER =
      new HashMap<List<String>, String>();

  /**
   * Map is used to store the relation between a Key and a Bundle.
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
  private static final StaticCompiledSourceCache CACHE = new StaticCompiledSourceCache();

  /**
   * a private Constructor is used to make a singelton.
   */
  private BundleMaker() {
  }

  /**
   * this static method is used to get from a List of Names representing
   * StaticSources the a key to Bundle. The method first checks if the a key for
   * the List already exist and if exists returns the key. Otherwise a new Key
   * will be created using the method {@link #createNewKey(List, Class)}
   *
   * @param _names
   *                List of Names representing StaticSources
   * @param _bundleclass
   *                The Class that will be instantiated in the case that the key
   *                did not already exist
   * @return the Key to a Bundle
   * @throws EFapsException on error
   */
  public static String getBundleKey(final List<String> _names,
                                    final Class<?> _bundleclass)
      throws EFapsException {
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
   * @param _names   List of Names representing StaticSources
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
   * Creates a new Key and instantiates the BundleInterface.
   *
   * @param _names        List of Names representing StaticSources
   * @param _bundleclass  The Class to be instantiated
   * @return the Key to a Bundle
   * @throws EFapsException on error
   */
  private static String createNewKey(final List<String> _names,
                                     final Class<?> _bundleclass)
      throws EFapsException {

    final StringBuilder builder = new StringBuilder();
    final List<String> oids = new ArrayList<String>();
    String ret = null;
    try {
      for (final String name : _names) {
        if (builder.length() > 0) {
          builder.append("-");
        }
        if (CACHE.get(name) != null) {
          final String oid = CACHE.get(name).getOid();
          builder.append(oid);
          oids.add(oid);
        }
      }
      ret = builder.toString();

      final BundleInterface bundle =
          (BundleInterface) _bundleclass.newInstance();
      bundle.setKey(ret, oids);
      BUNDLES.put(ret, bundle);
    } catch (final InstantiationException e) {
      throw new EFapsException(BundleMaker.class,
          "createNewKey.InstantiationException", e, _bundleclass);
    } catch (final IllegalAccessException e) {
      throw new EFapsException(BundleMaker.class,
          "createNewKey.IllegalAccessException", e, _bundleclass);
    }
    return ret;
  }

  /**
   * Does a Bundle for the Key exist.
   *
   * @param _key    key to search for
   * @return true if found, else false
   */
  public static boolean containsKey(final String _key) {
    return BUNDLES.containsKey(_key);
  }

  /**
   * Get a Bundle for a specific key.
   *
   * @param _key    key to get the Bundle for
   * @return the bundle if exist, else null
   */
  public static BundleInterface getBundle(final String _key) {
    return BUNDLES.get(_key);
  }

  /**
   * This class represents one StaticCompiledSource from the eFaps-DataBase.
   *
   * @author jmox
   * @version $Id$
   */
  private static class StaticCompiledSource implements CacheObjectInterface {

    /**
     * The Name for the StaticCompiledSource.
     */
    private final String name;

    /**
     * The oid for this StaticCompiledSource.
     */
    private final String oid;

    /**
     * Constructor.
     *
     * @param _oid    oid of the source
     * @param _name   name of the source
     */
    public StaticCompiledSource(final String _oid, final String _name) {
      this.name = _name;
      this.oid = _oid;
    }

    /**
     * not used.
     * @return 0
     */
    public long getId() {
      // not needed here
      return 0;
    }

    /**
     * Getter method for instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName() {
      return this.name;
    }

    /**
     * not used.
     * @return null
     */
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

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize() {
      CACHE.initialize(BundleMaker.class);
    }
  }

  private static final class StaticCompiledSourceCache extends AutomaticCache<StaticCompiledSource>{


    /* (non-Javadoc)
     * @see org.efaps.util.cache.Cache#readCache(java.util.Map, java.util.Map, java.util.Map)
     */
    @Override
    protected void readCache(final Map<Long, StaticCompiledSource> cache4Id,
        final Map<String, StaticCompiledSource> cache4Name, final Map<UUID, StaticCompiledSource> cache4UUID)
        throws CacheReloadException {
      try {

          final SearchQuery query = new SearchQuery();
          query.setQueryTypes(Type.get(ADMIN_PROGRAM_STATICCOMPILED).getName());
          query.setExpandChildTypes(true);
          query.addSelect("OID");
          query.addSelect("Name");
          query.execute();
          while (query.next()) {
            final String name = (String) query.get("Name");
            final String oid = (String) query.get("OID");
            final StaticCompiledSource source = new StaticCompiledSource(oid, name);
            cache4Name.put(source.getName(), source);
          }
      } catch (final EFapsException e) {
        throw new CacheReloadException(
            "could not initialise the Cache for the BundleMaker");
      }

    }

  }
}

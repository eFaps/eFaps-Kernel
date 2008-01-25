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

  private BundleMaker() {
  }

  private static Cache<StaticCompiledSource> CACHE =
      new Cache<StaticCompiledSource>(new CacheReloadInterface() {

        public int priority() {
          return 20000;
        };

        public void reloadCache() throws CacheReloadException {
          BundleMaker.loadCache();
        };
      });

  private static Map<List<String>, String> BUNDLEMAPPER =
      new HashMap<List<String>, String>();

  private static Map<String, BundleInterface> BUNDLES =
      new HashMap<String, BundleInterface>();

  public static String getBundleKey(final List<String> _names,
                                     Class<?> _bundleclass) {
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

  private static void loadCache() {
    try {
      synchronized (CACHE) {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_Program_StaticCompiled");
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
      // TODO
    }
  }

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
      final BundleInterface x = (BundleInterface) _bundleclass.newInstance();
      x.setKey(ret, oids);
      BUNDLES.put(ret, x);
    } catch (final InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //
    return ret;
  }

  public static boolean containsKey(final String _key) {
    return BUNDLES.containsKey(_key);
  }

  public static BundleInterface getBundle(final String _key) {
    return BUNDLES.get(_key);
  }

  private static class StaticCompiledSource implements CacheObjectInterface {

    private final String name;

    private final String oid;

    public StaticCompiledSource(final String _oid, final String _name) {
      this.name = _name;
      this.oid = _oid;
    }

    public long getId() {
      // TODO Auto-generated method stub
      return 0;
    }

    public String getName() {
      return this.name;
    }

    public UUID getUUID() {
      // TODO Auto-generated method stub
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

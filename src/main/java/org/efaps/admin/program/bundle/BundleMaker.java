/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.admin.program.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO description.
 *
 * @author The eFaps Team
 *
 */
public final class BundleMaker
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(BundleMaker.class);

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = BundleMaker.class.getName() +  ".Name";

    /**
     * Name of the Cache by Name.
     */
    private static final String CACHE4BUNDLEMAP = BundleMaker.class.getName() +  ".BundleMapper";

    /**
     * Name of the Cache by Name.
     */
    private static final String CACHE4BUNDLE = BundleMaker.class.getName() +  ".Bundles";


    /**
     * a private Constructor is used to make a singelton.
     */
    private BundleMaker()
    {
    }

    /**
     * Init this class.
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(BundleMaker.NAMECACHE)) {
            InfinispanCache.get().<UUID, Type>getCache(BundleMaker.NAMECACHE).clear();
            InfinispanCache.get().<UUID, Type>getCache(BundleMaker.CACHE4BUNDLE).clear();
            InfinispanCache.get().<UUID, Type>getCache(BundleMaker.CACHE4BUNDLEMAP).clear();
        } else {
            InfinispanCache.get().<UUID, Type>getCache(BundleMaker.NAMECACHE)
                            .addListener(new CacheLogListener(BundleMaker.LOG));
            InfinispanCache.get().<UUID, Type>getCache(BundleMaker.CACHE4BUNDLE)
                            .addListener(new CacheLogListener(BundleMaker.LOG));
            InfinispanCache.get().<UUID, Type>getCache(BundleMaker.CACHE4BUNDLEMAP)
                            .addListener(new CacheLogListener(BundleMaker.LOG));
        }
    }

    /**
     * this static method is used to get from a List of Names representing
     * StaticSources the a key to Bundle. The method first checks if the a key
     * for the List already exist and if exists returns the key. Otherwise a new
     * Key will be created using the method {@link #createNewKey(List, Class)}
     *
     * @param _names List of Names representing StaticSources
     * @param _bundleclass The Class that will be instantiated in the case that
     *            the key did not already exist
     * @return the Key to a Bundle
     * @throws EFapsException on error
     */
    public static String getBundleKey(final List<String> _names,
                                      final Class<?> _bundleclass)
        throws EFapsException
    {
        BundleMaker.mergeList(_names);
        String key;
        final Cache<List<String>, String> cache = InfinispanCache.get()
                        .<List<String>, String>getCache(BundleMaker.CACHE4BUNDLEMAP);
        final Cache<String, BundleInterface> cache4bundle = InfinispanCache.get()
                        .<String, BundleInterface>getCache(BundleMaker.CACHE4BUNDLE);
        if (cache.containsKey(_names)) {
            key = cache.get(_names);
            if (!cache4bundle.containsKey(key)) {
                BundleMaker.createNewKey(_names, _bundleclass);
            }
        } else {
            key = BundleMaker.createNewKey(_names, _bundleclass);
            cache.put(_names, key);
        }
        return key;
    }

    /**
     * Does a Bundle for the Key exist.
     *
     * @param _key key to search for
     * @return true if found, else false
     */
    public static boolean containsKey(final String _key)
    {
        final Cache<String, BundleInterface> cache = InfinispanCache.get()
                        .<String, BundleInterface>getIgnReCache(BundleMaker.CACHE4BUNDLE);
        return cache.containsKey(_key);
    }

    /**
     * Get a Bundle for a specific key.
     *
     * @param _key key to get the Bundle for
     * @return the bundle if exist, else null
     */
    public static BundleInterface getBundle(final String _key)
    {
        final Cache<String, BundleInterface> cache = InfinispanCache.get()
                        .<String, BundleInterface>getIgnReCache(BundleMaker.CACHE4BUNDLE);
        return cache.get(_key);
    }

    /**
     * This method removes all duplicated entries from the List from the end. <br>
     * e.g. a List like ABAACD will lead to a list like BACD
     *
     * @param _names List of Names representing StaticSources
     */
    private static void mergeList(final List<String> _names)
    {
        Collections.sort(_names);
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
     * @param _names List of Names representing StaticSources
     * @param _bundleclass The Class to be instantiated
     * @return the Key to a Bundle
     * @throws EFapsException on error
     */
    private static String createNewKey(final List<String> _names,
                                       final Class<?> _bundleclass)
        throws EFapsException
    {

        final StringBuilder builder = new StringBuilder();
        final List<String> oids = new ArrayList<String>();
        String ret = null;
        try {
            for (final String name : _names) {
                if (builder.length() > 0) {
                    builder.append("-");
                }
                final Cache<String, StaticCompiledSource> cache = InfinispanCache.get()
                                .<String, StaticCompiledSource>getIgnReCache(BundleMaker.NAMECACHE);
                if (!cache.containsKey(name)) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.StaticCompiled);
                    queryBldr.addWhereAttrEqValue(CIAdminProgram.StaticCompiled.Name, name);
                    final MultiPrintQuery multi = queryBldr.getPrint();
                    multi.addAttribute(CIAdminProgram.StaticCompiled.Name);
                    multi.execute();
                    while (multi.next()) {
                        final String statName = multi.<String>getAttribute(CIAdminProgram.StaticCompiled.Name);
                        final StaticCompiledSource source = new StaticCompiledSource(multi.getCurrentInstance()
                                        .getOid(),
                                        statName);
                        cache.put(source.getName(), source);
                    }
                }
                if (cache.containsKey(name)) {
                    final String oid = cache.get(name).getOid();
                    builder.append(oid);
                    oids.add(oid);
                }
            }
            ret = builder.toString();

            final BundleInterface bundle =
                            (BundleInterface) _bundleclass.newInstance();
            bundle.setKey(ret, oids);

            final Cache<String, BundleInterface> cache = InfinispanCache.get()
                            .<String, BundleInterface>getIgnReCache(BundleMaker.CACHE4BUNDLE);
            cache.put(ret, bundle);
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
     * This class represents one StaticCompiledSource from the eFaps-DataBase.
     */
    private static class StaticCompiledSource
        implements CacheObjectInterface
    {

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
         * @param _oid oid of the source
         * @param _name name of the source
         */
        public StaticCompiledSource(final String _oid,
                                    final String _name)
        {
            this.name = _name;
            this.oid = _oid;
        }

        /**
         * not used.
         *
         * @return 0
         */
        public long getId()
        {
            // not needed here
            return 0;
        }

        /**
         * Getter method for instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * not used.
         *
         * @return null
         */
        public UUID getUUID()
        {
            // not needed here
            return null;
        }

        /**
         * This is the getter method for the instance variable {@link #oid}.
         *
         * @return value of instance variable {@link #oid}
         */
        public String getOid()
        {
            return this.oid;
        }
    }
}

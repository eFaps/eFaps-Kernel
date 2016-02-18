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

package org.efaps.admin.dbproperty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads the Properties for eFaps from the connected Database and
 * holds them in a cache to be accessed fast during normal runtime. <br>
 * The Keys will be read from the database in the order of the Sequence of a
 * Bundle. That gives the possibility to override the key of a Bundle with the
 * same key of another Bundle by using a higher Sequence.<br>
 * The value returned for a key is searched first in the localized version, if
 * no Value can be found or no localized version for this language is existing
 * than the default value will be returned.
 *
 * @author The eFaps Team
 *
 */
public final class DBProperties
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DBProperties.class);

    /**
     * Name of the Infinispan Cache.
     */
    private static final String CACHENAME = DBProperties.class.getName();

    /**
     * String used instead of a <code>NULL</code> value.
     */
    private static final String NULLVALUE = "org.efaps.admin.dbproperty.NULL";

    /**
     * SQL Statement used to find a property including as criteria the language.
     */
    private static final String SQLSELECT = new SQLSelect()
                    .column(0, "DEFAULTV")
                    .column(2, "VALUE")
                    .from("T_ADPROP", 0)
                    .leftJoin("T_ADPROPBUN", 1, "ID", 0, "BUNDLEID")
                    .leftJoin("T_ADPROPLOC", 2, "PROPID", 0, "ID")
                    .leftJoin("T_ADLANG", 3, "ID", 2, "LANGID")
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PROPKEY").addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND)
                    .addPart(SQLPart.PARENTHESIS_OPEN)
                    .addColumnPart(3, "LANG").addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.OR)
                    .addColumnPart(3, "LANG").addPart(SQLPart.IS).addPart(SQLPart.NULL)
                    .addPart(SQLPart.PARENTHESIS_CLOSE)
                    .addPart(SQLPart.ORDERBY)
                    .addColumnPart(1, "SEQUENCE").addPart(SQLPart.DESC).addPart(SQLPart.COMMA)
                    .addColumnPart(3, "LANG").toString();

    /**
     * SQL Statement used to find a property used if with the previous Statement no
     * result where found.
     */
    private static final String SQLSELECTDEF = new SQLSelect()
                    .column(0, "DEFAULTV")
                    .from("T_ADPROP", 0)
                    .leftJoin("T_ADPROPBUN", 1, "ID", 0, "BUNDLEID")
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PROPKEY").addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.ORDERBY)
                    .addColumnPart(1, "SEQUENCE").addPart(SQLPart.DESC).toString();

    /**
     * SQL Statement used to get the properties that must be cached on start.
     */
    private static final String SQLSELECTONSTART = new SQLSelect()
                    .column(0, "PROPKEY")
                    .column(0, "DEFAULTV")
                    .column(2, "VALUE")
                    .column(3, "LANG")
                    .from("T_ADPROP", 0)
                    .innerJoin("T_ADPROPBUN", 1, "ID", 0, "BUNDLEID")
                    .leftJoin("T_ADPROPLOC", 2, "PROPID", 0, "ID")
                    .leftJoin("T_ADLANG", 3, "ID", 2, "LANGID")
                    .addPart(SQLPart.WHERE)
                    .addColumnPart(1, "CACHEONSTART").addPart(SQLPart.EQUAL).addBooleanValue(true)
                    .addPart(SQLPart.ORDERBY)
                    .addColumnPart(1, "SEQUENCE").addPart(SQLPart.ASC).addPart(SQLPart.COMMA)
                    .addColumnPart(0, "PROPKEY").toString();

    /**
     * SQL Statement to get a list of languages.
     */
    private static final String SQLLANG = new SQLSelect()
                    .column("LANG")
                    .from("T_ADLANG").toString();
    /**
     * Private Constructor for Utility class.
     */
    private DBProperties()
    {
    }

    /**
     * Method to find out if a specified key is existing.<br>
     * It is only checked in the default.
     *
     * @param _key Key to search for
     * @return true if the key exists
     */
    public static boolean hasProperty(final String _key)
    {
        return DBProperties.getProperty(_key, false) != null;
    }

    /**
     * Method that returns the value, depending on the language of the Context,
     * for the given key. <br>
     * The Search for the key, first searches for a localized Version and if not
     * found for a Default. If no value can be found, the key will be returned.
     *
     * @param _key Key to Search for
     * @return if key exists, the value for the key, otherwise the key
     */
    public static String getProperty(final String _key)
    {
        String language = null;
        try {
            language = Context.getThreadContext().getLanguage();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the language from the context", e);
        }
        return DBProperties.getProperty(_key, language);
    }

    /**
     * Method that returns the value, depending on the language of the Context,
     * for the given key. <br>
     * The Search for the key, first searches for a localized Version and if not
     * found for a Default. If no value can be found, the key will be returned.
     *
     * @param _key Key to Search for
     * @param _returnKey return the key if no property found
     * @return if key exists, the value for the key, otherwise the key
     */
    public static String getProperty(final String _key,
                                     final boolean _returnKey)
    {
        String language = null;
        try {
            language = Context.getThreadContext().getLanguage();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the language from the context", e);
        }
        return DBProperties.getProperty(_key, language, _returnKey);
    }

    /**
     * Method that returns the value, depending on the parameter _language, for
     * the given key. <br>
     * The Search for the key, first searches for a localized Version and if not
     * found for a Default. If no value can be found, the key will be returned.
     *
     * @param _key Key to Search for
     * @param _language language to use
     * @return if key exists, the value for the key, otherwise the key
     */
    public static String getProperty(final String _key,
                                     final String _language)
    {
        return DBProperties.getProperty(_key, _language, true);
    }

    /**
     * Method that returns the value, depending on the parameter _language, for
     * the given key. <br>
     * The Search for the key, first searches for a localized Version and if not
     * found for a Default. If no value can be found, the key will be returned.
     *
     * @param _key Key to Search for
     * @param _language language to use
     * @param _returnKey return the key if no property found
     * @return if key exists, the value for the key, otherwise the key
     */
    public static String getProperty(final String _key,
                                     final String _language,
                                     final boolean _returnKey)
    {
        String value = null;
        try {
            final SystemConfiguration config = EFapsSystemConfiguration.get();
            final boolean showKey = config == null
                            ? false
                            : config.getAttributeValueAsBoolean(KernelSettings.SHOW_DBPROPERTIES_KEY);

            if (showKey) {
                value = _key;
            } else {
                final String cachKey = _language + ":" + _key;
                final Cache<String, String> cache = InfinispanCache.get().<String, String>getCache(
                                DBProperties.CACHENAME);
                if (cache.containsKey(cachKey)) {
                    value = cache.get(cachKey);
                    if (value.equals(DBProperties.NULLVALUE)) {
                        value = null;
                    }
                } else {
                    value = DBProperties.getValueFromDB(_key, _language);
                    if (value == null) {
                        cache.put(cachKey, DBProperties.NULLVALUE);
                    } else {
                        cache.put(cachKey, value);
                    }
                }
            }
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read ShowDBPropertiesKey from the webConfig", e);
        }
        return (value == null && _returnKey) ? "?? - " + _key + " - ??" : value;
    }

    /**
     * Get a DBProperty and apply a <code>java.util.Formatter</code> with the
     * given _args on it.
     *
     * @param _key key the DBProperty will be searched for
     * @param _args object to be used for the formated
     * @return formated value for the key
     */
    public static String getFormatedDBProperty(final String _key,
                                               final Object... _args)
    {
        String language = null;
        try {
            language = Context.getThreadContext().getLanguage();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the language from the context", e);
        }
        return DBProperties.getFormatedDBProperty(_key, language, _args);
    }

    /**
     * Get a DBProperty and apply a <code>java.util.Formatter</code> with the
     * given _args on it.
     *
     * @param _key key the DBProperty will be searched for
     * @param _language language to be applied
     * @param _args object to be used for the formated
     * @return formated value for the key
     */
    public static String getFormatedDBProperty(final String _key,
                                               final String _language,
                                               final Object... _args)
    {
        String ret = "";
        try {
            ret = DBProperties.getProperty(_key, _language);
            final Locale local = Context.getThreadContext().getLocale();
            final Formatter formatter = new Formatter(local);
            formatter.format(ret, _args);
            ret = formatter.toString();
            formatter.close();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the locale from the context", e);
        } catch (final MissingFormatArgumentException e) {
            DBProperties.LOG.error("wrong format", e);
        }
        return ret;
    }

    /**
     * This method is initializing the cache.
     *
     * @param _key key to be read.
     * @param _language language to search for
     * @return value from the database, null if not found
     */
    private static String getValueFromDB(final String _key,
                                         final String _language)
    {
        String ret = null;
        try {
            boolean closeContext = false;
            if (!Context.isThreadActive()) {
                Context.begin();
                closeContext = true;
            }
            final ConnectionResource con = Context.getThreadContext().getConnectionResource();
            final PreparedStatement stmt = con.getConnection().prepareStatement(DBProperties.SQLSELECT);
            stmt.setString(1, _key);
            stmt.setString(2, _language);
            final ResultSet resultset = stmt.executeQuery();
            if (resultset.next()) {
                final String defaultValue = resultset.getString(1);
                final String value = resultset.getString(2);
                if (value != null) {
                    ret = value.trim();
                } else if (defaultValue != null) {
                    ret = defaultValue.trim();
                }
            } else {
                final PreparedStatement stmt2 = con.getConnection().prepareStatement(DBProperties.SQLSELECTDEF);
                stmt2.setString(1, _key);
                final ResultSet resultset2 = stmt2.executeQuery();
                if (resultset2.next()) {
                    final String defaultValue = resultset2.getString(1);
                    if (defaultValue != null) {
                        ret = defaultValue.trim();
                    }
                }
                resultset2.close();
                stmt2.close();
            }
            resultset.close();
            stmt.close();
            con.commit();
            if (closeContext) {
                Context.rollback();
            }
        } catch (final EFapsException e) {
            DBProperties.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            DBProperties.LOG.error("initialiseCache()", e);
        }
        return ret;
    }

    /**
     * Load the properties that must be cached on start.
     */
    private static void cacheOnStart()
    {
        try {
            boolean closeContext = false;
            if (!Context.isThreadActive()) {
                Context.begin();
                closeContext = true;
            }
            final ConnectionResource con = Context.getThreadContext().getConnectionResource();
            final PreparedStatement stmtLang = con.getConnection().prepareStatement(DBProperties.SQLLANG);
            final ResultSet rsLang = stmtLang.executeQuery();
            final Set<String> languages = new HashSet<String>();
            while (rsLang.next()) {
                languages.add(rsLang.getString(1).trim());
            }
            rsLang.close();
            stmtLang.close();
            final Cache<String, String> cache = InfinispanCache.get().<String, String>getCache(
                            DBProperties.CACHENAME);
            final PreparedStatement stmt = con.getConnection().prepareStatement(DBProperties.SQLSELECTONSTART);
            final ResultSet resultset = stmt.executeQuery();
            while (resultset.next()) {
                final String propKey = resultset.getString(1).trim();
                final String defaultValue = resultset.getString(2);
                if (defaultValue != null) {
                    final String value = defaultValue.trim();
                    for (final String lang : languages) {
                        final String cachKey = lang + ":" + propKey;
                        if (!cache.containsKey(cachKey)) {
                            cache.put(cachKey, value);
                        }
                    }
                }
                final String value = resultset.getString(3);
                final String lang = resultset.getString(4);
                if (value != null) {
                    final String cachKey = lang.trim() + ":" + propKey;
                    cache.put(cachKey, value.trim());
                }
            }
            resultset.close();
            stmt.close();
            con.commit();
            if (closeContext) {
                Context.rollback();
            }
        } catch (final EFapsException e) {
            DBProperties.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            DBProperties.LOG.error("initialiseCache()", e);
        }
    }

    /**
     * Initialize the Cache be calling it. Used from runtime level.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(DBProperties.CACHENAME)) {
            InfinispanCache.get().<String, String>getCache(DBProperties.CACHENAME).clear();
        } else {
            InfinispanCache.get().<String, String>getCache(DBProperties.CACHENAME)
                            .addListener(new CacheLogListener(DBProperties.LOG));
        }
        DBProperties.cacheOnStart();
    }
}

/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.admin.dbproperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads the Properties for eFaps from the connected Database and holds them in a cache to be accessed fast
 * during normal runtime. <br>
 * The Keys will be read from the database in the order of the Sequence of a Bundle. That gives the possibility to
 * override the key of a Bundle with the same key of another Bundle by using a higher Sequence.<br>
 * The value returned for a key is searched first in the localized version, if no Value can be found or no localized
 * version for this language is existing than the default value will be returned.
 *
 * @author The eFasp Team
 * @version $Id$
 */
public class DBProperties
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DBProperties.class);

    /**
     * Value used to identify the Default inside the Cache.
     */
    private static final String DEFAULT = "default";

    /**
     * Cache for the Properties.
     */
    private static final Map<String, Map<String, String>> PROPERTIESCACHE = new HashMap<String, Map<String, String>>();

    /**
     * Are the Properties initialized?
     */
    private static boolean INITIALIZED = false;

    /**
     * For getting all Properties in a Map.
     *
     * @return Map with all Properties
     */
    public Map<String, Map<String, String>> getProperties()
    {
        return DBProperties.PROPERTIESCACHE;
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
        if (!DBProperties.isInitialized()) {
            DBProperties.initialize();
        }
        return DBProperties.PROPERTIESCACHE.get(DBProperties.DEFAULT).get(_key) != null;
    }

    /**
     * Method that returns the value, depending on the language of the Context,
     *  for the given key. <br>
     * The Search for the key, first searches for a localized Version and if
     * not found for a Default. If no value can be
     * found, the key will be returned.
     *
     * @param _key Key to Search for
     * @return if key exists, the value for the key, otherwise the key
     */
    public static String getProperty(final String _key)
    {
        if (!DBProperties.isInitialized()) {
            DBProperties.initialize();
        }

        String language = null;

        try {
            language = Context.getThreadContext().getLanguage();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the language from the context", e);
        }
        return DBProperties.getProperty(_key, language);

    }

    /**
     * Method that returns the value, depending on the parameter _language,
     * for the given key. <br>
     * The Search for the key, first searches for a localized Version and
     * if not found for a Default. If no value can be
     * found, the key will be returned.
     *
     * @param _key Key to Search for
     * @param _language language to use
     * @return if key exists, the value for the key, otherwise the key
     */
    public static String getProperty(final String _key,
                                     final String _language)
    {
        if (!DBProperties.isInitialized()) {
            DBProperties.initialize();
        }

        String value = null;
        try {
            // WebApp-Configuration
            final SystemConfiguration webConfig = SystemConfiguration.get(
                                                    UUID.fromString("50a65460-2d08-4ea8-b801-37594e93dad5"));
            final boolean showKey = webConfig == null
                                                ? false
                                                : webConfig.getAttributeValueAsBoolean("ShowDBPropertiesKey");

            if (showKey) {
                value = _key;
            } else {
                final Map<String, String> map = DBProperties.PROPERTIESCACHE.get(_language);
                if (map != null) {
                    value = map.get(_key);
                }

                if (value == null) {
                    final Map<String, String> defaultProps = DBProperties.PROPERTIESCACHE.get(DBProperties.DEFAULT);
                    if (defaultProps != null) {
                        value = DBProperties.PROPERTIESCACHE.get(DBProperties.DEFAULT).get(_key);
                    }
                }
            }
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read ShowDBPropertiesKey from the webConfig", e);
        }
        return (value == null) ? "?? - " + _key + " - ??" : value;
    }

    /**
     * Get a DBProperty and apply a <code>java.util.Formatter</code> with
     * the given _args on it.
     *
     * @param _key  key the DBProperty will be searched for
     * @param _args object to be used for the formated
     * @return formated value for the key
     */
    public static String getFormatedDBProperty(final String _key,
                                               final Object... _args)
    {
        if (!DBProperties.isInitialized()) {
            DBProperties.initialize();
        }
        String language = null;
        try {
            language = Context.getThreadContext().getLanguage();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the language from the context", e);
        }
        return DBProperties.getFormatedDBProperty(_key, language, _args);
    }


    /**
     * Get a DBProperty and apply a <code>java.util.Formatter</code> with
     * the given _args on it.
     *
     * @param _key  key the DBProperty will be searched for
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
        } catch (final EFapsException e) {
            DBProperties.LOG.error("not able to read the locale from the context", e);
        } catch (final MissingFormatArgumentException e) {
            DBProperties.LOG.error("wrong format", e);
        }
        return ret;
    }

    /**
     * Method to initialize the Properties.
     */
    public static void initialize()
    {

        synchronized (DBProperties.PROPERTIESCACHE) {
            DBProperties.PROPERTIESCACHE.clear();
        }

        final String sqlStmt = new SQLSelect()
                                    .distinct(true)
                                    .column(0, "PROPKEY")
                                    .column(0, "DEFAULTV")
                                    .column(0, "PROPKEY")
                                    .column(1, "SEQUENCE")
                                    .from("T_ADPROP", 0)
                                    .leftJoin("T_ADPROPBUN", 1, "ID", 0, "BUNDLEID")
                                    .addPart(SQLPart.ORDERBY)
                                    .addColumnPart(1, "SEQUENCE").toString();

        DBProperties.initializeCache(sqlStmt);

        final String sqlStmt2 = new SQLSelect()
                                    .distinct(true)
                                    .column(0, "PROPKEY")
                                    .column(2, "VALUE")
                                    .column(3, "LANG")
                                    .column(1, "SEQUENCE")
                                    .from("T_ADPROP", 0)
                                    .innerJoin("T_ADPROPBUN", 1, "ID", 0, "BUNDLEID")
                                    .innerJoin("T_ADPROPLOC", 2, "PROPID", 0, "ID")
                                    .innerJoin("T_ADLANG", 3, "ID", 2, "LANGID")
                                    .addPart(SQLPart.ORDERBY)
                                    .addColumnPart(3, "LANG")
                                    .addPart(SQLPart.COMMA)
                                    .addColumnPart(1, "SEQUENCE").toString();

        DBProperties.initializeCache(sqlStmt2);
    }

    /**
     * Returns, if the properties are initialized.
     *
     * @return true if initilised, otherwise false
     */
    public static boolean isInitialized()
    {
        return DBProperties.INITIALIZED;
    }

    /**
     * This method is initializing the cache.
     *
     * @param _sqlstmt SQl-Statement to access the database
     */
    private static void initializeCache(final String _sqlstmt)
    {
        String language = "";

        Map<String, String> map = null;
        try {
            final ConnectionResource con = Context.getThreadContext().getConnectionResource();
            final Statement stmt = con.getConnection().createStatement();

            final ResultSet resultset = stmt.executeQuery(_sqlstmt);
            while (resultset.next()) {
                final String propKey = resultset.getString(1).trim();
                final String langTmp = resultset.getString(3).trim();
                if (langTmp.equals(propKey)) {
                    language = DBProperties.DEFAULT;
                } else {
                    language = langTmp;
                }
                map = DBProperties.PROPERTIESCACHE.get(language);
                if (map == null) {
                    map = new HashMap<String, String>();
                    DBProperties.PROPERTIESCACHE.put(language, map);
                }
                map.put(propKey, resultset.getString(2).trim());
            }
            DBProperties.INITIALIZED = true;
            resultset.close();
        } catch (final EFapsException e) {
            DBProperties.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            DBProperties.LOG.error("initialiseCache()", e);
        }
    }
}

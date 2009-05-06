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

package org.efaps.admin.dbproperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * This class reads the Properties for eFaps from the connected Database and
 * holds them in a cache to be accesed fast during normal runtime. <br>
 * The Keys will be read from the database in the order of the Sequence of a
 * Bundle. That gives the possibilty to override the key of a Bundle with the
 * same key of another Bundle by using a higher Sequence.<br>
 * The value returned for a key is searched first in the localised version, if
 * no Value can be found or no localised version for this language is existing
 * than the default value will be returned.
 *
 * @author jmox
 * @version $Id$
 */
public class DBProperties {

  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(DBProperties.class);

  /**
   * value used to identifie the Default inside the Cache
   */
  private static final String DEFAULT = "default";

  /**
   * Cache for the Properties
   */
  private static final Map<String, Map<String, String>> PROPERTIESCACHE =
      new HashMap<String, Map<String, String>>();

  /**
   * are the Properties initialised?
   */
  private static boolean INITIALIZED = false;

  /**
   * Method to find out if a specified key is existing.<br>
   * It is only checked in the default.
   *
   * @param _key
   *                Key to search for
   * @return true if the key exists
   */
  public static boolean hasProperty(final String _key) {

    if (!isInitialized()) {
      initialize();
    }

    return PROPERTIESCACHE.get(DEFAULT).get(_key) != null;
  }

  /**
   * Method that returns the value, depending on the language of the Context,
   * for the given key. <br>
   * The Search for the key, first searches for a localized Version and if not
   * found for a Default. If no value can be found, the key will be returned.
   *
   * @param _key
   *                Key to Search for
   * @return if key exists, the value for the key, otherwise the key
   */
  public static String getProperty(final String _key) {
    if (!isInitialized()) {
      initialize();
    }

    String language = null;

    try {
      language = Context.getThreadContext().getLocale().getLanguage();
    } catch (final EFapsException e) {
      LOG.error("not able to read the language from the context", e);
    }
    return getProperty(_key, language);

  }

  /**
   * Method that returns the value, depending on the parameter _language, for
   * the given key. <br>
   * The Search for the key, first searches for a localized Version and if not
   * found for a Default. If no value can be found, the key will be returned.
   *
   * @param _key
   *                Key to Search for
   * @param _language
   *                language to use
   * @return if key exists, the value for the key, otherwise the key
   */
  public static String getProperty(final String _key, final String _language) {
    if (!isInitialized()) {
      initialize();
    }

    String value = null;

    final Map<String, String> map = PROPERTIESCACHE.get(_language);
    if (map != null) {
      value = map.get(_key);
    }

    if (value == null) {
      final Map<String, String> defaultProps = PROPERTIESCACHE.get(DEFAULT);
      if (defaultProps != null) {
        value = PROPERTIESCACHE.get(DEFAULT).get(_key);
      }
    }

    return (value == null) ? "?? - " + _key + " - ??" : value;

  }

  /**
   * For getting all Properties in a Map
   *
   * @return Map with all Properties
   */
  public Map<String, Map<String, String>> getProperties() {
    return PROPERTIESCACHE;
  }

  /**
   * Method to initialise the Properties
   */
  public static void initialize() {

    synchronized (PROPERTIESCACHE) {
      PROPERTIESCACHE.clear();
    }

    final String sqlStmt =
        " select distinct PROPKEY, DEFAULTV,'"
            + DEFAULT
            + "' as LANG, SEQUENCE "
            + " from T_ADPROP "
            + " inner join T_ADPROPBUN on T_ADPROPBUN.ID = T_ADPROP.BUNDLEID  "
            + " order by SEQUENCE";

    initializeCache(sqlStmt);

    final String sqlStmt2 =
        "select distinct PROPKEY, VALUE, LANG, SEQUENCE from T_ADPROP "
            + " inner join T_ADPROPBUN on T_ADPROPBUN.ID = T_ADPROP.BUNDLEID "
            + " inner join T_ADPROPLOC on T_ADPROPLOC.PROPID = T_ADPROP.ID "
            + " inner join T_ADLANG on T_ADLANG.ID = T_ADPROPLOC.LANGID "
            + " order by LANG, SEQUENCE";

    initializeCache(sqlStmt2);

  }

  /**
   * Returns, if the properties are initialised
   *
   * @return true if initilised, otherwise false
   */
  public static boolean isInitialized() {
    return INITIALIZED;
  }

  /**
   * This method is initialising the cache
   *
   * @param _SQLStmt
   *                SQl-Statment to access the database
   */
  private static void initializeCache(final String _sqlstmt) {

    String value;
    String language = "";

    Map<String, String> map = null;
    try {
      final ConnectionResource con =
          Context.getThreadContext().getConnectionResource();
      final Statement stmt = con.getConnection().createStatement();

      final ResultSet resultset = stmt.executeQuery(_sqlstmt);
      while (resultset.next()) {
        value = resultset.getString(2);
        if (!language.equals(resultset.getString(3).trim())) {
          language = resultset.getString(3).trim();
          map = PROPERTIESCACHE.get(language);
          if (map == null) {
            map = new HashMap<String, String>();
            PROPERTIESCACHE.put(language, map);
          }
        }

        map.put(resultset.getString("PROPKEY").trim(), value.trim());
      }
      INITIALIZED = true;
      resultset.close();
    } catch (final EFapsException e) {

      LOG.error("initialiseCache()", e);
    } catch (final SQLException e) {

      LOG.error("initialiseCache()", e);
    }

  }
}

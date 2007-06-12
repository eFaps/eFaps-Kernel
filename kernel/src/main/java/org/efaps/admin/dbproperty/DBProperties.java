/*
 * Copyright 2003 - 2007 The eFaps Team
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * This class reads the Properties for eFaps from the connected Database and
 * holds them in a cache. It is possible to use a localized Version for the
 * Properties, by setting the Language of the Properties. If no Language is
 * explicitly set the default from the System is used.<br>
 * The value returned is the value from the localised version, if one is
 * existing, otherwise it returns the default value.
 * 
 * @author jmo
 * 
 */
public class DBProperties {
  /**
   * Logger for this class
   */
  private static final Log                                LOG             = LogFactory
                                                                              .getLog(DBProperties.class);

  private static String                                   DEFAULT         = "default";

  /**
   * Cache for the Properties
   */
  private static HashMap<String, HashMap<String, String>> PROPERTIESCACHE = new HashMap<String, HashMap<String, String>>();

  /**
   * are the Properties initialised?
   */
  private static boolean                                  INITIALISED;

  /**
   * Method that returns the value, depending on the language, for the given key
   * 
   * @param _key
   *          Key to Search for
   * @return if key exists, the value for the key, otherwise the key
   */
  public static String getProperty(String _key) {
    if (!isInitialised()) {
      initialise();
    }

    String language = null;
    String value = null;
    try {
      language = Context.getThreadContext().getLocale().getLanguage();
    } catch (EFapsException e) {

      LOG.error("getProperty(String)", e);
    }
    HashMap map = PROPERTIESCACHE.get(language);
    if (map != null) {
      value = (String) map.get(_key);
    }

    if (value == null) {
      value = PROPERTIESCACHE.get(DEFAULT).get(_key);
    }

    return (value == null) ? _key : value;

  }

  /**
   * For getting all Properties in a Map
   * 
   * @return Map with all Properties
   */
  public Map getProperties() {
    return PROPERTIESCACHE;
  }

  /**
   * Method to initialise the Propeties
   */
  public static void initialise() {
    String SQLStmt = " select distinct KEY, DEFAULTV,'"
        + DEFAULT
        + "' as LANG, SEQUENCE from T_ADPROP "
        + " inner join t_adpropbun on t_adpropbun.id = T_ADPROP.bundleid  order by SEQUENCE";

    initialiseCache(SQLStmt);

    SQLStmt = "select distinct key,value,lang,sequence from T_ADPROP "
        + " inner join  t_adpropbun on t_adpropbun.id=T_ADPROP.bundleid "
        + " inner join t_adproploc on t_adproploc.propid= T_ADPROP.id "
        + " inner join T_adlang on T_adlang.id=t_adproploc.langid "
        + " order by lang,SEQUENCE";
    initialiseCache(SQLStmt);

  }

  /**
   * Returns, if the properties are initialised
   * 
   * @return true if initilised, otherwise false
   */
  public static boolean isInitialised() {
    return INITIALISED;
  }

  /**
   * This method is initialising the cache
   * 
   * @param _SQLStmt
   *          SQl-Statment to access the database
   */
  private static void initialiseCache(final String _sqlstmt) {
    ConnectionResource con;
    String value;
    String language = "";

    HashMap<String, String> map = null;
    try {
      con = Context.getThreadContext().getConnectionResource();
      Statement stmt = con.getConnection().createStatement();

      ResultSet rs = stmt.executeQuery(_sqlstmt);
      while (rs.next()) {
        value = rs.getString(2);
        if (!language.equals(rs.getString(3))) {
          language = rs.getString(3);
          map = PROPERTIESCACHE.get(language);
          if (map == null) {
            map = new HashMap<String, String>();
            PROPERTIESCACHE.put(language, map);
          }
        }

        map.put(rs.getString("KEY").trim(), value.trim());
      }
      INITIALISED = true;
    } catch (EFapsException e) {

      LOG.error("initialiseCache()", e);
    } catch (SQLException e) {

      LOG.error("initialiseCache()", e);
    }

  }
}

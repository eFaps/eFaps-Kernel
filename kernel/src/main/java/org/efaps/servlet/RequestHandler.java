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

package org.efaps.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;

/**
 * 
 * @todo description
 * @todo move to webapps project
 * @author tmo
 * @version $Id$
 */
public class RequestHandler extends HttpServlet {

  /**
   * The static variable holds the resource name for the JDBC database
   * connection.
   */
  final private static String RESOURCE_JDBC   = "eFaps/jdbc";

  /**
   * The static variable holds the resource name for the database type.
   */
  final private static String RESOURCE_DBTYPE = "eFaps/dbType";

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @param _config
   */
  public void init(ServletConfig _config) throws ServletException {
    super.init(_config);

    initReplacableMacros("/"
        + _config.getServletContext().getServletContextName() + "/");

    try {

      InitialContext initCtx = new InitialContext();
      javax.naming.Context envCtx = (javax.naming.Context) initCtx
          .lookup("java:comp/env");

      AbstractDatabase dbType = (AbstractDatabase) envCtx
          .lookup(RESOURCE_DBTYPE);
      if (dbType == null) {
        throw new ServletException("no database type defined!");
      }
      Context.setDbType(dbType);

      DataSource ds = (DataSource) envCtx.lookup(RESOURCE_JDBC);
      if (ds == null) {
        throw new ServletException("no SQL data source defined!");
      }
      Context.setDataSource(ds);

      javax.transaction.TransactionManager tm = new org.apache.slide.transaction.SlideTransactionManager();
      tm.begin();
      Context context = Context.newThreadContext(tm.getTransaction(), null,
          null);
      try {
        RunLevel.init("webapp");
        RunLevel.execute();
      } catch (Throwable e) {
        e.printStackTrace();
      }
      tm.rollback();
      context.close();
    } catch (Exception e) {
      e.printStackTrace();

    }

  }

  // ///////////////////////////////////////////////////////////////////////////
  // url macro handler

  /**
   * The static method replaces all known url macros by real urls.
   * 
   * @param _url
   *          url with url macros
   * @return url string with replaces url macros
   * @see #replacableMacros
   * @see #initReplacableMacros
   */
  public static String replaceMacrosInUrl(String _url) {
    String url = _url;
    if (getReplacableMacros() != null) {
      for (Map.Entry<String, String> entry : getReplacableMacros().entrySet()) {
        url = url.replaceAll(entry.getKey(), entry.getValue());
      }
    }
    if (url.indexOf('?') < 0) {
      url += "?";
    }
    url = url.replaceAll("//", "/");
    return url;
  }

  /**
   * Stores the key and the value of the replaceable url macros in the map
   * {@link #replacableMacros} used by method {@link #replaceMacrosInUrl} to
   * create executeable urls.
   * 
   * @param _rootUrl
   *          root url of the application used to replace
   * @see #replaceMacrosInUrl
   * @see #replacableMacros
   */
  private static void initReplacableMacros(String _rootUrl) {
    setReplacableMacros(new HashMap<String, String>());
    replacableMacros.put("\\$\\{SERVLETURL\\}", _rootUrl + "request");
    replacableMacros.put("\\$\\{COMMONURL\\}", _rootUrl + "common");
    replacableMacros.put("\\$\\{ROOTURL\\}", _rootUrl);
    replacableMacros.put("\\$\\{ICONURL\\}", _rootUrl + "images");
  }

  /**
   * The static map stores all replacable url macros.
   * 
   * @see #getReplacableMacros
   * @see #setReplacableMacros
   */
  private static Map<String, String> replacableMacros = null;

  /**
   * This is the getter method for static variable {@link #replacableMacros}.
   * 
   * @return returns value of static variable {@link #replacableMacros}.
   * @see #replacableMacros
   * @see #setReplacableMacros
   */
  private static Map<String, String> getReplacableMacros() {
    return replacableMacros;
  }

  /**
   * This is the setter method for static variable {@link #replacableMacros}.
   * 
   * @param _replacableMacros
   *          new value for static variable {@link #replacableMacros}
   * @see #replacableMacros
   * @see #getReplacableMacros
   */
  private static void setReplacableMacros(Map<String, String> _replacableMacros) {
    replacableMacros = _replacableMacros;
  }

}

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

package org.efaps.maven.jetty.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.mortbay.jetty.servlet.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class ServerDefinition {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(ServerDefinition.class);

  /**
   * List of all Filters used in this server definition.
   */
  private final List<FilterDefinition> filters = new ArrayList<FilterDefinition>();

  /**
   * List of all servlets used in this server definition.
   */
  private final List<ServletDefinition> servlets = new ArrayList<ServletDefinition>();

  /**
   * 
   * @param _url  path to the XML file withi the server definition
   * @return
   */
  public static ServerDefinition read(final String _url)  {
    ServerDefinition ret = null;
    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("server", ServerDefinition.class);

      digester.addObjectCreate("server/filter", FilterDefinition.class);
      digester.addSetNext("server/filter", "addFilter");
      digester.addCallMethod("server/filter", "setName", 1);
      digester.addCallParam("server/filter", 0, "name");
      digester.addCallMethod("server/filter", "setClassName", 1);
      digester.addCallParam("server/filter", 0, "classname");
      digester.addCallMethod("server/filter", "setPathSpec", 1);
      digester.addCallParam("server/filter", 0, "path");
      digester.addCallMethod("server/filter/parameter", "addIniParam", 2);
      digester.addCallParam("server/filter/parameter", 0, "key");
      digester.addCallParam("server/filter/parameter", 1);

      digester.addObjectCreate("server/servlet", ServletDefinition.class);
      digester.addSetNext("server/servlet", "addServlet");
      digester.addCallMethod("server/servlet", "setName", 1);
      digester.addCallParam("server/servlet", 0, "name");
      digester.addCallMethod("server/servlet", "setClassName", 1);
      digester.addCallParam("server/servlet", 0, "classname");
      digester.addCallMethod("server/servlet", "setPathSpec", 1);
      digester.addCallParam("server/servlet", 0, "path");
      digester.addCallMethod("server/servlet", "setInitOrder", 1, new Class[]{Integer.class});
      digester.addCallParam("server/servlet", 0, "initorder");
      digester.addCallMethod("server/servlet/parameter", "addIniParam", 2);
      digester.addCallParam("server/servlet/parameter", 0, "key");
      digester.addCallParam("server/servlet/parameter", 1);

      ret = (ServerDefinition) digester.parse(_url);

    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  /**
   * Updates the context handler (defining the server) by appending servlets
   * and filters.
   *
   * @param _handler  context handler used to add filters / servlets
   * @see FilterDefinition#updateServer(Context)
   * @see ServletDefinition#updateServer(Context)
   */
  public void updateServer(final Context _handler)  {
    for (final FilterDefinition filter : this.filters)  {
      filter.updateServer(_handler);
    }
    for (final ServletDefinition servlet : this.servlets)  {
      servlet.updateServer(_handler);
    }
  }

  /**
   * Adds a new filter definition to the list of filter definition.
   *
   * @param _filter filter to add to the list of filters
   * @see #filters
   */
  public void addFilter(final FilterDefinition _filter)  {
    this.filters.add(_filter);
  }

  /**
   * Adds a new servlet definition to the list of servlet definitions.
   *
   * @param _servlet  servlet to add to the list of servlets
   * @see #servlets
   */
  public void addServlet(final ServletDefinition _servlet)  {
    this.servlets.add(_servlet);
  }
}

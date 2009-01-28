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

package org.efaps.ui.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.util.RequestHandler;

/**
 * @todo description
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class RequestHandlerInitServlet extends HttpServlet {

  /**
   *
   */
  private static final long serialVersionUID = 7212518317632161066L;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @param _config
   */
  @Override
  public void init(ServletConfig _config) throws ServletException {
    super.init(_config);

    RequestHandler.initReplacableMacros("/"
        + _config.getServletContext().getServletContextName()
        + "/");

    try {
      Context.begin();
      try {
        RunLevel.init("webapp");
        RunLevel.execute();
      } catch (final Throwable e) {
        e.printStackTrace();
      }
      Context.rollback();
    } catch (final Exception e) {
      e.printStackTrace();
    }

  }

}

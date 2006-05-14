/*
 * Copyright 2005 The eFaps Team
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The servlet logs out from the current eFaps application.
 *
 * @author tmo
 * @version $Rev$
 */
public class LogoutServlet extends HttpServlet  {

  /**
   * User wants to logout from eFaps. The session variable
   * {@link SecurityFilter#SESSIONPARAM_LOGIN_NAME} is removed and redirect to
   * the root directory is done.
   *
   * @param _req request variable
   * @param _res response variable
   * @see #checkLogin
   */
  protected void doGet(final HttpServletRequest _req, final HttpServletResponse _res) throws ServletException, IOException  {
    HttpSession session = _req.getSession(true);
    session.removeAttribute(SecurityFilter.SESSIONPARAM_LOGIN_NAME);
    _res.setContentType("text/html");
    _res.sendRedirect(RequestHandler.replaceMacrosInUrl("${ROOTURL}"));
  }
}
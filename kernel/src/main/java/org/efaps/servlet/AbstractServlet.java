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
 */

package org.efaps.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import org.efaps.admin.user.Person;
import org.efaps.db.Context;

public abstract class AbstractServlet extends HttpServlet  {

  /**
   * Name of the session variable for the login name.
   */
  final public static String SESSIONPARAM_LOGIN_NAME =   "login.name";

  /**
   * Check, if the session variable {@link #SESSIONPARAM_LOGIN_NAME} is set.
   * If not, user is not logged in. Normally then a redirect to login page
   * is made with method {@link #doRedirect2Login}.
   *
   * @param _req request variable
   * @return <i>true</i> if user logged in, otherwise <i>false</i>
   */
  protected boolean isLoggedIn(HttpServletRequest _req)  {
    boolean ret = false;

    HttpSession session = _req.getSession(true);
    String userName = (String)session.getAttribute(SESSIONPARAM_LOGIN_NAME);
    if (userName != null)  {
      ret = true;
    }
    return ret;
  }

  /**
   *
   */
  public Context createNewContext(HttpServletRequest _req) throws ServletException  {
    HttpSession session = _req.getSession(true);
    String userName = (String)session.getAttribute(SESSIONPARAM_LOGIN_NAME);
    if (userName==null)  {
throw new ServletException("You are not logged in!");
    }
    try  {
      return new Context(Person.get(userName));
    } catch (Exception e)  {
throw new ServletException(e);
    }
  }
}

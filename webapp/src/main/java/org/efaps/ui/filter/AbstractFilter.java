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

package org.efaps.ui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractFilter implements Filter {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables
  /**
   * Name of the InitParameter variable for the login name.
   */
  private final static String SESSIONPARAM_LOGIN_NAME = "login.name";

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables
  /**
   * Name of the session variable for the login name.
   */
  private String sessionParameterLoginName = "org.efaps.login.name";

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Called by the web container to indicate to a filter that it is being placed
   * into service. The servlet container calls the init method exactly once
   * after instantiating the filter. The init method must complete successfully
   * before the filter is asked to do any filtering work. The web container
   * cannot place the filter into service if the init method either 1.Throws a
   * ServletException 2.Does not return within a time period defined by the web
   * container // sets the login handler
   *
   * @param _filterConfig
   *                filter configuration instance
   * @see #INIT_PARAM_TITLE
   * @see #title
   * @see #INIT_PARAM_APPLICATION
   * @see #loginhandler
   * @todo description
   */
  public void init(final FilterConfig _filterConfig) throws ServletException {
    final String loginName = _filterConfig.getInitParameter(SESSIONPARAM_LOGIN_NAME);
    if (loginName != null) {
      this.sessionParameterLoginName = loginName;
    }
  }

  /**
   * Destroys the filter. Is an empty method in this implementation which could
   * be overwritten by derived classes.<br/> The method is called by the web
   * container to indicate to a filter that it is being taken out of service.
   * This method is only called once all threads within the filter's doFilter
   * method have exited or after a timeout period has passed. After the web
   * container calls this method, it will not call the doFilter method again on
   * this instance of the filter.<br/> This method gives the filter an
   * opportunity to clean up any resources that are being held (for example,
   * memory, file handles, threads) and make sure that any persistent state is
   * synchronized with the filter's current state in memory.
   */
  public void destroy() {
  }

  /**
   * First the filtes tests, if the http(s) protokoll is used. If the request is
   * not implementing the {@link HttpServletRequest} and the response is not
   * implementing the {@link HttpServletResponse} interface, a
   * {@link ServletException} is thrown.<br/>
   *
   * @throws ServletException
   *                 if the request and response does not use the http(s)
   *                 protokoll
   * @see HttpServletRequest
   * @see HttpServletResponse
   * @see #checkLogin
   */
  public void doFilter(final ServletRequest _request,
                       final ServletResponse _response, final FilterChain _chain)
                                                                                 throws IOException,
                                                                                 ServletException {

    if ((_request instanceof HttpServletRequest)
        && (_response instanceof HttpServletResponse)) {

      doFilter((HttpServletRequest) _request, (HttpServletResponse) _response,
          _chain);
    } else {
      throw new ServletException("request not allowed");
    }
  }

  abstract protected void doFilter(final HttpServletRequest _request,
                                   final HttpServletResponse _response,
                                   final FilterChain _chain)
                                                            throws IOException,
                                                            ServletException;

  /**
   * Check, if the session variable {@link #sessionParameterLoginName} is set.
   * If not, user is not logged in. Normally then a redirect to login page is
   * made with method {@link #doRedirect2Login}.
   *
   * @param _request
   *                http servlet request variable
   * @return <i>true</i> if user logged in, otherwise <i>false</i>
   */
  protected boolean isLoggedIn(final HttpServletRequest _request) {
    return getLoggedInUser(_request) != null ? true : false;
  }

  /**
   * Stores the logged in user name in a session attribute of the http servlet
   * request. If the new user name is <code>null</code>, the session
   * attribute is removed.
   *
   * @param _request
   *                http servlet request
   * @param _userName
   *                name of logged in user to set (or null if not defined)
   */
  protected void setLoggedInUser(final HttpServletRequest _request,
                                 final String _userName) {
    if (_userName == null) {
      _request.getSession(true).removeAttribute(this.sessionParameterLoginName);
    } else {
      _request.getSession(true).setAttribute(this.sessionParameterLoginName,
          _userName);
    }
  }

  protected String getLoggedInUser(final HttpServletRequest _request) {
    return (String) _request.getSession(true).getAttribute(
        this.sessionParameterLoginName);
  }
}

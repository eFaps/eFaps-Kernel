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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.util.RequestHandler;

/**
 * @author tmo
 * @version $Id: FormBasedAuthenticationFilter.java 1367 2007-09-22 12:25:31Z
 *          tmo $
 */
public class FormBasedAuthenticationFilter extends AbstractAuthenticationFilter {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(FormBasedAuthenticationFilter.class);

  /**
   * Name of the session variable for the login forward (after the login is done
   * this is the next page).
   */
  final public static String SESSIONPARAM_LOGIN_FORWARD = "login.forward";

  /**
   * The string is name of the parameter used to define the url login page.
   *
   * @see #init
   */
  final public static String INIT_PARAM_URL_LOGIN_PAGE = "urlLoginPage";

  /**
   * The string is name of the parameter used to define the url where
   * unprotected content lies.
   *
   * @see #init
   */
  final public static String INIT_PARAM_URL_IGNORE = "urlIgnore";

  /**
   * The string is name of the parameter used to define the url login used to
   * authenticate.
   *
   * @see #init
   */
  final public static String INIT_PARAM_URL_LOGIN = "urlLogin";

  /**
   * The string is name of the parameter used to define the url login used to
   * authenticate.
   *
   * @see #init
   */
  final public static String INIT_PARAM_URL_LOGOUT = "urlLogout";

  /**
   * The string is name of the parameter used to define the url to which is
   * forwared after correct authentication.
   *
   * @see #init
   */
  final private static String INIT_PARAM_URL_FORWARD = "urlForward";

  final public static String INIT_PARAM_LOGIN_PARAM_NAME = "loginParamName";

  final public static String INIT_PARAM_LOGIN_PARAM_PASSWORD =
      "loginParamPassword";

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All uris which are not needed filtered by security check (password check)
   * are stored in this set variable.
   *
   * @see #init
   */
  private final Set<String> exludeUris = new HashSet<String>();

  /**
   * The string is URI to which a forward must be made if the user is not logged
   * in. The default value is <code>login.jsp</code>
   *
   * @see #init
   */
  private String urlNotLoggedInForward = null;

  /**
   * The string stores the URL of the login request. The default value is
   * <code>login</code> and set in method {@link #init}.
   *
   * @see #init
   */
  private String urlLogin = null;

  /**
   * The string stores the URL of the logout request. The default value is
   * <code>logout</code> and set in method {@link #init}.
   *
   * @see #init
   */
  private String urlLogout = null;

  /**
   * The forward URL used if the login is correct and the next page must be
   * shown.
   *
   * @see #init
   */
  private String urlForward = "${COMMONURL}/Main.jsf";

  /**
   * name of the name parameter
   */
  private String paramLoginName = null;

  /**
   * name of the password parameter
   */
  private String paramLoginPassword = null;

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Called by the web container to indicate to a filter that it is being placed
   * into service. The servlet container calls the init method exactly once
   * after instantiating the filter. The init method must complete successfully
   * before the filter is asked to do any filtering work. The web container
   * cannot place the filter into service if the init method either 1.Throws a
   * ServletException 2.Does not return within a time period defined by the web
   * container
   */
  @Override
  public void init(final FilterConfig _filterConfig) throws ServletException {
    super.init(_filterConfig);
    final String root =
        "/" + _filterConfig.getServletContext().getServletContextName() + "/";

    this.urlNotLoggedInForward =
        _filterConfig.getInitParameter(INIT_PARAM_URL_IGNORE);
    if ((this.urlNotLoggedInForward != null)
        && (this.urlNotLoggedInForward.length() > 0)) {
      this.exludeUris.add((root + this.urlNotLoggedInForward).replaceAll("//+",
          "/"));
    }

    // define URL if the user is not logged in
    this.urlNotLoggedInForward =
        _filterConfig.getInitParameter(INIT_PARAM_URL_LOGIN_PAGE);
    if ((this.urlNotLoggedInForward == null)
        || (this.urlNotLoggedInForward.length() == 0)) {
      this.urlNotLoggedInForward = "login.jsp";
    }
    this.urlNotLoggedInForward =
        ("/" + this.urlNotLoggedInForward).replaceAll("//+", "/");
    this.exludeUris.add((root + this.urlNotLoggedInForward).replaceAll("//+",
        "/"));

    // define URL used to authenticate
    this.urlLogin = _filterConfig.getInitParameter(INIT_PARAM_URL_LOGIN);
    if ((this.urlLogin == null) || (this.urlLogin.length() == 0)) {
      this.urlLogin = "login";
    }
    this.urlLogin = (root + "/" + this.urlLogin).replaceAll("//+", "/");

    // define URL used to log out
    this.urlLogout = _filterConfig.getInitParameter(INIT_PARAM_URL_LOGOUT);
    if ((this.urlLogout == null) || (this.urlLogout.length() == 0)) {
      this.urlLogout = "logout";
    }
    this.urlLogout = (root + "/" + this.urlLogout).replaceAll("//+", "/");

    //
    this.urlForward = _filterConfig.getInitParameter(INIT_PARAM_URL_FORWARD);
    if ((this.urlForward == null) || (this.urlForward.length() == 0)) {
      this.urlForward = "${COMMONURL}/Main.jsf";
    }

    // define login parameter name for name
    this.paramLoginName =
        _filterConfig.getInitParameter(INIT_PARAM_LOGIN_PARAM_NAME);
    if ((this.paramLoginName == null) || (this.paramLoginName.length() == 0)) {
      this.paramLoginName = "name";
    }

    // define login parameter name for password
    this.paramLoginPassword =
        _filterConfig.getInitParameter(INIT_PARAM_LOGIN_PARAM_PASSWORD);
    if ((this.paramLoginPassword == null)
        || (this.paramLoginPassword.length() == 0)) {
      this.paramLoginPassword = "password";
    }

    // hack
    // this.exludeUris.add((root + "/servlet/login").replaceAll("//+", "/"));
  }

  /**
   * If the current user is already logged in, nothing is filtered.
   *
   * @see #doAuthenticate
   */
  @Override
  protected void doFilter(final HttpServletRequest _request,
                          final HttpServletResponse _response,
                          final FilterChain _chain) throws IOException,
                                                   ServletException {
    final String uri = _request.getRequestURI().replaceAll("//+", "/");
    // logout
    if (this.urlLogout.equals(uri)) {
      // setLoggedInUser(_request, null);
      // _request.getSession(true).removeAttribute(SESSIONPARAM_LOGIN_FORWARD);
      // remove all http session attributes! it's a logout!
      final HttpSession session = _request.getSession();
      for (final Enumeration<?> e = session.getAttributeNames(); e.hasMoreElements();) {
        session.removeAttribute((String) e.nextElement());
      }
      _request.getRequestDispatcher("/").forward(_request, _response);
      // otherwise normal behaviour
    } else {
      super.doFilter(_request, _response, _chain);
    }
  }

  /**
   * @see #doSendLoginFrameNotCorrect
   */
  @Override
  protected void doAuthenticate(final HttpServletRequest _request,
                                final HttpServletResponse _response,
                                final FilterChain _chain) throws IOException,
                                                         ServletException {
    final String uri = _request.getRequestURI().replaceAll("//+", "/");
    boolean exclude = false;
    for (final String excludeUri : this.exludeUris) {
      exclude = exclude || uri.startsWith(excludeUri);
    }

    if (exclude) {
      _chain.doFilter(_request, _response);
    } else if (this.urlLogin.equals(uri)) {
      final String name = _request.getParameter(this.paramLoginName);
      final String passwd = _request.getParameter(this.paramLoginPassword);
      if (checkLogin(name, passwd)) {
        setLoggedInUser(_request, name);
        _response.setContentType("text/html");

        String newUrl =
            (String) _request.getSession().getAttribute(
                SESSIONPARAM_LOGIN_FORWARD);
        if (newUrl == null) {
          newUrl = RequestHandler.replaceMacrosInUrl(this.urlForward);
        } else {
          _request.getSession().removeAttribute(SESSIONPARAM_LOGIN_FORWARD);
        }
        _response.sendRedirect(newUrl);
      } else {
        doSendLoginFrameNotCorrect(_request, _response);
      }
    } else {
      if (_request.getRequestURI().endsWith("common/MenuTree.jsp")) {
        String markUrl = _request.getRequestURI();
        if (_request.getQueryString() != null) {
          markUrl += "?" + _request.getQueryString();
        }
        _request.getSession().setAttribute(SESSIONPARAM_LOGIN_FORWARD, markUrl);
      }
      _request.getRequestDispatcher(this.urlNotLoggedInForward).forward(
          _request, _response);
    }
  }

  /**
   * This page is sent if the login is not correct.
   *
   * @param _req
   *                HttpServletRequest that encapsulates the request to the
   *                servlet
   * @param _res
   *                HttpServletResponse that encapsulates the response from the
   *                servlet
   */
  protected void doSendLoginFrameNotCorrect(final HttpServletRequest _req,
                                            final HttpServletResponse _res)
                                                                           throws ServletException,
                                                                           IOException {
    _res.setContentType("text/html");
    PrintWriter pW = null;
    try {
      pW = _res.getWriter();
      pW.println("<html>"
          + "<head>"
          + "<title>eFaps</title>"
          + "</head>"
          + "<script type=\"text/javascript\">"
          + "function wrongLogin() {"
          + "}"
          + "</script>"
          + "<frameset>"
          + "<frame src=\""
          + _req.getContextPath()
          + "\" name=\"Login\">"
          + "</frameset>"
          + "</html>");
    } catch (final IOException e) {
      throw e;
    } catch (final Exception e) {
      LOG.error("Could not write the frame for not correct login.", e);
      throw new ServletException(e);
    }
    finally {
      pW.close();
    }
  }
}

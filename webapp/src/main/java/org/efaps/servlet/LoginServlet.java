/*
 * Copyright 2006 The eFaps Team
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
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;

/**
 * The servlet logs in a user with name and password. The name and password is
 * checked with the help of Java Authentication and Authorization Service
 * (short JAAS).<br/>
 * The login servlet itself could handle the HTTP GET and POST operations
 * (see {@link #doGet} and {@link #doPost}).
 *
 * @author tmo
 * @version $Id$
 */
public class LoginServlet extends HttpServlet  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private static Log LOG = LogFactory.getLog(LoginServlet.class);

  /**
   * Name of the servlet initialise parameter for the application.
   */
  final private static String INIT_PARAM_APPLICATION = "application";

  /**
   * Name of the servlet initialise parameter for the application.
   */
  final private static String INIT_PARAM_FORWARD_URL = "forwardURL";

  /**
   * name of the name parameter
   */
  final private static String PARAM_USERNAME =        "name";

  /**
   * name of the password parameter
   */
  final private static String PARAM_PASSWORD =        "password";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The forward URL used if the login is correct and the next page must be
   * shown.
   *
   * @see #init
   * @see #doGet
   */
  private String forwardURL = "${COMMONURL}/Main.jsf";

  /**
   * @see #init
   * @see #doGet
   */
  private LoginHandler loginHandler = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The login servlet is initialised. The application name in
   * {@link #application} and the forward URL after login in
   * {@link #forwardURL} is set.
   *
   * @param _config
   * @see #INIT_PARAM_APPLICATION
   * @see #application
   * @see #INIT_PARAM_FORWARD_URL
   * @see #forwardURL
   */
  public void init(final ServletConfig _config) throws ServletException  {
    super.init(_config);

    String applInit = _config.getInitParameter(INIT_PARAM_APPLICATION);
    this.loginHandler = new LoginHandler(applInit);

    String forwInit = _config.getInitParameter(INIT_PARAM_FORWARD_URL);
    if (forwInit != null)  {
      this.forwardURL = forwInit;
    }
  }

  /**
   * Performs the HTTP GET operation; calls only method {@link #doHandle}.
   * The method is needed to overwrite the default implementation which reports
   * an HTTP BAD_REQUEST error.
   *
   * @param _req  HttpServletRequest that encapsulates the request to the 
   *              servlet
   * @param _res  HttpServletResponse that encapsulates the response from the 
   *              servlet
   * @see #doHandle
   */
  protected void doGet(final HttpServletRequest _req, 
                       final HttpServletResponse _res) 
                                        throws ServletException, IOException  {
    doHandle(_req, _res);
  }

  /**
   * Performs the HTTP POST operation; calls only method {@link #doHandle}.
   * The method is needed to overwrite the default implementation which reports
   * an HTTP BAD_REQUEST error.
   *
   * @param _req  HttpServletRequest that encapsulates the request to the 
   *              servlet
   * @param _res  HttpServletResponse that encapsulates the response from the 
   *              servlet
   * @see #doHandle
   */
  protected void doPost(final HttpServletRequest _req, 
                        final HttpServletResponse _res) 
                                        throws ServletException, IOException  {
    doHandle(_req, _res);
  }

  /**
   * User wants to login into eFaps. The user name and password is checked.
   * User name is stored in session variable 
   * {@link SecurityFilter#SESSIONPARAM_LOGIN_NAME}. After login a redirect to 
   * the "common/Main.jsf" is made.<br/>
   * The post parameter names are {@link #PARAM_USERNAME} and
   * {@link #PARAM_PASSWORD}.
   *
   * @param _req  HttpServletRequest that encapsulates the request to the 
   *              servlet
   * @param _res  HttpServletResponse that encapsulates the response from the 
   *              servlet
   * @see #doGet
   * @see #doPost
   * @see #doSendLoginFrameNotCorrect
   */
  protected void doHandle(final HttpServletRequest _req, 
                          final HttpServletResponse _res) 
                                        throws ServletException, IOException  {
    PrintWriter out = _res.getWriter();

    String name = _req.getParameter(PARAM_USERNAME);
    String passwd = _req.getParameter(PARAM_PASSWORD);

    if (loginHandler.checkLogin(name, passwd) != null)  {
      HttpSession session = _req.getSession(true);
      session.setAttribute(SecurityFilter.SESSIONPARAM_LOGIN_NAME, name);

      _res.setContentType("text/html");

      String newUrl = (String) _req.getSession().getAttribute(SecurityFilter.SESSIONPARAM_LOGIN_FORWARD);
      if (newUrl == null)  {
        newUrl = RequestHandler.replaceMacrosInUrl(this.forwardURL);
      } else  {
        _req.getSession().removeAttribute(SecurityFilter.SESSIONPARAM_LOGIN_FORWARD);
      }
      _res.sendRedirect(newUrl);
    } else  {
      doSendLoginFrameNotCorrect(_req, _res);
    }
  }

  /**
   * This page is sent if the login is not correct.
   *
   * @param _req  HttpServletRequest that encapsulates the request to the 
   *              servlet
   * @param _res  HttpServletResponse that encapsulates the response from the 
   *              servlet
   */
  protected void doSendLoginFrameNotCorrect(final HttpServletRequest _req, 
                                            final HttpServletResponse _res) 
                                        throws ServletException, IOException  {
    _res.setContentType("text/html");
    PrintWriter pW = null;
    try  {
      pW = _res.getWriter();
      pW.println(
          "<html>"+
            "<head>"+
              "<title>eFaps</title>"+
            "</head>"+
            "<script type=\"text/javascript\">"+
              "function wrongLogin() {"+
              "}"+
            "</script>"+
            "<frameset>"+
            "<frame src=\""+_req.getContextPath()+"\" name=\"Login\">"+
          "</frameset>"+
        "</html>"
      );
    } catch(IOException e)  {
      throw e;
    } catch (Exception e)  {
      LOG.error("Could not write the frame for not correct login.", e);
      throw new ServletException(e);
    } finally  {
      pW.close();
    }
  }
}
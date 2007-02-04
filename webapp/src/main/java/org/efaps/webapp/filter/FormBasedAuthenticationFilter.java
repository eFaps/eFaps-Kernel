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

package org.efaps.webapp.filter;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tmo
 * @version $Id$
 */
public class FormBasedAuthenticationFilter 
                                        extends AbstractAuthenticationFilter  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(FormBasedAuthenticationFilter.class);

  /**
   * Name of the session variable for the login forward (after the login is
   * done this is the next page).
   */
  final public static String SESSIONPARAM_LOGIN_FORWARD = "login.forward";

  /**
   * The string is name of the parameter used to define the url login page.
   */
  final public static String INIT_PARAM_URL_LOGIN_PAGE = "urlLoginPage";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables


  /**
   * All uris which are not needed filtered by security check (password check)
   * are stored in this set variable.
   */
  private final Set exludeUris = new HashSet();

  /**
   * The string is URI to which a forward must be made if the user is not
   * logged in.
   */
  private String notLoggedInForward = null;

  /**
   *
      Called by the web container to indicate to a filter that it is being placed into service. The servlet container calls the init method exactly once after instantiating the filter. The init method must complete successfully before the filter is asked to do any filtering work.

      The web container cannot place the filter into service if the init method either
      1.Throws a ServletException
      2.Does not return within a time period defined by the web container

   */
  public void init(final FilterConfig _filterConfig) throws ServletException  {
    super.init(_filterConfig);
    String root = "/" + _filterConfig.getServletContext().getServletContextName() + "/";

    this.notLoggedInForward = "/" + _filterConfig.getInitParameter(INIT_PARAM_URL_LOGIN_PAGE);

    if ((this.notLoggedInForward == null) || (this.notLoggedInForward.length() == 0))  {
      throw new ServletException("Init parameter "
          + "'" + INIT_PARAM_URL_LOGIN_PAGE + "' not defined");
    }

    this.exludeUris.add((root + this.notLoggedInForward).replaceAll("//+", "/"));
    this.exludeUris.add((root + "/servlet/login").replaceAll("//+", "/"));
  }

  /**
The doFilter method of the Filter is called by the container each time a request/response pair is passed through the chain due to a client request for a resource at the end of the chain. The FilterChain passed in to this method allows the Filter to pass on the request and response to the next entity in the chain.

A typical implementation of this method would follow the following pattern:-
1. Examine the request
2. Optionally wrap the request object with a custom implementation to filter content or headers for input filtering
3. Optionally wrap the response object with a custom implementation to filter content or headers for output filtering
4. a) Either invoke the next entity in the chain using the FilterChain object (chain.doFilter()),
4. b) or not pass on the request/response pair to the next entity in the filter chain to block the request processing
5. Directly set headers on the response after invocation of the next entity in ther filter chain.
   *
   * @todo remove hard coded proof of the MenuTree.jsp to set the forwarding url
   */
/*  public void doFilter(ServletRequest _request, ServletResponse _response,
                     FilterChain _chain) throws IOException, ServletException  {

HttpServletRequest httpRequest = (HttpServletRequest) _request;
System.out.println("------ filter doFilter="+_request.getAttributeNames());

System.out.println("       fitler output");
for (java.util.Enumeration e = _request.getAttributeNames() ; e.hasMoreElements() ;) {
         System.out.println(e.nextElement());

}
System.out.println("        filter doFilter="+_request.getParameterMap());
System.out.println("        filter getScheme() ="+_request.getScheme() );
System.out.println("        filter getServerName() ="+_request.getServerName() );
System.out.println("        filter getServerName() ="+httpRequest.getContextPath() );
System.out.println("        filter getAuthType()  ="+httpRequest.getAuthType()  );
System.out.println("        filter getRequestURI()  ="+httpRequest.getRequestURI()+":");
String uri = httpRequest.getRequestURI();

    if (isLoggedIn(httpRequest))  {
      String userName = (String)httpRequest.getSession().getAttribute(SESSIONPARAM_LOGIN_NAME);
      doFilter(userName, httpRequest, _response, _chain);
    } else if (this.exludeUris.contains(uri))  {
      doFilter(null, _request, _response, _chain);
    } else  {
      if (httpRequest.getRequestURI().endsWith("common/MenuTree.jsp"))  {
        String markUrl = httpRequest.getRequestURI();
        if (httpRequest.getQueryString() != null)  {
          markUrl += "?" + httpRequest.getQueryString();
        }
        httpRequest.getSession().setAttribute(SESSIONPARAM_LOGIN_FORWARD, markUrl);
      }
      _request.getRequestDispatcher(this.notLoggedInForward).forward(_request, _response);
    }
  }
*/

  /**
   *
   */
  protected void doAuthenticate(final HttpServletRequest _request,
                                final HttpServletResponse _response,
                                final FilterChain _chain)
                                throws IOException, ServletException  {
    String uri = _request.getRequestURI();
    if (isLoggedIn(_request))  {
//      String userName = (String)httpRequest.getSession().getAttribute(SESSIONPARAM_LOGIN_NAME);
//      doFilter(userName, httpRequest, _response, _chain);
    } else if (this.exludeUris.contains(uri))  {
//      doFilter(null, _request, _response, _chain);
    } else  {
      if (_request.getRequestURI().endsWith("common/MenuTree.jsp"))  {
        String markUrl = _request.getRequestURI();
        if (_request.getQueryString() != null)  {
          markUrl += "?" + _request.getQueryString();
        }
        _request.getSession().setAttribute(SESSIONPARAM_LOGIN_FORWARD, markUrl);
      }
      _request.getRequestDispatcher(this.notLoggedInForward)
              .forward(_request, _response);
    }
  }
}
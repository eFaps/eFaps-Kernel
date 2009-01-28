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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

/**
 * The filter is used to make basic access authentication defined in RFC 2617.
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class BasicAuthenticationFilter extends AbstractAuthenticationFilter {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * The string is name of the parameter used to define the title.
   *
   * @see #init
   */
  final private static String INIT_PARAM_TITLE = "title";

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Store the title which is shown in the realm dialog on the client side.
   *
   * @see #init
   * @see #doFilter
   */
  private String              title            = "eFaps";

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
   * Called by the web container to indicate to a filter that it is being placed
   * into service. The servlet container calls the init method exactly once
   * after instantiating the filter. The init method must complete successfully
   * before the filter is asked to do any filtering work.
   *
   * The web container cannot place the filter into service if the init method
   * either 1.Throws a ServletException 2.Does not return within a time period
   * defined by the web container
   *
   * @param _filterConfig
   *          filter configuration instance
   * @see #INIT_PARAM_TITLE
   * @see #title
   * @see #INIT_PARAM_APPLICATION
   * @see #loginhandler
   * @todo description
   */
  @Override
  public void init(final FilterConfig _filterConfig) throws ServletException {
    super.init(_filterConfig);
    // sets the title
    final String title = _filterConfig.getInitParameter(INIT_PARAM_TITLE);
    if (title != null) {
      this.title = title;
    }
  }

  /**
   * If the user does not make authentication, the header for basic
   * authentication is sent to the client. After the client makes the
   * authentication, the name and password is checked with method
   * {@link AbstractAuthenticationFilter#checkLogin}.<br/> If the
   * authentication fails, the header for basic authentication is sent again to
   * the used, otherwise the nothing is filtered anymore.
   *
   */
  @Override
  protected void doAuthenticate(final HttpServletRequest _request,
                                final HttpServletResponse _response,
                                final FilterChain _chain) throws IOException,
                                                         ServletException {
    final String header = _request.getHeader("Authorization");

    if (header == null) {
      _response.setHeader("WWW-Authenticate", "Basic realm=\"" + this.title
          + "\"");
      _response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    } else {
      final String encoded = header.substring(header.indexOf(" ") + 1);
      final String decoded = new String(Base64.decodeBase64(encoded.getBytes()));
      final String name = decoded.substring(0, decoded.indexOf(":"));
      final String passwd = decoded.substring(decoded.indexOf(":") + 1);
      if (checkLogin(name, passwd)) {
        setLoggedInUser(_request, name);
        _chain.doFilter(_request, _response);
      } else {
        _response.setHeader("WWW-Authenticate", "Basic realm=\"" + this.title
            + "\"");
        _response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
  }
}

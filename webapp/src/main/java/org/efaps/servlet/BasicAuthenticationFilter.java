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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class BasicAuthenticationFilter implements Filter  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * The string is name of the parameter used to define the url login page.
   */
  final public static String INIT_PARAM_URL_LOGIN_PAGE = "urlLoginPage";

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
      Called by the web container to indicate to a filter that it is being placed into service. The servlet container calls the init method exactly once after instantiating the filter. The init method must complete successfully before the filter is asked to do any filtering work.

      The web container cannot place the filter into service if the init method either
      1.Throws a ServletException
      2.Does not return within a time period defined by the web container

   */
  public void init(final FilterConfig _filterConfig) throws ServletException  {

  }
  
 /**
   * Destroys the filter. Is an empty method in this implementation.
Called by the web container to indicate to a filter that it is being taken out of service. This method is only called once all threads within the filter's doFilter method have exited or after a timeout period has passed. After the web container calls this method, it will not call the doFilter method again on this instance of the filter.

This method gives the filter an opportunity to clean up any resources that are being held (for example, memory, file handles, threads) and make sure that any persistent state is synchronized with the filter's current state in memory.
   */
  public void destroy()  {
System.out.println("------ filter destroxy");
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
   * If the request is not implementing the {@link HttpServletRequest} and the
   * response is not implementing the {@link HttpServletResponse} interface,
   * a {@link ServletException} is thrown.
   *
   *
   * @throws ServletException if the request and response does not use the 
   *                          http(s) protokoll
   * @see HttpServletRequest
   * @see HttpServletResponse
   */
  public void doFilter(final ServletRequest _request, 
                       final ServletResponse _response,
                       final FilterChain _chain) throws IOException, ServletException  {
    
    if ((_request instanceof HttpServletRequest) 
        && (_response instanceof HttpServletResponse))  {

      HttpServletRequest httpRequest = (HttpServletRequest) _request;
      HttpServletResponse httpResponse = (HttpServletResponse) _response;
      
      String header = httpRequest.getHeader("Authorization");

      if (header == null)  {
        httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"eFaps WebDAV\"");
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      } else  {
        String encoded = header.substring(header.indexOf(" ") + 1);
        String decoded = new String(Base64.decodeBase64(encoded.getBytes()));
        String userName = decoded.substring(0, decoded.indexOf(":"));
        String password = decoded.substring(decoded.indexOf(":") + 1);
        httpRequest.getSession().setAttribute(SecurityFilter.SESSIONPARAM_LOGIN_NAME, userName);
        _chain.doFilter(httpRequest, httpResponse);
      }
    } else  {
      throw new ServletException("request not allowed");
    }
  }
}


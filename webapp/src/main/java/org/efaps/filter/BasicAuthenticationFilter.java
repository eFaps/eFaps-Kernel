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

package org.efaps.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;

/**
 * The filter is used to make basic access authentication defined in RFC 2617.
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class BasicAuthenticationFilter implements Filter  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(SecurityFilter.class);

  /**
   * The string is name of the parameter used to define the application.
   *
   * @see #init 
   */
  final private static String INIT_PARAM_APPLICATION = "application";

  /**
   * The string is name of the parameter used to define the title.
   *
   * @see #init 
   */
  final private static String INIT_PARAM_TITLE = "title";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Login handler used to check the login in method {@link #checkLogin}.
   *
   * @see #init 
   * @see #checkLogin
   */
  private LoginHandler loginHandler = null;

  /**
   * Store the title which is shown in the realm dialog on the client side.
   *
   * @see #init
   * @see #doFilter
   */
  private String title = "eFaps";

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
      Called by the web container to indicate to a filter that it is being placed into service. The servlet container calls the init method exactly once after instantiating the filter. The init method must complete successfully before the filter is asked to do any filtering work.

      The web container cannot place the filter into service if the init method either
      1.Throws a ServletException
      2.Does not return within a time period defined by the web container

   * @param _filterConfig   filter configuration instance
   * @see #INIT_PARAM_TITLE
   * @see #title
   * @see #INIT_PARAM_APPLICATION
   * @see #loginhandler
   * @todo description
   */
  public void init(final FilterConfig _filterConfig) throws ServletException  {
    
    // sets the title
    String title = _filterConfig.getInitParameter(INIT_PARAM_TITLE);
    if (title != null)  {
      this.title = title;
    }
    
    // sets the login handler
    String applInit = _filterConfig.getInitParameter(INIT_PARAM_APPLICATION);
    this.loginHandler = new LoginHandler(applInit);
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
   * First the filtes tests, if the http(s) protokoll is used.
   * If the request is not implementing the {@link HttpServletRequest} and the
   * response is not implementing the {@link HttpServletResponse} interface,
   * a {@link ServletException} is thrown.<br/>
   * If the current user is already logged in, nothing is filtered. If the user
   * is not logged in and does not make authentication, the header for basic
   * authentication is sent to the client. After the client makes the 
   * authentication, the name and password is checked in {@link #checkLogin}.
   * If the authentication fails, the header for basic authentication is sent
   * again to the used, otherwise the nothing is filtered anymore.
   *
   * @throws ServletException if the request and response does not use the 
   *                          http(s) protokoll
   * @see HttpServletRequest
   * @see HttpServletResponse
   * @see #checkLogin
   */
  public void doFilter(final ServletRequest _request, 
                       final ServletResponse _response,
                       final FilterChain _chain) throws IOException, ServletException  {
    
    if ((_request instanceof HttpServletRequest) 
        && (_response instanceof HttpServletResponse))  {

      HttpServletRequest httpRequest = (HttpServletRequest) _request;
      HttpServletResponse httpResponse = (HttpServletResponse) _response;
      
      HttpSession session = httpRequest.getSession(true);

      String userName = (String) session.getAttribute(SecurityFilter.SESSIONPARAM_LOGIN_NAME);
      if (userName != null)  {
        _chain.doFilter(httpRequest, httpResponse);
      } else  {
        String header = httpRequest.getHeader("Authorization");

        if (header == null)  {
          httpResponse.setHeader("WWW-Authenticate", 
                                 "Basic realm=\"" + this.title + "\"");
          httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else  {
          String encoded = header.substring(header.indexOf(" ") + 1);
          String decoded = new String(Base64.decodeBase64(encoded.getBytes()));
          String name = decoded.substring(0, decoded.indexOf(":"));
          String passwd = decoded.substring(decoded.indexOf(":") + 1);
          if (checkLogin(name, passwd))  {
            session.setAttribute(SecurityFilter.SESSIONPARAM_LOGIN_NAME, name);
            _chain.doFilter(httpRequest, httpResponse);
          } else  {
            httpResponse.setHeader("WWW-Authenticate", 
                                   "Basic realm=\"" + this.title + "\"");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          }
        }
      }
    } else  {
      throw new ServletException("request not allowed");
    }
  }

  /**
   * Checks if the user with given name and password is allowed to login.
   *
   * @param _name   user name to check
   * @param _passwd password to check
   * @return <i>true</i> if the user is allowed to login (name and password is
   *         correct), otherweise <i>false</i>
   * @see #loginhandler
   * @see #doFilter
   */
  private boolean checkLogin(final String _name,
                             final String _passwd)  {
  
    boolean loginOk = false;

    Context context = null;
    try  {
      SecurityFilter.transactionManager.begin();

      context = Context.newThreadContext(SecurityFilter.transactionManager.getTransaction());
 
      boolean ok = false;

      try {
        if (this.loginHandler.checkLogin(_name, _passwd) != null)  {
          loginOk = true;
        }
        ok = true;
      } finally  {
  
        if (ok && context.allConnectionClosed()
            && (SecurityFilter.transactionManager.getStatus() == Status.STATUS_ACTIVE))  {
  
          SecurityFilter.transactionManager.commit();
        } else  {
          if (SecurityFilter.transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK)  {
            LOG.error("transaction is marked to roll back");
          } else if (!context.allConnectionClosed())  {
            LOG.error("not all connection to database are closed");
          } else  {
            LOG.error("transaction manager in undefined status");
          }
          SecurityFilter.transactionManager.rollback();
        }
      }
    } catch (EFapsException e)  {
      LOG.error("could not check name and password", e);
    } catch (NotSupportedException e)  {
      LOG.error("could not initialise the context", e);
    } catch (RollbackException e)  {
      LOG.error(e);
    } catch (HeuristicRollbackException e)  {
      LOG.error(e);
    } catch (HeuristicMixedException e)  {
      LOG.error(e);
    } catch (javax.transaction.SystemException e)  {
      LOG.error(e);
    } finally  {
      context.close();
    }
    
    return loginOk;
  }
}


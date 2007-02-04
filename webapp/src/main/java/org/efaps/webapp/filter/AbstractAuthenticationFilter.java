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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.NotSupportedException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.slide.transaction.SlideTransactionManager;

import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractAuthenticationFilter implements Filter  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * The static variable holds the transaction manager which is used within
   * the eFaps web application.
   */
  final public static TransactionManager transactionManager 
                                              = new SlideTransactionManager();

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG 
                      = LogFactory.getLog(AbstractAuthenticationFilter.class);

  /**
   * Name of the session variable for the login name.
   */
  final public static String SESSIONPARAM_LOGIN_NAME =   "login.name";

  /**
   * The string is name of the parameter used to define the application.
   *
   * @see #init 
   */
  final private static String INIT_PARAM_APPLICATION = "application";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Login handler used to check the login in method {@link #checkLogin}.
   *
   * @see #init 
   * @see #checkLogin
   */
  private LoginHandler loginHandler = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
      Called by the web container to indicate to a filter that it is being placed into service. The servlet container calls the init method exactly once after instantiating the filter. The init method must complete successfully before the filter is asked to do any filtering work.

      The web container cannot place the filter into service if the init method either
      1.Throws a ServletException
      2.Does not return within a time period defined by the web container
    // sets the login handler
   * @param _filterConfig   filter configuration instance
   * @see #INIT_PARAM_TITLE
   * @see #title
   * @see #INIT_PARAM_APPLICATION
   * @see #loginhandler
   * @todo description
   */
  public void init(final FilterConfig _filterConfig) throws ServletException  {
    String applInit = _filterConfig.getInitParameter(INIT_PARAM_APPLICATION);
    this.loginHandler = new LoginHandler(applInit);
  }

 /**
   * Destroys the filter. Is an empty method in this implementation.
Called by the web container to indicate to a filter that it is being taken out of service. This method is only called once all threads within the filter's doFilter method have exited or after a timeout period has passed. After the web container calls this method, it will not call the doFilter method again on this instance of the filter.

This method gives the filter an opportunity to clean up any resources that are being held (for example, memory, file handles, threads) and make sure that any persistent state is synchronized with the filter's current state in memory.
   */
  public void destroy()  {
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

      if (isLoggedIn(httpRequest))  {
        _chain.doFilter(httpRequest, httpResponse);
      } else  {
        doAuthenticate(httpRequest, httpResponse);
      }
    } else  {
      throw new ServletException("request not allowed");
    }
  }

  /**
   *
   */
  abstract protected void doAuthenticate(final HttpServletRequest _request,
                                         final HttpServletResponse _response,
                                         final FilterChain _chain)
                                         throws IOException, ServletException;

  /**
   * @param _userName   name of the logged in user (or null if current user is 
   *                                                not logged in
   * @param _request    servlet request
   * @param _response   servlet response
   * @param _chain      filter chain
   */
  protected void doFilter(
            final String _userName,
            final ServletRequest _request,
            final ServletResponse _response,
            final FilterChain _chain) throws IOException, ServletException  {
  
    Context context = null;
    try  {
      transactionManager.begin();
      Locale locale = null;
      Map < String, String[] > params = null;
      Map < String, FileItem > fileParams = null;
      if (_request instanceof HttpServletRequest)  {
        HttpServletRequest httpRequest = (HttpServletRequest) _request;
        locale = httpRequest.getLocale();
        if (ServletFileUpload.isMultipartContent(httpRequest))  {
          DiskFileUpload dfu = new DiskFileUpload();
// TODO: global setting!! + temp variable! 
// dfu.setRepositoryPath("s:\\temp");	also works without this *jul*
    
          List files = dfu.parseRequest(httpRequest);
          params = new HashMap < String, String[] > ();
          fileParams = new HashMap < String, FileItem > ();
          for (Object obj : files)  {
            FileItem file = (FileItem)obj;
            if (file.isFormField())  {
              params.put(file.getFieldName(), new String[]{file.getString()});
            } else  {
              fileParams.put(file.getFieldName(), file);
            }
          }
        } else  {
          params = httpRequest.getParameterMap();
        }
      } else  {
        params = _request.getParameterMap();
      }
      context = Context.newThreadContext(transactionManager.getTransaction(), 
                                         _userName, locale, 
                                         params, fileParams);
    } catch (FileUploadException e)  {
      LOG.error("could not initialise the context", e);
      throw new ServletException(e);
    } catch (EFapsException e)  {
      LOG.error("could not initialise the context", e);
      throw new ServletException(e);
    } catch (SystemException e)  {
      LOG.error("could not initialise the context", e);
      throw new ServletException(e);
    } catch (NotSupportedException e)  {
      LOG.error("could not initialise the context", e);
      throw new ServletException(e);
    }
  
    // TODO: is a open sql connection in the context returned automatically?
    try  {
      boolean ok = false;
      try {
        _chain.doFilter(_request, _response);
        ok = true;
      } finally  {
  
        if (ok && context.allConnectionClosed()
            && (transactionManager.getStatus() == Status.STATUS_ACTIVE))  {
  
          transactionManager.commit();
        } else  {
          if (transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK)  {
            LOG.error("transaction is marked to roll back");
  // TODO: throw of Exception is not a good idea... if an exception is thrown in the try code, this exception is overwritten!
  //          throw new ServletException("transaction in undefined status");
          } else if (!context.allConnectionClosed())  {
            LOG.error("not all connection to database are closed");
          } else  {
            LOG.error("transaction manager in undefined status");
          }
          transactionManager.rollback();
        }
      }
    } catch (RollbackException e)  {
      LOG.error(e);
      throw new ServletException(e);
    } catch (HeuristicRollbackException e)  {
      LOG.error(e);
      throw new ServletException(e);
    } catch (HeuristicMixedException e)  {
      LOG.error(e);
      throw new ServletException(e);
    } catch (javax.transaction.SystemException e)  {
      LOG.error(e);
      throw new ServletException(e);
    } finally  {
      context.close();
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
  protected boolean checkLogin(final String _name,
                             final String _passwd)  {
  
    boolean loginOk = false;

    Context context = null;
    try  {
      transactionManager.begin();

      context = Context.newThreadContext(transactionManager.getTransaction());
 
      boolean ok = false;

      try {
        if (this.loginHandler.checkLogin(_name, _passwd) != null)  {
          loginOk = true;
        }
        ok = true;
      } finally  {
  
        if (ok && context.allConnectionClosed()
            && (transactionManager.getStatus() == Status.STATUS_ACTIVE))  {
  
          transactionManager.commit();
        } else  {
          if (transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK)  {
            LOG.error("transaction is marked to roll back");
          } else if (!context.allConnectionClosed())  {
            LOG.error("not all connection to database are closed");
          } else  {
            LOG.error("transaction manager in undefined status");
          }
          transactionManager.rollback();
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

  /**
   * Check, if the session variable {@link #SESSIONPARAM_LOGIN_NAME} is set.
   * If not, user is not logged in. Normally then a redirect to login page
   * is made with method {@link #doRedirect2Login}.
   *
   * @param _request http servlet request variable
   * @return <i>true</i> if user logged in, otherwise <i>false</i>
   */
  protected boolean isLoggedIn(final HttpServletRequest _request)  {
    boolean ret = false;

    HttpSession session = _request.getSession(true);
    String userName = (String) session.getAttribute(SESSIONPARAM_LOGIN_NAME);
    if (userName != null)  {
      ret = true;
    }
    return ret;
  }
}

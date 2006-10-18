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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
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
import org.efaps.admin.user.Person;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class SecurityFilter implements Filter  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(SecurityFilter.class);

  /**
   * Name of the session variable for the login name.
   */
  final public static String SESSIONPARAM_LOGIN_NAME =   "login.name";

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
   *
   */
  final public static TransactionManager transactionManager = new SlideTransactionManager();

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
   * @todo remove hard coded proof of the MenuTree.jsp to set the forwarding url
   */
  public void doFilter(ServletRequest _request, ServletResponse _response,
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


  /**
   * @param _userName   name of the logged in user (or null if current user is 
   *                                                not logged in
   * @param _request    servlet request
   * @param _response   servlet response
   * @param _chain      filter chain
   */
  private void doFilter(
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
e.printStackTrace();
      LOG.error(e);
      throw new ServletException(e);
    } catch (HeuristicRollbackException e)  {
e.printStackTrace();
      LOG.error(e);
      throw new ServletException(e);
    } catch (HeuristicMixedException e)  {
e.printStackTrace();
      LOG.error(e);
      throw new ServletException(e);
    } catch (javax.transaction.SystemException e)  {
e.printStackTrace();
      LOG.error(e);
      throw new ServletException(e);
    } finally  {
      context.close();
    }
  }

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
}
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

package org.efaps.ui.wicket.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class TransactionFilter extends AbstractFilter  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(TransactionFilter.class);

  /**
   * Name of the session variable for the login forward (after the login is
   * done this is the next page).
   */
  final public static String SESSIONPARAM_LOGIN_FORWARD = "login.forward";

  /**
   * Name of the session variable for the login forward (after the login is
   * done this is the next page).
   */
  final private static String SESSION_CONTEXT_ATTRIBUTES = "contextAttributes";

  /**
   * The string is name of the parameter used to define the url login page.
   */
  final private static String INIT_PARAM_URL_LOGIN_PAGE = "urlLoginPage";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All uris which are not needed filtered by security check (password check)
   * are stored in this set variable.
   */
  private final Set<String> exludeUris = new HashSet<String>();

  /**
   * The string is URI to which a forward must be made if the user is not
   * logged in.
   */
  private String notLoggedInForward = null;

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
   * @param _request    servlet request
   * @param _response   servlet response
   * @param _chain      filter chain
   */
  protected void doFilter(
            final HttpServletRequest _request,
            final HttpServletResponse _response,
            final FilterChain _chain) throws IOException, ServletException  {
  
    Context context = null;
    try  {
      Locale locale = null;
      Map<String, String[]> params = null;
      Map<String, FileItem> fileParams = null;
      locale = _request.getLocale();
      if (ServletFileUpload.isMultipartContent(_request))  {
        DiskFileUpload dfu = new DiskFileUpload();
// TODO: global setting!! + temp variable! 
// dfu.setRepositoryPath("s:\\temp");	also works without this *jul*
    
        List files = dfu.parseRequest(_request);
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
        params = new HashMap < String, String[] > (_request.getParameterMap());
      }

      context = Context.begin(getLoggedInUser(_request),
                              locale,
                              getContextSessionAttributes(_request),
                              params,
                              fileParams);
    } catch (FileUploadException e)  {
      LOG.error("could not initialise the context", e);
      throw new ServletException(e);
    } catch (EFapsException e)  {
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
  
        if (ok && context.allConnectionClosed() && Context.isTMActive())  {
          Context.commit();
        } else  {
          if (Context.isTMMarkedRollback())  {
            LOG.error("transaction is marked to roll back");
  // TODO: throw of Exception is not a good idea... if an exception is thrown in the try code, this exception is overwritten!
  //          throw new ServletException("transaction in undefined status");
          } else if (!context.allConnectionClosed())  {
            LOG.error("not all connection to database are closed");
          } else  {
            LOG.error("transaction manager in undefined status");
          }
          Context.rollback();
        }
      }
    } catch (EFapsException e)  {
      LOG.error("", e);
      throw new ServletException(e);
    }
  }

  /**
   *
   *
   * @param _request  http servlet request
   * @return map of session attributes used for the context object
   */
  protected Map < String, Object > getContextSessionAttributes(final HttpServletRequest _request)  {
    Map<String, Object> map 
        = (Map<String, Object>) _request.getSession().getAttribute(SESSION_CONTEXT_ATTRIBUTES);
    if (map == null)  {
      map = new HashMap < String, Object > ();
      _request.getSession().setAttribute(SESSION_CONTEXT_ATTRIBUTES, map);
    }
    return map;
  }
}
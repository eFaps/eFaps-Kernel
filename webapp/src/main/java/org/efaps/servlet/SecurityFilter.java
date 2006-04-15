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
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.slide.transaction.SlideTransactionManager;

import org.efaps.db.Context;
import org.efaps.admin.user.Person;

/**
 *
 */
public class SecurityFilter implements Filter  {

  /**
   * Name of the session variable for the login name.
   */
  final public static String SESSIONPARAM_LOGIN_NAME =   "login.name";

  /**
   *
   */
  final private static TransactionManager transactionManager = new SlideTransactionManager();


  /**
   *
      Called by the web container to indicate to a filter that it is being placed into service. The servlet container calls the init method exactly once after instantiating the filter. The init method must complete successfully before the filter is asked to do any filtering work.

      The web container cannot place the filter into service if the init method either
      1.Throws a ServletException
      2.Does not return within a time period defined by the web container

   */
  public void init(FilterConfig _filterConfig) throws ServletException  {
System.out.println("------ filter init");
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


    if (httpRequest.getAuthType()!=null && httpRequest.getAuthType().equals(HttpServletRequest.BASIC_AUTH))  {
System.out.println("        Principal principal = "+httpRequest.getUserPrincipal());
String userName = httpRequest.getUserPrincipal().getName();
test(userName, httpRequest, _response, _chain);

    } else if (isLoggedIn(httpRequest) || uri.equals("/eFaps/login.jsp") || uri.equals("/eFaps/servlet/login"))  {

  String userName = (String)httpRequest.getSession().getAttribute(SESSIONPARAM_LOGIN_NAME);
test(userName, httpRequest, _response, _chain);


    } else  {
System.out.println("       NOT logged in");

      _request.getRequestDispatcher("/login.jsp").forward(_request, _response);

//((HttpServletResponse)_response).sendRedirect("/eFaps/login.jsp");
    }
  }

  public void test(final String _userName, final HttpServletRequest _httpRequest, ServletResponse _response, FilterChain _chain) throws IOException, ServletException  {

System.out.println("############################################################################### logged in start="+Thread.currentThread().getId());
Context context = null;
try  {
  transactionManager.begin();
  Person person = (_userName == null ? null : Person.get(_userName));
  context = new Context(transactionManager.getTransaction(), person, _httpRequest.getLocale());
  Context.setThreadContext(context);
} catch (SystemException e)  {
  throw new ServletException(e);
} catch (NotSupportedException e)  {
  throw new ServletException(e);
} catch (Exception e)  {
e.printStackTrace();
}
      try  {
boolean ok = false;
try {
        _chain.doFilter(_httpRequest, _response);
        ok = true;
} finally  {

        switch (transactionManager.getStatus())  {
        case Status.STATUS_ACTIVE:
          if (ok)  {
System.out.println("###############################################################################1 transaction commit");
            transactionManager.commit();
            break;
          }
          // if not ok, rollback!
        case Status.STATUS_MARKED_ROLLBACK:
          try {
System.out.println("###############################################################################2 transaction rollback");
            transactionManager.rollback();
          } catch (Throwable e)  {
          }

        default:
          try {
System.out.println("###############################################################################3 transaction rollback");
            transactionManager.rollback();
          } catch (Throwable e)  {
          }
          throw new ServletException("transaction in undefined status");
        }
}

      } catch (IOException e)  {
e.printStackTrace();
        throw e;
      } catch (ServletException e)  {
e.printStackTrace();
        throw e;
      } catch (Throwable e)  {
e.printStackTrace();
        throw new ServletException(e);
      } finally  {
        context.close();
      }

System.out.println("############################################################################### logged in end="+Thread.currentThread().getId());
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
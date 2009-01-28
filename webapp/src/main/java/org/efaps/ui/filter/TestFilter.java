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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.efaps.admin.program.esjp.EFapsClassLoader;

/**
 * @author tim
 * @version $Id$
 */
public class TestFilter implements Filter {

  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    // TODO Auto-generated method stub
System.out.println("---> test filter destroy");
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(final ServletRequest _servletrequest,
                       final ServletResponse _servletresponse,
                       final FilterChain _filterchain)
      throws IOException, ServletException {
    // TODO Auto-generated method stub
System.out.println("---> test filter doFilter");
try {
  Servlet servlet = null;
  final Object obj = Class.forName("org.tmo.test.TestServlet",
      true,
      new EFapsClassLoader(this.getClass().getClassLoader()))
  .newInstance();
  if (obj instanceof Servlet)  {
    servlet = (Servlet) obj;
    servlet.service(_servletrequest, _servletresponse);
    } else  {
    System.out.println("class does not implement interface "
    + Servlet.class);
    }

} catch (InstantiationException e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
} catch (IllegalAccessException e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
} catch (ClassNotFoundException e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
}

  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(final FilterConfig _filterconfig) throws ServletException {
    // TODO Auto-generated method stub
System.out.println("---> test filter init="+_filterconfig);
  }

}

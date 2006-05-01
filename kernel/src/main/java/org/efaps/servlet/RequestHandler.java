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

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.efaps.admin.user.Person;
import org.efaps.db.Cache;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.util.EFapsException;

/**
 *
 */
public class RequestHandler extends AbstractServlet  {

  /**
   * Parameter name for absolut url of login without protocol and host name.
   */
  final private static String INITPARAM_URL_LOGIN_PAGE =      "urlLoginPage";

  /**
   * Parameter name for the logging properties.
   */
  final private static String INITPARAM_LOGGING = "logging";

  /**
   *
   */
  final private static String SESSIONPARAM_LOGIN_TARGET = "login.target";

  /**
   *
   */
  final private static String SESSIONPARAM_ERROR =        "error";

  /**
   *
   */
  final private static String POSTPARAM_USERNAME =        "name";

  /**
   *
   */
  final private static String POSTPARAM_PASSWORD =        "password";

  /**
   * The static variable holds the resource name for the JDBC database connection.
   */
  final private static String RESOURCE_JDBC = "eFaps/jdbc";

  /**
   * The static variable holds the resource name for the database type.
   */
  final private static String RESOURCE_DBTYPE = "eFaps/dbType";



final private static String URL_LOGOUT =  "/Logout";
final private static String URL_LOGIN =  "/Login";

  /////////////////////////////////////////////////////////////////////////////


  /**
   * @param _config
   */
  public void init(ServletConfig _config) throws ServletException  {
    super.init(_config);



    initReplacableMacros("/"+_config.getServletContext().getServletContextName()+"/");

this.pathLoginPage = replaceMacrosInUrl(_config.getInitParameter(INITPARAM_URL_LOGIN_PAGE));


try {

InitialContext initCtx = new InitialContext();
javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");

AbstractDatabase dbType = (AbstractDatabase) envCtx.lookup(RESOURCE_DBTYPE);
if (dbType == null)  {
  throw new ServletException("no database type defined!");
}
Context.setDbType(dbType);

DataSource ds = (DataSource) envCtx.lookup(RESOURCE_JDBC);
if (ds == null)  {
  throw new ServletException("no SQL data source defined!");
}
Context.setDataSource(ds);


javax.transaction.TransactionManager tm = new org.apache.slide.transaction.SlideTransactionManager();
tm.begin();
Context context = new Context(tm.getTransaction(), null, null);
Context.setThreadContext(context);
try {
Cache.reloadCache(context);
} catch (Throwable e)  {
  e.printStackTrace();
}
tm.rollback();

} catch (Exception e)  {
e.printStackTrace();
}


  }

  public void doGet(HttpServletRequest _req, HttpServletResponse _res) throws ServletException, IOException  {
System.out.println("--->getServletContextName()="+_req.getSession().getServletContext().getServletContextName());
System.out.println("--->getServletPath()="+_req.getServletPath());
System.out.println("--->getPathInfo()="+_req.getPathInfo());

    if (_req.getPathInfo()!=null && _req.getPathInfo().equals(URL_LOGOUT))  {
      doLogout(_req, _res);
//    } else if (_req.getPathInfo()!=null && _req.getPathInfo().equals(URL_LOGIN))  {
//      doLogin(_req, _res);
    } else if (isLoggedIn(_req))  {
      _res.setContentType("text/html");
      _res.sendRedirect(replaceMacrosInUrl("${COMMONURL}/Main.jsf"));
    } else  if (_req.getPathInfo()==null || _req.getPathInfo().equals("/"))  {
      doSendLoginFrame(_req, _res);
    } else  {
      doRedirect2Login(_req, _res);
    }
  }

  /**
   * Write survey results to output file in response to the POSTed
   * form.  Write a "thank you" to the client.
   */
  public void doPost(HttpServletRequest _req, HttpServletResponse _res) throws ServletException, IOException  {

//    if (_req.getPathInfo().equals(URL_LOGIN))  {
//      doLogin(_req, _res);
//    } else  {
//      if (!isLoggedIn(_req))  {
//        doRedirect2Login(_req, _res);
//      }
//    }
  }

  /**
   *
   * @param _req request variable
   * @param _res response variable
   */
  protected void doSendLoginFrame(HttpServletRequest _req, HttpServletResponse _res) throws IOException  {
    PrintWriter pW = null;
    _res.setContentType("text/html");

    try  {

      pW = _res.getWriter();
      pW.print(
        "<html>"+
          "<head>"+
            "<title>eFaps</title>"+
          "</head>"+
          "<script type=\"text/javascript\">"+
            "function test4top() {"+
              "if(top!=self)  {"+
                "top.location=self.location;"+
              "}"+
            "}"+
          "</script>"+
          "<frameset onload=\"test4top()\">"+
            "<frame src=\""+getPathLoginPage()+"\" name=\"Login\">"+
          "</frameset>"+
        "</html>"
      );
    } catch(IOException e)  {
      throw e;
    } catch (Exception e)  {
e.printStackTrace();
    } finally  {
      pW.close();
    }
  }

  /**
   *
   * @param _req request variable
   * @param _res response variable
   */
  protected void doSendLoginFrameNotCorrect(HttpServletRequest _req, HttpServletResponse _res) throws IOException  {
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
              "function alertWrongLogin() {"+
                "alert('User name and / or password is not correct. Please try again.');"+
              "}"+
            "</script>"+
            "<frameset onload=\"alertWrongLogin()\">"+
            "<frame src=\""+getPathLoginPage()+"\" name=\"Login\">"+
          "</frameset>"+
        "</html>"
      );
    } catch(IOException e)  {
      throw e;
    } catch (Exception e)  {
e.printStackTrace();
    } finally  {
      pW.close();
    }
  }

  /**
   * User wants to login into eFaps. The user name and password is checked.
   * User name is stored in session variable {@link AbstractServlet#SESSIONPARAM_LOGIN_NAME}.
   * After login a redirect to the "common/Main.jsp" is made.<br/>
   * The post parameter names are {@link #POSTPARAM_USERNAME} and
   * {@link #POSTPARAM_PASSWORD}.
   *
   * @param _req request variable
   * @param _res response variable
   * @see #checkLogin
   */
/*  protected void doLogin(HttpServletRequest _req, HttpServletResponse _res) throws IOException  {
    PrintWriter out = _res.getWriter();

    String name = _req.getParameter(POSTPARAM_USERNAME);
    String passwd = _req.getParameter(POSTPARAM_PASSWORD);

    if (checkLogin(name, passwd))  {
      HttpSession session = _req.getSession(true);
      session.setAttribute(SESSIONPARAM_LOGIN_NAME, name);  // just a marker object

      _res.setContentType("text/html");
      _res.sendRedirect(replaceMacrosInUrl("${COMMONURL}/Main.jsp"));
    } else  {
      doSendLoginFrameNotCorrect(_req, _res);
    }
  }
*/

  /**
   *
   */
  protected void doLogout(HttpServletRequest _req, HttpServletResponse _res) throws IOException  {
    HttpSession session = _req.getSession(true);
    session.removeAttribute(SESSIONPARAM_LOGIN_NAME);
    _res.setContentType("text/html");
    _res.sendRedirect(replaceMacrosInUrl("${ROOTURL}"));
  }

  /**
   * The instance method checks if for the given user the password is correct
   * and the person is active (status equals 10001).
   *
   * @param _name   name of the person name to check
   * @param _passwd password of the person to check
   * @see #checkLogin
   */
/*  protected boolean checkLogin(String _name, String _passwd)  {
    boolean ret = false;
    Context context = null;
    try  {
      context = new Context();
      if (_name!=null)  {
        Person person = Person.get(_name);
        ret = person.checkPassword(context, _passwd);
      }
    } catch (Throwable e)  {
e.printStackTrace();
    } finally  {
      context.close();
    }
    return ret;
  }
*/

  /**
   * Redirect to the login page. To go back to calling page, the request
   * URL variable is stored in the session variable
   * {@link #SESSIONPARAM_LOGIN_TARGET}.
   *
   * @param _req request variable
   * @param _res response variable
   */
  protected void doRedirect2Login(HttpServletRequest _req, HttpServletResponse _res) throws IOException  {
    HttpSession session = _req.getSession(true);

    session.setAttribute(SESSIONPARAM_LOGIN_TARGET, _req.getPathInfo());
    _res.setContentType("text/html");
    _res.sendRedirect(replaceMacrosInUrl("${ROOTURL}"));
  }


  /////////////////////////////////////////////////////////////////////////////

  private String pathLoginPage = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #pathLoginPage}.
   *
   * @return returns value of instance variable {@link #pathLoginPage}.
   * @see #pathLoginPage
   */
  private String getPathLoginPage()  {
    return this.pathLoginPage;
  }

  /////////////////////////////////////////////////////////////////////////////
  // url macro handler

  /**
   * The static method replaces all known url macros by real urls.
   *
   * @param _url  url with url macros
   * @return url string with replaces url macros
   * @see #replacableMacros
   * @see #initReplacableMacros
   */
  public static String replaceMacrosInUrl(String _url)  {
    String url = _url;
    if (getReplacableMacros()!=null)  {
      for (Map.Entry<String,String> entry : getReplacableMacros().entrySet())  {
        url = url.replaceAll(entry.getKey(), entry.getValue());
      }
    }
    if (url.indexOf('?')<0)  {
      url += "?";
    }
    return url;
  }

  /**
   * Stores the key and the value of the replaceable url macros in the map
   * {@link #replacableMacros} used by method {@link #replaceMacrosInUrl} to
   * create executeable urls.
   *
   * @param _rootUrl  root url of the application used to replace
   * @see #replaceMacrosInUrl
   * @see #replacableMacros
   */
  private static void initReplacableMacros(String _rootUrl)  {
    setReplacableMacros(new HashMap<String,String>());
    replacableMacros.put("\\$\\{SERVLETURL\\}", _rootUrl+"request");
    replacableMacros.put("\\$\\{COMMONURL\\}",  _rootUrl+"common");
    replacableMacros.put("\\$\\{ROOTURL\\}",    _rootUrl);
    replacableMacros.put("\\$\\{ICONURL\\}",    _rootUrl+"images");
  }

  /**
   * The static map stores all replacable url macros.
   *
   * @see #getReplacableMacros
   * @see #setReplacableMacros
   */
  private static Map<String,String> replacableMacros = null;

  /**
   * This is the getter method for static variable {@link #replacableMacros}.
   *
   * @return returns value of static variable {@link #replacableMacros}.
   * @see #replacableMacros
   * @see #setReplacableMacros
   */
  private static Map<String,String> getReplacableMacros()  {
    return replacableMacros;
  }

  /**
   * This is the setter method for static variable {@link #replacableMacros}.
   *
   * @param _replacableMacros   new value for static variable
   *                            {@link #replacableMacros}
   * @see #replacableMacros
   * @see #getReplacableMacros
   */
  private static void setReplacableMacros(Map<String,String> _replacableMacros)  {
    replacableMacros = _replacableMacros;
  }

}

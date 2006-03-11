/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import org.efaps.admin.user.Person;
import org.efaps.db.Context;

/**
 * The servlet logs in a user with name and password.
 */
public class LoginServlet extends AbstractServlet  {

  /**
   * name of the name parameter
   */
  final private static String PARAM_USERNAME =        "name";

  /**
   * name of the password parameter
   */
  final private static String PARAM_PASSWORD =        "password";

  /**
   * User wants to login into eFaps. The user name and password is checked.
   * User name is stored in session variable {@link AbstractServlet#SESSIONPARAM_LOGIN_NAME}.
   * After login a redirect to the "common/Main.jsp" is made.<br/>
   * The post parameter names are {@link #PARAM_USERNAME} and
   * {@link #PARAM_PASSWORD}.
   *
   * @param _req request variable
   * @param _res response variable
   * @see #checkLogin
   */
  protected void doGet(HttpServletRequest _req, HttpServletResponse _res) throws ServletException, IOException  {
    PrintWriter out = _res.getWriter();

    String name = _req.getParameter(PARAM_USERNAME);
    String passwd = _req.getParameter(PARAM_PASSWORD);

    if (checkLogin(name, passwd))  {
      HttpSession session = _req.getSession(true);
      session.setAttribute(SESSIONPARAM_LOGIN_NAME, name);  // just a marker object

      _res.setContentType("text/html");
      _res.sendRedirect(RequestHandler.replaceMacrosInUrl("${COMMONURL}/Main.jsp"));
    } else  {
//_res.sendRedirect(RequestHandler.replaceMacrosInUrl("${ROOTURL}/"));
      doSendLoginFrameNotCorrect(_req, _res);
    }
  }

  /**
   *
   * @param _req request variable
   * @param _res response variable
   */
  protected void doSendLoginFrameNotCorrect(HttpServletRequest _req, HttpServletResponse _res) throws ServletException, IOException  {
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
              "function wrongLogin() {"+
              "}"+
            "</script>"+
            "<frameset>"+
            "<frame src=\""+_req.getContextPath()+"\" name=\"Login\">"+
          "</frameset>"+
        "</html>"
      );
    } catch(IOException e)  {
      throw e;
    } catch (Exception e)  {
e.printStackTrace();
throw new ServletException(e);
    } finally  {
      pW.close();
    }
  }

  /**
   * The instance method checks if for the given user the password is correct
   * and the person is active (status equals 10001).
   *
   * @param _name   name of the person name to check
   * @param _passwd password of the person to check
   * @see #checkLogin
   */
  protected boolean checkLogin(String _name, String _passwd)  {
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
}
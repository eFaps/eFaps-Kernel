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
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The servlet logs in a user with name and password. The name and password is
 * checked with the help of Java Authentication and Authorization Service
 * (short JAAS).
 *
 * @author tmo
 * @version $Id$
 */
public class LoginServlet extends HttpServlet  {

  /**
   * Logging instance used to give logging information of this class.
   */
  private static Log LOG = LogFactory.getLog(LoginServlet.class);

  /**
   * Name of the servlet initialise parameter for the application.
   */
  final private static String INIT_PARAM_APPLICATION = "application";

  /**
   * Name of the servlet initialise parameter for the application.
   */
  final private static String INIT_PARAM_FORWARD_URL = "forwardURL";

  /**
   * name of the name parameter
   */
  final private static String PARAM_USERNAME =        "name";

  /**
   * name of the password parameter
   */
  final private static String PARAM_PASSWORD =        "password";

  /**
   * The name of the application used to create a new login context.
   *
   * @see #checkLogin
   * @see #init
   */
  private String application = "eFaps";

  /**
   * The forward URL used if the login is correct and the next page must be
   * shown.
   *
   * @see #init
   * @see #doGet
   */
  private String forwardURL = "${COMMONURL}/Main.jsf";

  /**
   * The login servlet is initialised. The application name in
   * {@link #application} and the forward URL after login in
   * {@link #forwardURL} is set.
   *
   * @param _config
   * @see #INIT_PARAM_APPLICATION
   * @see #application
   * @see #INIT_PARAM_FORWARD_URL
   * @see #forwardURL
   */
  public void init(final ServletConfig _config) throws ServletException  {
    super.init(_config);

    String applInit = _config.getInitParameter(INIT_PARAM_APPLICATION);
    if (applInit != null)  {
      this.application = applInit;
    }

    String forwInit = _config.getInitParameter(INIT_PARAM_FORWARD_URL);
    if (forwInit != null)  {
      this.forwardURL = forwInit;
    }
  }

  /**
   * User wants to login into eFaps. The user name and password is checked.
   * User name is stored in session variable {@link SecurityFilter#SESSIONPARAM_LOGIN_NAME}.
   * After login a redirect to the "common/Main.jsf" is made.<br/>
   * The post parameter names are {@link #PARAM_USERNAME} and
   * {@link #PARAM_PASSWORD}.
   *
   * @param _req request variable
   * @param _res response variable
   * @see #checkLogin
   */
  protected void doGet(final HttpServletRequest _req, final HttpServletResponse _res) throws ServletException, IOException  {
    PrintWriter out = _res.getWriter();

    String name = _req.getParameter(PARAM_USERNAME);
    String passwd = _req.getParameter(PARAM_PASSWORD);

    if (checkLogin(name, passwd))  {
      HttpSession session = _req.getSession(true);
      session.setAttribute(SecurityFilter.SESSIONPARAM_LOGIN_NAME, name);

      _res.setContentType("text/html");

      String newUrl = (String) _req.getSession().getAttribute(SecurityFilter.SESSIONPARAM_LOGIN_FORWARD);
      if (newUrl == null)  {
        newUrl = RequestHandler.replaceMacrosInUrl(this.forwardURL);
      } else  {
        _req.getSession().removeAttribute(SecurityFilter.SESSIONPARAM_LOGIN_FORWARD);
      }
      _res.sendRedirect(newUrl);
    } else  {
      doSendLoginFrameNotCorrect(_req, _res);
    }
  }

  /**
   * This page is sent if the login is not correct.
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
      LOG.error("Could not write the frame for not correct login.", e);
      throw new ServletException(e);
    } finally  {
      pW.close();
    }
  }

  /**
   * The instance method checks if for the given user the password is correct.
   * The test itself is done with
   *
   * @param _name   name of the person name to check
   * @param _passwd password of the person to check
   * @see #checkLogin
   */
  protected boolean checkLogin(final String _name, final String _passwd)  {
    boolean ret = false;
    try  {
      LoginContext login = new LoginContext(this.application,
              new LoginCallBackHandler(_name, _passwd));
      login.login();

      Person person = null;
      for (JAASSystem system : JAASSystem.getAllJAASSystems())  {
        Set users = login.getSubject().getPrincipals(system.getPersonJAASPrincipleClass());
System.out.println("---------------------->users="+users);
        for (Object persObj : users)  {
          try  {
            String persKey = (String) system.getPersonMethodKey().invoke(
                                                              persObj, null);

            Person foundPerson = Person.getWithJAASKey(system, persKey);
            if (foundPerson == null)  {
// TODO: JAASKey for person must be added!!!
            } else if (person == null)  {
              person = foundPerson;
            } else if (person.getId() != foundPerson.getId()) {
              LOG.error("For JAAS system " + system.getName() + " "
                          + "person with key '" + persKey + "' is not unique!"
                          + "Have found person '" + person.getName() + "' "
                          + "(id = " + person.getId() + ") and person "
                          + "'" + foundPerson.getName() + "' "
                          + "(id = " + foundPerson.getId() + ").");
// TODO: throw exception!!
            }
          } catch (IllegalAccessException e)  {
            LOG.error("could not execute person key method for system "
                                                  + system.getName(), e);
// TODO: throw exception!!
          } catch (IllegalArgumentException e)  {
            LOG.error("could not execute person key method for system "
                                                  + system.getName(), e);
// TODO: throw exception!!
          } catch (InvocationTargetException e)  {
            LOG.error("could not execute person key method for system "
                                                  + system.getName(), e);
// TODO: throw exception!!
          }
        }
      }

if (person == null)  {
  for (JAASSystem system : JAASSystem.getAllJAASSystems())  {
    Set users = login.getSubject().getPrincipals(system.getPersonJAASPrincipleClass());
    for (Object persObj : users)  {
      try  {
        String persKey = (String) system.getPersonMethodKey().invoke(
                                                          persObj, null);

        if (person == null)  {
          person = Person.createPerson(system, persKey, persKey);
        } else  {
          person.assignToJAASSystem(system, persKey);
        }

      } catch (IllegalAccessException e)  {
        LOG.error("could not execute person key method for system "
                                              + system.getName(), e);
// TODO: throw exception!!
      } catch (IllegalArgumentException e)  {
        LOG.error("could not execute person key method for system "
                                              + system.getName(), e);
// TODO: throw exception!!
      } catch (InvocationTargetException e)  {
        LOG.error("could not execute person key method for system "
                                              + system.getName(), e);
// TODO: throw exception!!
      }
    }
  }
}

      person.cleanUp();

      for (JAASSystem system : JAASSystem.getAllJAASSystems())  {
        if (system.getRoleJAASPrincipleClass() != null)  {
          Set rolesJaas = login.getSubject().getPrincipals(
                                        system.getRoleJAASPrincipleClass());
          Set < Role > rolesEfaps = new HashSet < Role > ();
          for (Object roleObj : rolesJaas)  {
            try  {
              String roleKey = (String) system.getRoleMethodKey().invoke(
                                                                roleObj, null);
              Role roleEfaps = Role.getWithJAASKey(system, roleKey);
              if (roleEfaps != null)  {
                rolesEfaps.add(roleEfaps);
              }
            } catch (IllegalAccessException e)  {
              LOG.error("could not execute role key method for system "
                                                    + system.getName(), e);
            } catch (IllegalArgumentException e)  {
              LOG.error("could not execute role key method for system "
                                                    + system.getName(), e);
            } catch (InvocationTargetException e)  {
              LOG.error("could not execute role key method for system "
                                                    + system.getName(), e);
            }
          }
          person.setRoles(system, rolesEfaps);
        }
      }

      ret = true;
    } catch (EFapsException e)  {
e.printStackTrace();
      LOG.error("login failed for '" + _name + "'", e);
    } catch (LoginException e)  {
e.printStackTrace();
      LOG.error("login failed for '" + _name + "'", e);
    }
    return ret;
  }


  private class LoginCallBackHandler implements CallbackHandler  {

    /**
     * The user name to test is stored in this instance variable.
     */
    private final String name;

    /**
     * The password used from the user is stored in this instance variable.
     */
    private final String password;

    /**
     * Constructor initialising the name and password in this callback
     * handler.
     *
     * @see #name
     * @see #password
     */
    private LoginCallBackHandler(final String _name, final String _passwd)  {
      this.name = _name;
      this.password = _passwd;
    }

    /**
     * The handler sets for instances of {@link NameCallBack} the given
     * {@link #name} and for instances of {@link PasswordCallBack} the given
     * {@link #password}. {@link TextOutputCallBack} instances are ignored.
     *
     * @param _callbacks callback instances to handle
     * @throws UnsupportedCallbackException for all {@link Callback}
     *         instances which are not {@link NameCallBack},
     *         {@link PasswordCallBack} or {@link TextOutputCallBack}.
     */
    public void handle(final Callback[] _callbacks)
      throws IOException, UnsupportedCallbackException {

      for (int i = 0; i < _callbacks.length; i++) {
        if (_callbacks[i] instanceof TextOutputCallback) {
          // do nothing, TextOutputCallBack's are ignored!
        } else if (_callbacks[i] instanceof NameCallback) {
          NameCallback nc = (NameCallback) _callbacks[i];
          nc.setName(this.name);
        } else if (_callbacks[i] instanceof PasswordCallback) {
          PasswordCallback pc = (PasswordCallback)_callbacks[i];
          pc.setPassword(this.password.toCharArray());
        } else {
          throw new UnsupportedCallbackException
              (_callbacks[i], "Unrecognized Callback");
        }
      }
    }
  }

  private class JAASConfiguration  {

    /**
     *
     */
    private String name = null;

    /**
     *
     */
    private String userClass = null;

    /**
     *
     */
    private String roleClass = null;

    /**
     *
     */
    private String nameMethod = null;

    public void setName(final String _name)  {
      this.name = _name;
    }

    public void setUserClass(final String _userClass)  {
      this.userClass = _userClass;
    }

    public void setRoleClass(final String _roleClass)  {
      this.roleClass = _roleClass;
    }

    public void setNameMethod(final String _nameMethod)  {
      this.nameMethod = _nameMethod;
    }
  }
}
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

package org.efaps.jaas.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import org.efaps.jaas.ActionCallback;

/***
 *
 * @author tmo
 * @version $Id$
 */
public class XMLUserLoginModule implements LoginModule  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(XMLUserLoginModule.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Stores the mode in which mode the login module is called.
   */
  private ActionCallback.Mode mode = ActionCallback.Mode.Undefined;

  /**
   * The subject is stored from the initialize method to store all single
   * entities for the person logging in.
   */
  private Subject subject = null;

  /**
   * Has our own <code>commit()</code> returned successfully?
   */
  private boolean committed = false;

  /**
   *
   */
  private CallbackHandler callbackHandler = null;

  /**
   * The logged in person principal is stored in this variable. The variable is
   * set if the user logged in and reset to null if the user is logged out.
   */
  private XMLPersonPrincipal person = null;

  /**
   *
   */
  private final Map < String, XMLPersonPrincipal > allPersons
                              = new HashMap < String, XMLPersonPrincipal > ();

  /////////////////////////////////////////////////////////////////////////////
  // methods

// TODO: description
  /**
   * @param _subject
   * @param _callbackHandler
   * @param _sharedState
   * @param _options
   * @see #readPersons
   */
  public final void initialize(final Subject _subject,
                               final CallbackHandler _callbackHandler,
                               final Map < String, ? > _sharedState,
                               final Map < String, ? > _options)  {

    LOG.debug("Init");
    this.subject = _subject;
    this.callbackHandler = _callbackHandler;
    readPersons((String)_options.get("xmlFileName"));
  }

// TODO: description
  /**
   * @return <i>true</i> if login is allowed and user name with password is
   *         correct
   * @throws FailedLoginException if login is not allowed with given user name
   *         and password (if user does not exists or password is not correct)
   * @throws LoginException if an error occurs while calling the callback
   *         handler or the {@link #checkLogin} method
   * @see #checkLogin
   * @return <i>true</i> if this login module is used to authentificate and the
   *         user could be authentificated with this login module, otherwise
   *         <i>false</i>
   * @throws FailedLoginException if the user is found, but the password does
   *         not match
   * @throws LoginException if user or password could not be get from the
   *         callback handler
   */
  public final boolean login() throws LoginException  {
    boolean ret = false;

    Callback[] callbacks = new Callback[3];
    callbacks[0] = new ActionCallback();
    callbacks[1] = new NameCallback("Username: ");
    callbacks[2] = new PasswordCallback("Password: ", false);
    // Interact with the user to retrieve the username and password
    String userName = null;
    String password = null;
    try  {
      this.callbackHandler.handle(callbacks);
      this.mode = ((ActionCallback) callbacks[0]).getMode();
      userName = ((NameCallback) callbacks[1]).getName();
      if (((PasswordCallback) callbacks[2]).getPassword() != null)  {
        password = new String(((PasswordCallback) callbacks[2]).getPassword());
      }
    } catch (IOException e)  {
      throw new LoginException(e.toString());
    } catch (UnsupportedCallbackException e)  {
      throw new LoginException(e.toString());
    }

    if (this.mode == ActionCallback.Mode.AllPersons)  {
      ret = true;
    } else if (this.mode == ActionCallback.Mode.PersonInformation)  {
      this.person = this.allPersons.get(userName);
      if (this.person != null)  {
        if (LOG.isDebugEnabled())  {
          LOG.debug("found '" + this.person + "'");
        }
        ret = true;
      }
    } else  {
      this.person = this.allPersons.get(userName);
      if (this.person != null)  {
        if ((password == null)
            || ((password != null)
                && !password.equals(this.person.getPassword())))  {
  
          LOG.error("person '" + this.person + "' tried to log in with wrong password");
          this.person = null;
          throw new FailedLoginException("Username or password is incorrect");
        }
        if (LOG.isDebugEnabled())  {
          LOG.debug("log in of '" + this.person + "'");
        }
        this.mode = ActionCallback.Mode.Login;
        ret = true;
      }
    }

    return ret;
  }

  /**
   * Adds the principal person and all related roles and groups to the
   * {@link #subject} if the {@link #person} was found and the user could log
   * in with this login module.
   *
   * @return <i>true</i> if this login module is used to authentificate the
   *         current user (checked with {@link #person} is not null),
   *         otherwise <i>false</i>
   */
  public final boolean commit() throws LoginException  {
    boolean ret = false;

    if (this.mode == ActionCallback.Mode.AllPersons)  {
      for (XMLPersonPrincipal person : this.allPersons.values())  {
        if (!this.subject.getPrincipals().contains(person))  {
          if (LOG.isDebugEnabled())  {
            LOG.debug("commit person '" + person + "'");
          }
          this.subject.getPrincipals().add(person);
        }
      }
      this.committed = true;
      ret = true;
    } else if (this.person != null)  {
      if (LOG.isDebugEnabled())  {
        LOG.debug("commit of '" + this.person + "'");
      }
      if (!this.subject.getPrincipals().contains(this.person))  {
        this.subject.getPrincipals().add(this.person);
        for (XMLRolePrincipal principal : this.person.getRoles())  {
          this.subject.getPrincipals().add(principal);
        }
        for (XMLGroupPrincipal principal : this.person.getGroups())  {
          this.subject.getPrincipals().add(principal);
        }
      }
      this.committed = true;
      ret = true;
    }

    return ret;
  }

// TODO: description
  /**
   * An abort for this login module works like the {@link #logout} method.
   *
   * @see #logout
   */
  public final boolean abort()  {
    boolean ret = false;

    if (this.person != null)  {
      if (LOG.isDebugEnabled())  {
        LOG.debug("abort of " + this.person);
      }
      this.subject.getPrincipals().remove(this.person);
      for (XMLRolePrincipal principal : this.person.getRoles())  {
        this.subject.getPrincipals().remove(principal);
      }
      for (XMLGroupPrincipal principal : this.person.getGroups())  {
        this.subject.getPrincipals().remove(principal);
      }
      this.person = null;
      this.committed = false;
      ret = true;
    }
    return ret;
  }

  /**
   * The method logs out a Subject. All principals (the person and the related
   * roles and groups) are removed from the subject in {@link #subject}.
   *
   * @return <i>true</i> if this login module is used to authentificate the
   *         current user, otherwise <i>false</i>
   */
  public final boolean logout()  {
    boolean ret = false;

    if (this.person != null)  {
      if (LOG.isDebugEnabled())  {
        LOG.debug("logout of " + this.person);
      }
      this.subject.getPrincipals().remove(this.person);
      for (XMLRolePrincipal principal : this.person.getRoles())  {
        this.subject.getPrincipals().remove(principal);
      }
      for (XMLGroupPrincipal principal : this.person.getGroups())  {
        this.subject.getPrincipals().remove(principal);
      }
      this.person = null;
      this.committed = false;
      ret = true;
    }
    return ret;
  }

  /**
   * The name of the xml is store in this instance variable. The xlm file holds
   * all allowed persons and their related roles and groups.
   *
   * @param _fileName name of the XML file with the user data
   */
  private void readPersons(final String _fileName)  {
    try  {
      File _file = new File(_fileName);

      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("persons", ArrayList.class);
      digester.addObjectCreate("persons/person", XMLPersonPrincipal.class);
      digester.addSetNext("persons/person", "add");

      digester.addCallMethod("persons/person/name", "setName", 1);
      digester.addCallParam("persons/person/name", 0);

      digester.addCallMethod("persons/person/password", "setPassword", 1);
      digester.addCallParam("persons/person/password", 0);

      digester.addCallMethod("persons/person/firstName", "setFirstName", 1);
      digester.addCallParam("persons/person/firstName", 0);

      digester.addCallMethod("persons/person/lastName", "setLastName", 1);
      digester.addCallParam("persons/person/lastName", 0);

      digester.addCallMethod("persons/person/email", "setEmail", 1);
      digester.addCallParam("persons/person/email", 0);

      digester.addCallMethod("persons/person/role", "addRole", 1);
      digester.addCallParam("persons/person/role", 0);

      digester.addCallMethod("persons/person/group", "addGroup", 1);
      digester.addCallParam("persons/person/group", 0);

      List < XMLPersonPrincipal > personList
              = (List < XMLPersonPrincipal > ) digester.parse(_file);
      for (XMLPersonPrincipal person : personList)  {
        this.allPersons.put(person.getName(), person);
      }
    } catch (IOException e)  {
      LOG.error("could not open file '" + _fileName + "'", e);
    } catch (SAXException e)  {
      LOG.error("could not read file '" + _fileName + "'", e);
    }
  }
}
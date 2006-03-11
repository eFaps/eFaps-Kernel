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

package org.efaps.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.user.Person;
import org.efaps.db.Context;

/**
 *
 */
public class UserLoginModule implements LoginModule  {

  /**
   * Logging instance used to give logging information of this class.
   */
  private static Log log = LogFactory.getLog(UserLoginModule.class);

  /**
   * Has our own <code>commit()</code> returned successfully?
   */
  private boolean committed = false;

  // initial state
  private Subject             subject;
  private CallbackHandler     callbackHandler;
  private Map<String,?>       sharedState;
  private Map<String,?>       options;

  private Principal principal = null;

  /**
   * @param _subject
   * @param _callbackHandler
   * @param _sharedState
   * @param _options
   */
  public final void initialize(final Subject _subject,
      final CallbackHandler _callbackHandler, final Map<String,?> _sharedState,
      final Map<String,?> _options)  {

    log.debug("Init");
    this.subject          = _subject;
    this.callbackHandler  = _callbackHandler;
    this.sharedState      = _sharedState;
    this.options          = _options;
  }

  /**
   * @return <i>true</i> if login is allowed and user name with password is
   *         correct
   * @throws FailedLoginException if login is not allowed with given user name
   *         and password (if user does not exists or password is not correct)
   * @throws LoginException if an error occurs while calling the callback
   *         handler or the {@link #checkLogin} method
   * @see #checkLogin
   */
  public final boolean login() throws LoginException  {
System.out.println("UserLoginModule.login");
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("Username: ");
    callbacks[1] = new PasswordCallback("Password: ", false);
    // Interact with the user to retrieve the username and password
    String userName = null;
    String password = null;
    try  {
      this.callbackHandler.handle(callbacks);
      userName = ((NameCallback) callbacks[0]).getName();
      password = new String(((PasswordCallback) callbacks[1]).getPassword());
    } catch (IOException e)  {
      throw new LoginException(e.toString());
    } catch (UnsupportedCallbackException e)  {
      throw new LoginException(e.toString());
    }

    if (!checkLogin(userName, password))  {
      throw new FailedLoginException("Username or password is incorrect");
    }

    this.principal = new UserPrincipal(userName);

    log.debug("login " + userName + " " + this.principal);

    return true;
  }

  /**
   *
   */
  public final boolean commit() throws LoginException  {
System.out.println("UserLoginModule.commit");
    boolean ret = true;

    // If authentication was not successful, just return false
    if (this.principal == null)  {
      return (false);
    }

    // Add our Principal to the Subject if needed
    if (!this.subject.getPrincipals().contains(this.principal))  {
      this.subject.getPrincipals().add(this.principal);
this.subject.getPrincipals().add(new RolePrincipal("manager"));
this.subject.getPrincipals().add(new RolePrincipal("admin"));
this.subject.getPrincipals().add(new RolePrincipal("manager2"));
    }

    this.committed = true;
    return ret;
  }

  /**
   *
   */
  public final boolean abort()  {
    boolean ret = false;

    log.debug("Abort of " + this.principal);

    // If our authentication was successful, just return false
    if (this.principal != null)  {

      // Clean up if overall authentication failed
      if (this.committed)  {
        subject.getPrincipals().remove(principal);
      }
      this.committed = false;
      this.principal = null;
      ret = true;
    }
    return ret;
  }

  /**
   * @return always <i>true</i>
   */
  public final boolean logout()  {
    log.debug("Logout of " + this.principal);

    subject.getPrincipals().remove(principal);
    this.committed = false;
    this.principal = null;
    return true;
  }

  /**
   * The instance method checks if for the given user the password is correct
   * and the person is active (status equals 10001).<br/>
   * All exceptions which could be thrown from the test are catched. Instead
   * a <i>false</i> is returned.
   *
   * @param _name   name of the person name to check
   * @param _passwd password of the person to check
   * @return <i>true</i> if user name and password is correct and exists,
   *         otherwise <i>false</i> is returned
   */
  private boolean checkLogin(final String _name, final String _passwd)  {
    boolean ret = false;
    Context context = null;
    try  {
      context = new Context();
      if (_name != null)  {
        Person person = Person.get(_name);
        ret = person.checkPassword(context, _passwd);
      }
    } catch (Throwable e)  {
      log.warn("Check  of login failed for user '" + _name + "'", e);
    } finally  {
      context.close();
    }
    return ret;
  }
}
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

package org.efaps.jaas.efaps;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

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

import org.efaps.admin.user.Group;
import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class UserLoginModule implements LoginModule  {

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(UserLoginModule.class);

  /**
   * The string stores the name of the JAAS system. The default value is
   * <b>eFaps</b>, but could changed with {@link #setJaasSystem}
   */
  private String jaasSystem = "eFaps";

  /**
   * Has our own <code>commit()</code> returned successfully?
   */
  private boolean committed = false;

  /**
   * The subject is stored from the initialize method to store all single
   * entities for the person logging in.
   */
  private Subject subject = null;

  // initial state
  private CallbackHandler   callbackHandler;

  private Principal principal = null;

  /**
   * @param _subject
   * @param _callbackHandler
   * @param _sharedState
   * @param _options
   */
  public final void initialize(final Subject _subject,
                               final CallbackHandler _callbackHandler,
                               final Map < String, ? > _sharedState,
                               final Map < String, ? > _options)  {

    if (LOG.isDebugEnabled())  {
      LOG.debug("Init");
    }
    this.subject = _subject;
    this.callbackHandler = _callbackHandler;

    String jaasSystem = (String)_options.get("jaasSystem");
    if (jaasSystem != null)  {
      this.jaasSystem = jaasSystem;
    }
  }

// TODO: Rework description
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
   * @return <i>true</i> if login is allowed and user name with password is
   *         correct
   * @throws FailedLoginException if login is not allowed with given user name
   *         and password (if user does not exists or password is not correct)
   * @throws LoginException if an error occurs while calling the callback
   *         handler or the {@link #checkLogin} method
   * @throws LoginException if user or password could not be get from the
   *         callback handler
   */
  public final boolean login() throws LoginException  {
    boolean ret = false;

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
      LOG.error("login failed for user '" + userName + "'", e);
      throw new LoginException(e.toString());
    } catch (UnsupportedCallbackException e)  {
      LOG.error("login failed for user '" + userName + "'", e);
      throw new LoginException(e.toString());
    }

    if (userName != null)  {
      try  {
        Person person = Person.getWithJAASKey(
                            JAASSystem.getJAASSystem(this.jaasSystem), userName);
        if (person != null)  {
          if (!person.checkPassword(password))  {
            throw new FailedLoginException("Username or password is incorrect");
          }
          ret = true;
          this.principal = new PersonPrincipal(userName);
          if (LOG.isDebugEnabled())  {
            LOG.debug("login " + userName + " " + this.principal);
          }
        }
      } catch (EFapsException e)  {
        LOG.error("login failed for user '" + userName + "'", e);
        throw new LoginException(e.toString());
      }
    }
    return ret;
  }

  /**
   * Adds the principal person and all found roles for the given JAAS system
   * {@link #jaasSystem} related to the person.
   *
   * @return <i>true</i> if authentification was successful,
   *         otherwise <i>false</i>
   */
  public final boolean commit() throws LoginException  {
    boolean ret = true;

    // If authentication was not successful, just return false
    if (this.principal == null)  {
      return (false);
    }

    // Add our Principal and Related Roles to the Subject if needed
    if (!this.subject.getPrincipals().contains(this.principal))  {
      this.subject.getPrincipals().add(this.principal);

      try  {
        JAASSystem jaasSystem = JAASSystem.getJAASSystem(this.jaasSystem);
        Person person = Person.getWithJAASKey(jaasSystem, this.principal.getName());
        if (person != null)  {
          Set < Role > roles = person.getRolesFromDB(jaasSystem);
          for (Role role : roles)  {
            this.subject.getPrincipals().add(new RolePrincipal(role.getName()));
          }
          Set < Group > groups = person.getGroupsFromDB(jaasSystem);
          for (Group group : groups)  {
            this.subject.getPrincipals().add(new GroupPrincipal(group.getName()));
          }
        }
      } catch (EFapsException e)  {
e.printStackTrace();
        LOG.error("assign of roles to user '" + this.principal.getName()
                                                        + "' not possible", e);
// TODO: throw LoginException
//        throw new LoginException(e);
      }
    }

    this.committed = true;
    return ret;
  }

  /**
   *
   */
  public final boolean abort()  {
    boolean ret = false;

    if (LOG.isDebugEnabled())  {
      LOG.debug("Abort of " + this.principal);
    }

    // If our authentication was successful, just return false
    if (this.principal != null)  {

      // Clean up if overall authentication failed
      if (this.committed)  {
        this.subject.getPrincipals().remove(principal);
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
    if (LOG.isDebugEnabled())  {
      LOG.debug("Logout of " + this.principal);
    }

    this.subject.getPrincipals().remove(this.principal);
    this.committed = false;
    this.principal = null;
    return true;
  }
}
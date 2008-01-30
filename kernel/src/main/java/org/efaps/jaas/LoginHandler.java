/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.jaas;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.user.Group;
import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.util.EFapsException;

/**
 * The login handler is used to handle the interface between JAAS and eFaps.
 * With the name and password of the user method {@link #checkLogin} could be
 * used to test if the user is allowed to login. The method returns then the
 * related person to the given name and password (if found) or <i>null</i> (if
 * not found)..
 * 
 * @author tmo
 * @version $Id$
 */
public class LoginHandler {

  // ////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private static Logger LOG = LoggerFactory.getLogger(LoginHandler.class);

  // ////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The name of the application used to create a new login context. The default
   * value is <code>eFaps</code>.
   * 
   * @see #checkLogin
   */
  private String application = "eFaps";

  // ////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to initialize the login handler. If <i>null</i> is given to
   * the application name, the default value defined in {@link #application} is
   * used.
   * 
   * @param _application
   *                application name of the JAAS configuration
   */
  public LoginHandler(final String _application) {
    if (_application != null) {
      this.application = _application;
    }
  }

  // ////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The instance method checks if for the given user the password is correct.
   * The test itself is done with the JAAS module from Java.<br/> If a person
   * is found and successfully logged in, the last login information from the
   * person is updated to current timestamp.
   * 
   * @param _name
   *                name of the person name to check
   * @param _passwd
   *                password of the person to check
   * @return found person
   * @see #getPerson
   * @see #createPerson
   * @see #updatePerson
   * @see #updateRoles
   * @see #updateGroups
   */
  public Person checkLogin(final String _name, final String _passwd) {
    Person person = null;
    try {
      LoginContext login =
          new LoginContext(getApplication(), new LoginCallbackHandler(
              ActionCallback.Mode.Login, _name, _passwd));
      login.login();

      person = getPerson(login);

      if (person == null) {
        person = createPerson(login);
      }

      if (person != null) {

        updatePerson(login, person);

        person.cleanUp();

        updateRoles(login, person);
        updateGroups(login, person);

        person.updateLastLogin();
      }
    } catch (EFapsException e) {
      LOG.error("login failed for '" + _name + "'", e);
    } catch (LoginException e) {
      LOG.error("login failed for '" + _name + "'", e);
    }
    return person;
  }

  /**
   * For the given JAAS login context the person inside eFaps is searched. If
   * more than one person is related to the JAAS login context, an exception is
   * thrown. If no person is found, <i>null</i> is returned.
   * 
   * @param _login
   *                JAAS login context
   * @return <i>null</i> if no person in eFaps is found for given JAAS login
   *         context
   * @throws EFapsException
   *                 if more than one person for given JAAS login context is
   *                 found or a method of the principals inside the JAAS login
   *                 contexts could not be executed.
   */
  protected Person getPerson(final LoginContext _login) throws EFapsException {
    Person person = null;
    for (JAASSystem system : JAASSystem.getAllJAASSystems()) {
      Set<?> users =
          _login.getSubject().getPrincipals(
              system.getPersonJAASPrincipleClass());

      for (Object persObj : users) {
        try {
          String persKey =
              (String) system.getPersonMethodKey().invoke(persObj);

          Person foundPerson = Person.getWithJAASKey(system, persKey);
          if (foundPerson == null) {
            // TODO: muss noch gemacht werden!!! da funkt halt was nicht...
            // person.assignToJAASSystem(system, persKey);
          } else if (person == null) {
            person = foundPerson;
          } else if (person.getId() != foundPerson.getId()) {
            LOG.error("For JAAS system " + system.getName() + " "
                + "person with key '" + persKey + "' is not unique!"
                + "Have found person '" + person.getName() + "' " + "(id = "
                + person.getId() + ") and person " + "'"
                + foundPerson.getName() + "' " + "(id = " + foundPerson.getId()
                + ").");
            // TODO: throw exception!!
          }
        } catch (IllegalAccessException e) {
          LOG.error("could not execute person key method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        } catch (IllegalArgumentException e) {
          LOG.error("could not execute person key method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        } catch (InvocationTargetException e) {
          LOG.error("could not execute person key method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        }
      }
    }
    return person;
  }

  /**
   * The person represented in the JAAS login context is created and associated
   * to eFaps. If the person is defined in more than one JAAS system, the person
   * is also assiciated to the other JAAS systems.
   * 
   * @param _login
   *                JAAS login context
   * @return Java instance of newly created person
   * @throws EFapsException
   *                 if a method of the principals inside the JAAS login
   *                 contexts could not be executed.
   */
  protected Person createPerson(final LoginContext _login)
      throws EFapsException {
    Person person = null;

    for (JAASSystem system : JAASSystem.getAllJAASSystems()) {
      Set<?> users =
          _login.getSubject().getPrincipals(
              system.getPersonJAASPrincipleClass());
      for (Object persObj : users) {
        try {
          String persKey =
              (String) system.getPersonMethodKey().invoke(persObj);
          String persName =
              (String) system.getPersonMethodName().invoke(persObj);

          if (person == null) {
            person = Person.createPerson(system, persKey, persName);
          } else {
            person.assignToJAASSystem(system, persKey);
          }

        } catch (IllegalAccessException e) {
          LOG.error("could not execute a person method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        } catch (IllegalArgumentException e) {
          LOG.error("could not execute a person method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        } catch (InvocationTargetException e) {
          LOG.error("could not execute a person method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        }
      }
    }
    return person;
  }

  /**
   * The person information inside eFaps is update with information from JAAS
   * login context.
   * 
   * @param _login
   *                JAAS login context
   * @param _person
   *                Java person instance inside eFaps to update
   * @throws EFapsException
   *                 if a method of the principals inside the JAAS login
   *                 contexts could not be executed or the person could not be
   *                 updated from the values in the JAAS login context.
   */
  protected void updatePerson(final LoginContext _login, final Person _person)
      throws EFapsException {

    for (JAASSystem system : JAASSystem.getAllJAASSystems()) {
      Set<?> users =
          _login.getSubject().getPrincipals(
              system.getPersonJAASPrincipleClass());
      for (Object persObj : users) {
        try {
          for (Map.Entry<Person.AttrName, Method> entry : system
              .getPersonMethodAttributes().entrySet()) {
            _person.updateAttrValue(entry.getKey(), (String) entry.getValue()
                .invoke(persObj));
          }

        } catch (IllegalAccessException e) {
          LOG.error("could not execute a person method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        } catch (IllegalArgumentException e) {
          LOG.error("could not execute a person method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        } catch (InvocationTargetException e) {
          LOG.error("could not execute a person method for system "
              + system.getName(), e);
          // TODO: throw exception!!
        }
      }
    }
    _person.commitAttrValuesInDB();
  }

  /**
   * The roles of the given person are updated with the information from the
   * JAAS login context.
   * 
   * @param _login
   *                JAAS login context
   * @param _person
   *                person for which the roles must be updated
   * @throws EFapsException
   *                 if a method of the principals inside the JAAS login
   *                 contexts could not be executed or the roles for the given
   *                 person could not be set.
   */
  protected void updateRoles(final LoginContext _login, final Person _person)
      throws EFapsException {

    for (JAASSystem system : JAASSystem.getAllJAASSystems()) {
      if (system.getRoleJAASPrincipleClass() != null) {
        Set<?> rolesJaas =
            _login.getSubject().getPrincipals(
                system.getRoleJAASPrincipleClass());
        Set<Role> rolesEfaps = new HashSet<Role>();
        for (Object roleObj : rolesJaas) {
          try {
            String roleKey = (String) system.getRoleMethodKey().invoke(roleObj);
            Role roleEfaps = Role.getWithJAASKey(system, roleKey);
            if (roleEfaps != null) {
              rolesEfaps.add(roleEfaps);
            }
          } catch (IllegalAccessException e) {
            LOG.error("could not execute role key method for system "
                + system.getName(), e);
          } catch (IllegalArgumentException e) {
            LOG.error("could not execute role key method for system "
                + system.getName(), e);
          } catch (InvocationTargetException e) {
            LOG.error("could not execute role key method for system "
                + system.getName(), e);
          }
        }
        _person.setRoles(system, rolesEfaps);
      }
    }
  }

  /**
   * The groups of the given person are updated with the information from the
   * JAAS login context.
   * 
   * @param _login
   *                JAAS login context
   * @param _person
   *                person for which the groups must be updated
   * @throws EFapsException
   *                 if a method of the principals inside the JAAS login
   *                 contexts could not be executed or the groups for the given
   *                 person could not be set.
   */
  protected void updateGroups(final LoginContext _login, final Person _person)
      throws EFapsException {

    for (JAASSystem system : JAASSystem.getAllJAASSystems()) {
      if (system.getGroupJAASPrincipleClass() != null) {
        Set<?> groupsJaas =
            _login.getSubject().getPrincipals(
                system.getGroupJAASPrincipleClass());
        Set<Group> groupsEfaps = new HashSet<Group>();
        for (Object groupObj : groupsJaas) {
          try {
            String groupKey =
                (String) system.getGroupMethodKey().invoke(groupObj);
            Group groupEfaps = Group.getWithJAASKey(system, groupKey);
            if (groupEfaps != null) {
              groupsEfaps.add(groupEfaps);
            }
          } catch (IllegalAccessException e) {
            LOG.error("could not execute group key method for system "
                + system.getName(), e);
          } catch (IllegalArgumentException e) {
            LOG.error("could not execute group key method for system "
                + system.getName(), e);
          } catch (InvocationTargetException e) {
            LOG.error("could not execute group key method for system "
                + system.getName(), e);
          }
        }
        _person.setGroups(system, groupsEfaps);
      }
    }
  }

  // ////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for instance variable {@link #application}.
   * 
   * @return the value of the instance variable {@link #application}.
   * @see #application
   */
  public String getApplication() {
    return this.application;
  }

  // ////////////////////////////////////////////////////////////////////////////
  // internal classes

  /**
   * Class used to handle the call to the JAAS login handler. It's used to
   * return the name and password on request from the implementing login
   * modules.
   */
  protected class LoginCallbackHandler implements CallbackHandler {

    /**
     * The user name to test is stored in this instance variable.
     */
    private final String name;

    /**
     * The password used from the user is stored in this instance variable.
     */
    private final String password;

    /**
     * The action mode for which the login must be made is stored in this
     * instance variable (e.g. login, information about all persons, etc.)
     */
    private final ActionCallback.Mode mode;

    /**
     * Constructor initializing the action, name and password in this call back
     * handler.
     * 
     * @param _action
     *                defines action for which the login is made
     * @param _name
     *                name of the login user
     * @param _passwd
     *                password of the login user
     * @see #action
     * @see #name
     * @see #password
     */
    protected LoginCallbackHandler(final ActionCallback.Mode _mode,
                                   final String _name, final String _passwd) {
      this.mode = _mode;
      this.name = _name;
      this.password = _passwd;
    }

    /**
     * The handler sets for instances of {@link NameCallBack} the given
     * {@link #name} and for instances of {@link PasswordCallBack} the given
     * {@link #password}. {@link TextOutputCallBack} instances are ignored.
     * 
     * @param _callbacks
     *                callback instances to handle
     * @throws UnsupportedCallbackException
     *                 for all {@link Callback} instances which are not
     *                 {@link NameCallBack}, {@link PasswordCallBack} or
     *                 {@link TextOutputCallBack}.
     */
    public void handle(final Callback[] _callbacks) throws IOException,
        UnsupportedCallbackException {

      for (int i = 0; i < _callbacks.length; i++) {
        if (_callbacks[i] instanceof ActionCallback) {
          ActionCallback ac = (ActionCallback) _callbacks[i];
          ac.setMode(this.mode);
        } else if (_callbacks[i] instanceof NameCallback) {
          NameCallback nc = (NameCallback) _callbacks[i];
          nc.setName(this.name);
        } else if (_callbacks[i] instanceof PasswordCallback) {
          if (this.password != null) {
            PasswordCallback pc = (PasswordCallback) _callbacks[i];
            pc.setPassword(this.password.toCharArray());
          }
        } else if (_callbacks[i] instanceof TextOutputCallback) {
          // do nothing, TextOutputCallBack's are ignored!
        } else {
          throw new UnsupportedCallbackException(_callbacks[i],
              "Unrecognized Callback");
        }
      }
    }
  }
}

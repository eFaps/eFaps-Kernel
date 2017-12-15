/*
 * Copyright 2003 - 2016 The eFaps Team
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

import org.efaps.admin.user.Group;
import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The login handler is used to handle the interface between JAAS and eFaps.
 * With the name and password of the user method {@link #checkLogin} could be
 * used to test if the user is allowed to login. The method returns then the
 * related person to the given name and password (if found) or <i>null</i> (if
 * not found)..
 *
 * @author The eFaps Team
 *
 */
public class LoginHandler
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static Logger LOG = LoggerFactory.getLogger(LoginHandler.class);

    /**
     * The name of the application used to create a new login context. The
     * default value is <code>eFaps</code>.
     *
     * @see #checkLogin(String, String)
     */
    private String applicationName = "eFaps";

    /**
     * Constructor to initialize the login handler. If <i>null</i> is given to
     * the application name, the default value defined in {@link #applicationName}
     * is used.
     *
     * @param _application  application name of the JAAS configuration
     */
    public LoginHandler(final String _application)
    {
        if (_application != null) {
            this.applicationName = _application;
        }
    }

    /**
     * The instance method checks if for the given user the password is
     * correct. The test itself is done with the JAAS module from Java.<br/> If
     * a person is found and successfully logged in, the last login information
     * from the person is updated to current time stamp.
     *
     * @param _name     name of the person name to check
     * @param _passwd   password of the person to check
     * @return found person
     * @see #getPerson(LoginContext)
     * @see #createPerson(LoginContext)
     * @see #updatePerson(LoginContext, Person)
     * @see #updateRoles(LoginContext, Person)
     * @see #updateGroups(LoginContext, Person)
     */
    public Person checkLogin(final String _name,
                             final String _passwd)
    {
        Person person = null;
        try {
            final LoginCallbackHandler callback = new LoginCallbackHandler(ActionCallback.Mode.LOGIN, _name, _passwd);
            final LoginContext login = new LoginContext(getApplicationName(), callback);
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
                updateCompanies(login, person);

                person.updateLastLogin();
            }
        } catch (final EFapsException e) {
            LoginHandler.LOG.error("login failed for '" + _name + "'", e);
        } catch (final LoginException e) {
            LoginHandler.LOG.error("login failed for '" + _name + "'", e);
        }
        return person;
    }

    /**
     * For the given JAAS login context the person inside eFaps is searched. If
     * more than one person is related to the JAAS login context, an exception
     * is thrown. If no person is found, <code>null</code> is returned.
     *
     * @param _login    JAAS login context
     * @return <i>null</i> if no person in eFaps is found for given JAAS login
     *         context
     * @throws EFapsException   if more than one person for given JAAS login
     *                          context is found or a method of the principals
     *                          inside the JAAS login contexts could not be
     *                          executed.
     */
    protected Person getPerson(final LoginContext _login)
        throws EFapsException
    {
        Person person = null;
        for (final JAASSystem system : JAASSystem.getAllJAASSystems()) {
            final Set<?> users = _login.getSubject().getPrincipals(system.getPersonJAASPrincipleClass());

            for (final Object persObj : users) {
                try {
                    final String persKey = (String) system.getPersonMethodKey().invoke(persObj);

                    final Person foundPerson = Person.getWithJAASKey(system, persKey);
                    if (foundPerson == null) {
                        person.assignToJAASSystem(system, persKey);
                    } else if (person == null) {
                        person = foundPerson;
                    } else if (person.getId() != foundPerson.getId()) {
                        LoginHandler.LOG.error("For JAAS system " + system.getName() + " "
                            + "person with key '" + persKey + "' is not unique!"
                            + "Have found person '" + person.getName() + "' " + "(id = "
                            + person.getId() + ") and person " + "'"
                            + foundPerson.getName() + "' " + "(id = " + foundPerson.getId()
                            + ").");
                        throw new EFapsException(LoginHandler.class, "notFound", persKey);
                    }
                } catch (final IllegalAccessException e) {
                    LoginHandler.LOG.error("could not execute person key method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "IllegalAccessException", e);
                } catch (final IllegalArgumentException e) {
                    LoginHandler.LOG.error("could not execute person key method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "IllegalArgumentException", e);
                } catch (final InvocationTargetException e) {
                    LoginHandler.LOG.error("could not execute person key method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "InvocationTargetException", e);
                }
            }
        }
        return person;
    }

    /**
     * The person represented in the JAAS login context is created and
     * associated to eFaps. If the person is defined in more than one JAAS
     * system, the person is also associated to the other JAAS systems.
     *
     * @param _login    JAAS login context
     * @return Java instance of newly created person
     * @throws EFapsException if a method of the principals inside the JAAS
     *                        login contexts could not be executed.
     */
    protected Person createPerson(final LoginContext _login)
        throws EFapsException
    {
        Person person = null;

        for (final JAASSystem system : JAASSystem.getAllJAASSystems()) {
            final Set<?> users = _login.getSubject().getPrincipals(system.getPersonJAASPrincipleClass());
            for (final Object persObj : users) {
                try {
                    final String persKey = (String) system.getPersonMethodKey().invoke(persObj);
                    final String persName = (String) system.getPersonMethodName().invoke(persObj);

                    if (person == null) {
                        person = Person.createPerson(system, persKey, persName, null);
                    } else {
                        person.assignToJAASSystem(system, persKey);
                    }
                } catch (final IllegalAccessException e) {
                    LoginHandler.LOG.error("could not execute a person method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "IllegalAccessException", e);
                } catch (final IllegalArgumentException e) {
                    LoginHandler.LOG.error("could not execute a person method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "IllegalArgumentException", e);
                } catch (final InvocationTargetException e) {
                    LoginHandler.LOG.error("could not execute a person method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "InvocationTargetException", e);
                }
            }
        }
        return person;
    }

    /**
     * The person information inside eFaps is update with information from JAAS
     * login context.
     *
     * @param _login    JAAS login context
     * @param _person   Java person instance inside eFaps to update
     * @throws EFapsException if a method of the principals inside the JAAS
     *                        login contexts could not be executed or the
     *                        person could not be updated from the values in
     *                        the JAAS login context.
     */
    protected void updatePerson(final LoginContext _login,
                                final Person _person)
        throws EFapsException
    {
        for (final JAASSystem system : JAASSystem.getAllJAASSystems()) {
            final Set<?> users = _login.getSubject().getPrincipals(system.getPersonJAASPrincipleClass());
            for (final Object persObj : users) {
                try {
                    for (final Map.Entry<Person.AttrName, Method> entry
                            : system.getPersonMethodAttributes().entrySet()) {
                        _person.updateAttrValue(entry.getKey(), (String) entry.getValue().invoke(persObj));
                    }
                } catch (final IllegalAccessException e) {
                    LoginHandler.LOG.error("could not execute a person method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "IllegalAccessException", e);
                } catch (final IllegalArgumentException e) {
                    LoginHandler.LOG.error("could not execute a person method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "IllegalArgumentException", e);
                } catch (final InvocationTargetException e) {
                    LoginHandler.LOG.error("could not execute a person method for system " + system.getName(), e);
                    throw new EFapsException(LoginHandler.class, "InvocationTargetException", e);
                }
            }
        }
        _person.commitAttrValuesInDB();
    }

    /**
     * The roles of the given person are updated with the information from the
     * JAAS login context.
     *
     * @param _login    JAAS login context
     * @param _person   person for which the roles must be updated
     * @throws EFapsException if a method of the principals inside the JAAS
     *                        login contexts could not be executed or the roles
     *                        for the given person could not be set.
     */
    protected void updateRoles(final LoginContext _login,
                               final Person _person)
        throws EFapsException
    {
        for (final JAASSystem system : JAASSystem.getAllJAASSystems()) {
            if (system.getRoleJAASPrincipleClass() != null) {
                final Set<?> rolesJaas = _login.getSubject().getPrincipals(system.getRoleJAASPrincipleClass());
                final Set<Role> rolesEfaps = new HashSet<>();
                for (final Object roleObj : rolesJaas) {
                    try {
                        final String roleKey = (String) system.getRoleMethodKey().invoke(roleObj);
                        final Role roleEfaps = Role.getWithJAASKey(system, roleKey);
                        if (roleEfaps != null) {
                            rolesEfaps.add(roleEfaps);
                        }
                    } catch (final IllegalAccessException e) {
                        LoginHandler.LOG.error("could not execute role key method for system " + system.getName(), e);
                    } catch (final IllegalArgumentException e) {
                        LoginHandler.LOG.error("could not execute role key method for system " + system.getName(), e);
                    } catch (final InvocationTargetException e) {
                        LoginHandler.LOG.error("could not execute role key method for system " + system.getName(), e);
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
     * @param _login    JAAS login context
     * @param _person   person for which the groups must be updated
     * @throws EFapsException if a method of the principals inside the JAAS
     *                        login contexts could not be executed or the
     *                        groups for the given person could not be set.
     */
    protected void updateGroups(final LoginContext _login,
                                final Person _person)
        throws EFapsException
    {
        for (final JAASSystem system : JAASSystem.getAllJAASSystems()) {
            if (system.getGroupJAASPrincipleClass() != null) {
                final Set<?> groupsJaas = _login.getSubject().getPrincipals(system.getGroupJAASPrincipleClass());
                final Set<Group> groupsEfaps = new HashSet<>();
                for (final Object groupObj : groupsJaas) {
                    try {
                        final String groupKey = (String) system.getGroupMethodKey().invoke(groupObj);
                        final Group groupEfaps = Group.getWithJAASKey(system, groupKey);
                        if (groupEfaps != null) {
                            groupsEfaps.add(groupEfaps);
                        }
                    } catch (final IllegalAccessException e) {
                        LoginHandler.LOG.error("could not execute group key method for system " + system.getName(), e);
                    } catch (final IllegalArgumentException e) {
                        LoginHandler.LOG.error("could not execute group key method for system " + system.getName(), e);
                    } catch (final InvocationTargetException e) {
                        LoginHandler.LOG.error("could not execute group key method for system " + system.getName(), e);
                    }
                }
                _person.setGroups(system, groupsEfaps);
            }
        }
    }

    /**
     * The groups of the given person are updated with the information from the
     * JAAS login context.
     * TODO: no real check or update is done
     * @param _login    JAAS login context
     * @param _person   person for which the groups must be updated
     * @throws EFapsException if a method of the principals inside the JAAS
     *                        login contexts could not be executed or the
     *                        groups for the given person could not be set.
     */
    protected void updateCompanies(final LoginContext _login,
                                   final Person _person)
        throws EFapsException
    {
        if (!JAASSystem.getAllJAASSystems().isEmpty()) {
            _person.setCompanies(JAASSystem.getAllJAASSystems().iterator().next(), _person.getCompaniesFromDB(null));
        }
    }

    /**
     * This is the getter method for instance variable {@link #applicationName}.
     *
     * @return the value of the instance variable {@link #applicationName}.
     * @see #applicationName
     */
    public String getApplicationName()
    {
        return this.applicationName;
    }

    /**
     * Class used to handle the call to the JAAS login handler. It's used to
     * return the name and password on request from the implementing login
     * modules.
     */
    protected class LoginCallbackHandler
        implements CallbackHandler
    {
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
         * Constructor initializing the action, name and password in this call
         * back handler.
         *
         * @param _mode     defines mode for which the login is made
         * @param _name     name of the login user
         * @param _passwd   password of the login user
         * @see #mode
         * @see #name
         * @see #password
         */
        protected LoginCallbackHandler(final ActionCallback.Mode _mode,
                                       final String _name,
                                       final String _passwd)
        {
            this.mode = _mode;
            this.name = _name;
            this.password = _passwd;
        }

        /**
         * The handler sets for instances of {@link NameCallback} the given
         * {@link #name} and for instances of {@link PasswordCallback} the given
         * {@link #password}. {@link TextOutputCallback} instances are ignored.
         *
         * @param _callbacks    callback instances to handle
         * @throws UnsupportedCallbackException for all {@link Callback}
         *                  instances which are not {@link NameCallback},
         *                  {@link PasswordCallback} or
         *                  {@link TextOutputCallback}
         */
        @Override
        public void handle(final Callback[] _callbacks)
            throws UnsupportedCallbackException
        {
            for (int i = 0; i < _callbacks.length; i++) {
                if (_callbacks[i] instanceof ActionCallback) {
                    final ActionCallback ac = (ActionCallback) _callbacks[i];
                    ac.setMode(this.mode);
                } else if (_callbacks[i] instanceof NameCallback) {
                    final NameCallback nc = (NameCallback) _callbacks[i];
                    nc.setName(this.name);
                } else if (_callbacks[i] instanceof PasswordCallback) {
                    if (this.password != null) {
                        final PasswordCallback pc = (PasswordCallback) _callbacks[i];
                        pc.setPassword(this.password.toCharArray());
                    }
                } else if (!(_callbacks[i] instanceof TextOutputCallback)) {
                    throw new UnsupportedCallbackException(_callbacks[i], "Unrecognized Callback");
                }
            }
        }
    }
}

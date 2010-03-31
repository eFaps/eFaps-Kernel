/*
 * Copyright 2003 - 2010 The eFaps Team
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

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.efaps.admin.user.Group;
import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Role;
import org.efaps.jaas.ActionCallback;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class UserLoginModule
    implements LoginModule
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserLoginModule.class);

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
    private CallbackHandler callbackHandler;

    private Principal principal = null;

    /**
     * @param _subject
     * @param _callbackHandler
     * @param _sharedState
     * @param _options
     */
    public final void initialize(final Subject _subject,
                                 final CallbackHandler _callbackHandler,
                                 final Map<String, ?> _sharedState,
                                 final Map<String, ?> _options)
    {
        if (UserLoginModule.LOG.isDebugEnabled()) {
            UserLoginModule.LOG.debug("Init");
        }
        this.subject = _subject;
        this.callbackHandler = _callbackHandler;

        final String jaasSystem = (String) _options.get("jaasSystem");
        if (jaasSystem != null) {
            this.jaasSystem = jaasSystem;
        }
    }

    /**
     * This instance method is used for two different use cases:
     * <ul>
     * <li>the login of a User, it checks if for the given user the password is
     *     correct</li>
     * <li>the Change of a Password of a User, it checks first if the User can
     *     login an then sets the new Password</li>
     * </ul>
     *
     * @return <i>true</i> if user name and password is correct and exists,
     *         otherwise <i>false</i> is returned
     * @throws FailedLoginException
     *                 if login is not allowed with given user name and password
     *                 (if user does not exists or password is not correct)
     * @throws LoginException
     *                 if an error occurs while calling the callback handler or
     *                 the {@link #checkLogin} method
     * @throws LoginException
     *                 if user or password could not be get from the callback
     *                 handler
     * @throws UpdateException
     *                 if the new Password could not be set by the Update, due to
     *                 some restriction
     */
    public final boolean login()
        throws LoginException
    {
        boolean ret = false;

        final Callback[] callbacks = new Callback[4];
        callbacks[0] = new ActionCallback();
        callbacks[1] = new NameCallback("Username: ");
        callbacks[2] = new PasswordCallback("Password", false);
        callbacks[3] = new PasswordCallback("newPassword", false);
        // Interact with the user to retrieve the username and passwords

        String userName = null;
        String password = null;
        String newPassword = null;
        ActionCallback.Mode mode = null;

        try {
            this.callbackHandler.handle(callbacks);
            mode = ((ActionCallback) callbacks[0]).getMode();
            userName = ((NameCallback) callbacks[1]).getName();
            password = new String(((PasswordCallback) callbacks[2]).getPassword());
            newPassword = new String(((PasswordCallback) callbacks[3]).getPassword());
        } catch (final IOException e) {
            UserLoginModule.LOG.error("login failed for user '" + userName + "'", e);
            throw new LoginException(e.toString());
        } catch (final UnsupportedCallbackException e) {
            UserLoginModule.LOG.error("login failed for user '" + userName + "'", e);
            throw new LoginException(e.toString());
        }

        if (userName != null) {
            try {
                final Person person = Person.getWithJAASKey(JAASSystem.getJAASSystem(this.jaasSystem), userName);
                if (person != null) {
                    if (!person.checkPassword(password)) {
                        throw new FailedLoginException("Username or password is incorrect");
                    }

                    ret = true;
                    if (mode.equals(ActionCallback.Mode.SET_PASSWORD)) {
                        try {
                            person.setPassword(newPassword);
                        } catch (final Exception e) {
                            throw new UpdateException();
                        }
                    }
                    this.principal = new PersonPrincipal(userName);
                    if (UserLoginModule.LOG.isDebugEnabled()) {
                        UserLoginModule.LOG.debug("login " + userName + " " + this.principal);
                    }
                }
            } catch (final EFapsException e) {
                UserLoginModule.LOG.error("login failed for user '" + userName + "'", e);
                throw new LoginException(e.toString());
            }
        }
        return ret;
    }

    /**
     * Adds the principal person and all found roles for the given JAAS system
     * {@link #jaasSystem} related to the person.
     *
     * @return <i>true</i> if authentification was successful, otherwise
     *         <i>false</i>
     */
    public final boolean commit()
        throws LoginException
    {
        final boolean ret;

        // If authentication was not successful, just return false
        if (this.principal == null) {
            ret = false;
        } else {
            ret = true;

            // Add our Principal and Related Roles to the Subject if needed
            if (!this.subject.getPrincipals().contains(this.principal)) {
                this.subject.getPrincipals().add(this.principal);

                try {
                    final JAASSystem jaasSystem = JAASSystem.getJAASSystem(this.jaasSystem);
                    final Person person = Person.getWithJAASKey(jaasSystem, this.principal.getName());
                    if (person != null) {
                        final Set<Role> roles = person.getRolesFromDB(jaasSystem);
                        for (final Role role : roles) {
                            this.subject.getPrincipals().add(new RolePrincipal(role.getName()));
                        }
                        final Set<Group> groups = person.getGroupsFromDB(jaasSystem);
                        for (final Group group : groups) {
                            this.subject.getPrincipals().add(new GroupPrincipal(group.getName()));
                        }
                    }
                } catch (final EFapsException e) {
                    UserLoginModule.LOG.error("assign of roles to user '"
                            + this.principal.getName() + "' not possible", e);
                    throw new LoginException(e.toString());
                }
            }
        }

        this.committed = true;
        return ret;
    }

    /**
     *
     */
    public final boolean abort()
    {
        boolean ret = false;

        if (UserLoginModule.LOG.isDebugEnabled()) {
            UserLoginModule.LOG.debug("Abort of " + this.principal);
        }

        // If our authentication was successful, just return false
        if (this.principal != null) {

            // Clean up if overall authentication failed
            if (this.committed) {
                this.subject.getPrincipals().remove(this.principal);
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
    public final boolean logout()
    {
        if (UserLoginModule.LOG.isDebugEnabled()) {
            UserLoginModule.LOG.debug("Logout of " + this.principal);
        }

        this.subject.getPrincipals().remove(this.principal);
        this.committed = false;
        this.principal = null;
        return true;
    }

    /**
     * This class is used to throw an error which indicates that an Update was
     * not successful.
     */
    public class UpdateException extends LoginException
    {
        private static final long serialVersionUID = 1L;
    }
}

/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
import org.efaps.jaas.ActionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/***
 *
 * @author The eFaps Team
 *
 */
public class XMLUserLoginModule
    implements LoginModule
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XMLUserLoginModule.class);

    /**
     * Stores the mode in which mode the login module is called.
     */
    private ActionCallback.Mode mode = ActionCallback.Mode.UNDEFINED;

    /**
     * The subject is stored from the initialize method to store all single
     * entities for the person logging in.
     */
    private Subject subject = null;

    /**
     *
     */
    private CallbackHandler callbackHandler = null;

    /**
     * The logged in person principal is stored in this variable. The variable
     * is set if the user logged in and reset to null if the user is logged
     * out.
     */
    private XMLPersonPrincipal person = null;

    /**
     *
     */
    private final Map<String, XMLPersonPrincipal> allPersons = new HashMap<String, XMLPersonPrincipal>();

    /**
     * Initialize this LoginModule.
     *
     * <p> This method is called by the <code>LoginContext</code>
     * after this <code>LoginModule</code> has been instantiated.
     * The purpose of this method is to initialize this
     * <code>LoginModule</code> with the relevant information.
     * If this <code>LoginModule</code> does not understand
     * any of the data stored in <code>sharedState</code> or
     * <code>options</code> parameters, they can be ignored.
     *
     * <p>
     *
     * @param _subject      the <code>Subject</code> to be authenticated. <p>
     * @param _callbackHandler a <code>CallbackHandler</code> for communicating
     *          with the end user (prompting for usernames and
     *          passwords, for example). <p>
     * @param _sharedState state shared with other configured LoginModules. <p>
     * @param _options options specified in the login
     *          <code>Configuration</code> for this particular
     *          <code>LoginModule</code>.
     */
    @Override
    public final void initialize(final Subject _subject,
                                 final CallbackHandler _callbackHandler,
                                 final Map < String, ? > _sharedState,
                                 final Map < String, ? > _options)
    {
        XMLUserLoginModule.LOG.debug("Init");
        this.subject = _subject;
        this.callbackHandler = _callbackHandler;
        readPersons((String) _options.get("xmlFileName"));
    }

    /**
     * Method to authenticate a <code>Subject</code> (phase 1).
     *
     * <p> The implementation of this method authenticates
     * a <code>Subject</code>.  For example, it may prompt for
     * <code>Subject</code> information such
     * as a username and password and then attempt to verify the password.
     * This method saves the result of the authentication attempt
     * as private state within the LoginModule.
     *
     * <p>
     *
     * @exception LoginException if the authentication fails
     *
     * @return true if the authentication succeeded, or false if this
     *          <code>LoginModule</code> should be ignored.
     */
    public final boolean login()
        throws LoginException
    {
        boolean ret = false;

        final Callback[] callbacks = new Callback[3];
        callbacks[0] = new ActionCallback();
        callbacks[1] = new NameCallback("Username: ");
        callbacks[2] = new PasswordCallback("Password: ", false);
        // Interact with the user to retrieve the username and password
        String userName = null;
        String password = null;
        try {
            this.callbackHandler.handle(callbacks);
            this.mode = ((ActionCallback) callbacks[0]).getMode();
            userName = ((NameCallback) callbacks[1]).getName();
            if (((PasswordCallback) callbacks[2]).getPassword() != null) {
                password = new String(((PasswordCallback) callbacks[2]).getPassword());
            }
        } catch (final IOException e) {
            throw new LoginException(e.toString());
        } catch (final UnsupportedCallbackException e) {
            throw new LoginException(e.toString());
        }

        if (this.mode == ActionCallback.Mode.ALL_PERSONS) {
            ret = true;
        } else if (this.mode == ActionCallback.Mode.PERSON_INFORMATION) {
            this.person = this.allPersons.get(userName);
            if (this.person != null) {
                if (XMLUserLoginModule.LOG.isDebugEnabled()) {
                    XMLUserLoginModule.LOG.debug("found '" + this.person + "'");
                }
                ret = true;
            }
        } else {
            this.person = this.allPersons.get(userName);
            if (this.person != null) {
                if ((password == null)
                                || ((password != null)
                                && !password.equals(this.person.getPassword()))) {

                    XMLUserLoginModule.LOG.error("person '" + this.person + "' tried to log in with wrong password");
                    this.person = null;
                    throw new FailedLoginException("Username or password is incorrect");
                }
                if (XMLUserLoginModule.LOG.isDebugEnabled()) {
                    XMLUserLoginModule.LOG.debug("log in of '" + this.person + "'");
                }
                this.mode = ActionCallback.Mode.LOGIN;
                ret = true;
            }
        }

        return ret;
    }

    /**
     * Method to commit the authentication process (phase 2).
     *
     * <p> This method is called if the LoginContext's
     * overall authentication succeeded
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * succeeded).
     *
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates relevant
     * Principals and Credentials with the <code>Subject</code> located in the
     * <code>LoginModule</code>.  If this LoginModule's own
     * authentication attempted failed, then this method removes/destroys
     * any state that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the commit fails
     *
     * @return true if this method succeeded, or false if this
     *          <code>LoginModule</code> should be ignored.
     */
    public final boolean commit()
        throws LoginException
    {
        boolean ret = false;

        if (this.mode == ActionCallback.Mode.ALL_PERSONS)  {
            for (final XMLPersonPrincipal personTmp : this.allPersons.values())  {
                if (!this.subject.getPrincipals().contains(personTmp))  {
                    if (XMLUserLoginModule.LOG.isDebugEnabled())  {
                        XMLUserLoginModule.LOG.debug("commit person '" + personTmp + "'");
                    }
                    this.subject.getPrincipals().add(personTmp);
                }
            }
            ret = true;
        } else if (this.person != null)  {
            if (XMLUserLoginModule.LOG.isDebugEnabled())  {
                XMLUserLoginModule.LOG.debug("commit of '" + this.person + "'");
            }
            if (!this.subject.getPrincipals().contains(this.person))  {
                this.subject.getPrincipals().add(this.person);
                for (final XMLRolePrincipal principal : this.person.getRoles())  {
                    this.subject.getPrincipals().add(principal);
                }
                for (final XMLGroupPrincipal principal : this.person.getGroups())  {
                    this.subject.getPrincipals().add(principal);
                }
            }
            ret = true;
        }

        return ret;
    }

    /**
     * Method to abort the authentication process (phase 2).
     *
     * <p> This method is called if the LoginContext's
     * overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * did not succeed).
     *
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method cleans up any state
     * that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the abort fails
     *
     * @return true if this method succeeded, or false if this
     *          <code>LoginModule</code> should be ignored.
     */
    public final boolean abort()
        throws LoginException
    {
        boolean ret = false;

        if (this.person != null)  {
            if (XMLUserLoginModule.LOG.isDebugEnabled())  {
                XMLUserLoginModule.LOG.debug("abort of " + this.person);
            }
            this.subject.getPrincipals().remove(this.person);
            for (final XMLRolePrincipal principal : this.person.getRoles())  {
                this.subject.getPrincipals().remove(principal);
            }
            for (final XMLGroupPrincipal principal : this.person.getGroups())  {
                this.subject.getPrincipals().remove(principal);
            }
            this.person = null;
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
    public final boolean logout()
    {
        boolean ret = false;

        if (this.person != null)  {
            if (XMLUserLoginModule.LOG.isDebugEnabled())  {
                XMLUserLoginModule.LOG.debug("logout of " + this.person);
            }
            this.subject.getPrincipals().remove(this.person);
            for (final XMLRolePrincipal principal : this.person.getRoles())  {
                this.subject.getPrincipals().remove(principal);
            }
            for (final XMLGroupPrincipal principal : this.person.getGroups())  {
                this.subject.getPrincipals().remove(principal);
            }
            this.person = null;
            ret = true;
        }
        return ret;
    }

    /**
     * The name of the XML is store in this instance variable. The XML file
     * holds all allowed persons and their related roles and groups.
     *
     * @param _fileName name of the XML file with the user data
     */
    @SuppressWarnings("unchecked")
    private void readPersons(final String _fileName)
    {
        try  {
            final File file = new File(_fileName);

            final Digester digester = new Digester();
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

            digester.addCallMethod("persons/person/organisation", "setOrganisation", 1);
            digester.addCallParam("persons/person/organisation", 0);

            digester.addCallMethod("persons/person/url", "setUrl", 1);
            digester.addCallParam("persons/person/url", 0);

            digester.addCallMethod("persons/person/phone", "setPhone", 1);
            digester.addCallParam("persons/person/phone", 0);

            digester.addCallMethod("persons/person/mobile", "setMobile", 1);
            digester.addCallParam("persons/person/mobile", 0);

            digester.addCallMethod("persons/person/fax", "setFax", 1);
            digester.addCallParam("persons/person/fax", 0);

            digester.addCallMethod("persons/person/role", "addRole", 1);
            digester.addCallParam("persons/person/role", 0);

            digester.addCallMethod("persons/person/group", "addGroup", 1);
            digester.addCallParam("persons/person/group", 0);

            final List<XMLPersonPrincipal> personList = (List<XMLPersonPrincipal>) digester.parse(file);
            for (final XMLPersonPrincipal personTmp : personList)  {
                this.allPersons.put(personTmp.getName(), personTmp);
            }
        } catch (final IOException e)  {
            XMLUserLoginModule.LOG.error("could not open file '" + _fileName + "'", e);
        } catch (final SAXException e)  {
            XMLUserLoginModule.LOG.error("could not read file '" + _fileName + "'", e);
        }
    }
}

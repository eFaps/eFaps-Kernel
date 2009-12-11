/*
 * Copyright 2003 - 2009 The eFaps Team
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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * This is class is used to handle the interface between JAAS and eFaps that
 * enables a User to change his Password <br>
 * To be able to change the Passwordx the name and password of the user method
 * {@link #checkLogin} could be used to test if the user is allowed to login.
 * The method returns then the related person to the given name and password (if
 * found) or <i>null</i> (if not found)
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SetPasswordHandler
{
    /**
     * The name of the application used to create a new login context. The
     * default value is <code>eFaps</code>.
     */
    private String application = "eFaps";

    /**
     * Constructor to initialize the setpassword handler. If <i>null</i> is
     * given to the application name, the default value defined in
     * {@link #application} is used.
     *
     * @param _application  application name of the JAAS configuration
     */
    public SetPasswordHandler(final String _application)
    {
        if (_application != null) {
            this.application = _application;
        }
    }

    public boolean setPassword(final String _name,
                               final String _newpasswd,
                               final String _oldpasswd)
        throws LoginException
    {
        boolean ret = false;

        final LoginContext login = new LoginContext(
                this.application,
                new SetPasswordCallbackHandler(ActionCallback.Mode.SET_PASSWORD, _name, _newpasswd, _oldpasswd));
        login.login();
        ret = true;
        return ret;
    }

    protected class SetPasswordCallbackHandler
        implements CallbackHandler
    {
        /**
         * The user name to test is stored in this instance variable.
         */
        private final String name;

        /**
         * The new password used from the user is stored in this instance variable.
         */
        private final String newpassword;

        /**
         * The action mode for which the login must be made is stored in this
         * instance variable (e.g. login, information about all persons, etc.)
         */
        private final ActionCallback.Mode mode;

        /**
         * The old password used from the user is stored in this instance variable.
         */
        private final String oldpassword;

        /**
         * Constructor initializing the action, name and password in this call back
         * handler.
         *
         * @param _mode         defines mode for which the login is made
         * @param _name         name of the login user
         * @param _newpasswd    new password for the user
         * @param _oldpasswd    old password
         *                password of the login user
         * @see #action
         * @see #name
         * @see #newpassword
         */
        protected SetPasswordCallbackHandler(final ActionCallback.Mode _mode,
                                             final String _name,
                                             final String _newpasswd,
                                             final String _oldpasswd) {
            this.mode = _mode;
            this.name = _name;
            this.oldpassword = _oldpasswd;
            this.newpassword = _newpasswd;
        }

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
                    final PasswordCallback pc = (PasswordCallback) _callbacks[i];
                    if ("Password".equals(pc.getPrompt()) && this.oldpassword != null) {
                        pc.setPassword(this.oldpassword.toCharArray());
                    }
                    if ("newPassword".equals(pc.getPrompt()) && this.newpassword != null) {
                        pc.setPassword(this.newpassword.toCharArray());
                    }
                } else if (_callbacks[i] instanceof TextOutputCallback) {
// do nothing, TextOutputCallBack's are ignored!
                } else {
                    throw new UnsupportedCallbackException(_callbacks[i], "Unrecognized Callback");
                }
            }
        }
    }
}

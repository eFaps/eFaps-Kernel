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

package org.efaps.jaas;

import java.util.Collections;
import java.util.Set;

import org.efaps.util.EFapsException;

/**
 * Class contains the information about the access rigths for
 * one instance of the application. The Handler defines two different
 * modi.<br/>
 * <ul>
 *  <li>If the loginRoles contain names than only the access for this roles
 * are accepted. If for example a command does not have an access
 * definition it must not be accessible.</li>
 *  <li>If the loginRoles is empty than access of all roles inside eFaps
 * are accepted. If for example a command does not have an access
 * definition it is accessible.</li>
 * </ul>
 *
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class AppAccessHandler
{
    /**
     * Handler for the current application.
     */
    private static AppAccessHandler HANDLER;

    /**
     * Default key of the related Application if not set explicitely.
     */
    private final static String KEYDEFAULT = "eFaps";

    /**
     * Immutable Set of login roles that is allowed for the application instance
     * related to this filter.
     */
    private final Set<String> loginRoles;

    /**
     * Key of the Aplication this AccessHandler belongs to.
     */
    private final String appKey;

    /**
     * Private Constructor to make a Singleton.
     * @param _key of the application
     * @param _loginRoles allowed login roles
     */
    private AppAccessHandler(final String _appKey,
                             final Set<String> _loginRoles)
    {
        this.appKey = _appKey == null ? AppAccessHandler.KEYDEFAULT : _appKey;
        this.loginRoles = Collections.unmodifiableSet(_loginRoles);
    }

    /**
     * @return Hanlder for currecnt application instance
     * @throws EFapsException if not initialized
     */
    public static AppAccessHandler getAccessHandler()
        throws EFapsException
    {
        if (!AppAccessHandler.initialized()) {
            throw new EFapsException(AppAccessHandler.class, "not initialized", "");
        }
        return AppAccessHandler.HANDLER;
    }

    /**
     * Init the Handler. Can only be executed once.
     * @param _key of the application
     * @param _loginRoles allowd Login roles
     */
    public static void init(final String _appKey,
                            final Set<String> _loginRoles)
    {
        if (AppAccessHandler.HANDLER == null) {
            AppAccessHandler.HANDLER = new AppAccessHandler(_appKey, _loginRoles);
        }
    }

    /**
     * Is the Handler initialized.
     *
     * @return true if allready the Handler is set
     */
    public static boolean initialized()
    {
        return AppAccessHandler.HANDLER != null;
    }

    /**
     *
     * @return allowed login roles
     * @throws EFapsException if not initialized
     */
    public static Set<String> getLoginRoles()
        throws EFapsException
    {
        if (!AppAccessHandler.initialized()) {
            throw new EFapsException(AppAccessHandler.class, "not initialized", "");
        }
        return AppAccessHandler.HANDLER.loginRoles;
    }

    /**
     * @return true if include mode
     * @throws EFapsException if not initialized
     */
    public static boolean excludeMode()
        throws EFapsException
    {
        if (!AppAccessHandler.initialized()) {
            throw new EFapsException(AppAccessHandler.class, "not initialized", "");
        }
        return !AppAccessHandler.HANDLER.loginRoles.isEmpty();
    }

    /**
     * @return key of the related application
     * @throws EFapsException if not initialized
     */
    public static String getApplicationKey()
        throws EFapsException
    {
        if (!AppAccessHandler.initialized()) {
            throw new EFapsException(AppAccessHandler.class, "not initialized", "");
        }
        return AppAccessHandler.HANDLER.appKey;
    }

}

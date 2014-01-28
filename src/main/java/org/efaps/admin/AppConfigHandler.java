/*
 * Copyright 2003 - 2014 The eFaps Team
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


package org.efaps.admin;

import java.util.Map;

/**
 * Class used to set the configuration of an single instance of eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class AppConfigHandler
{
    /**
     * Enum used to have a String representation of the config key,
     * that can be used by an webapp initialization parameter.
     */
    public enum Parameter
    {
        /**
         * Deactivate the AccessCache.
         */
        ACCESSCACHE_DEACTIVATE("org.efaps.application.config.AccessCache.deactivate");

        /**
         * Key of the enum instance.
         */
        private String key;

        /**
         * @param _key key of the enum instance.
         */
        private Parameter(final String _key)
        {
            this.key = _key;
        }

        /**
         * Getter method for the instance variable {@link #key}.
         *
         * @return value of instance variable {@link #key}
         */
        public String getKey()
        {
            return this.key;
        }
    }

    /**
     * Singelton instance.
     */
    private static AppConfigHandler HANDLER;

    /**
     * Possibility to deactivate the AccessCache.
     */
    private final boolean accessCacheDeactivated;

    /**
     * Singleton Constructor.
     * @param _values values for the init
     */
    private AppConfigHandler(final Map<String, String> _values)
    {
        this.accessCacheDeactivated =  "true".equalsIgnoreCase(_values.get(Parameter.ACCESSCACHE_DEACTIVATE.getKey()));
    }

    /**
     * Init the Handler. Can only be executed once.
     * @param _values values for the init
     */
    public static void init(final Map<String, String> _values)
    {
        if (AppConfigHandler.HANDLER == null) {
            AppConfigHandler.HANDLER = new AppConfigHandler(_values);
        }
    }


    /**
     * Is the Handler initialized.
     *
     * @return true if already the Handler is set
     */
    public static boolean initialized()
    {
        return AppConfigHandler.HANDLER != null;
    }


    public static AppConfigHandler get() {
        return AppConfigHandler.HANDLER;
    }

    /**
     * Getter method for the instance variable {@link #accessCacheDeactivated}.
     *
     * @return value of instance variable {@link #accessCacheDeactivated}
     */
    public boolean isAccessCacheDeactivated()
    {
        return this.accessCacheDeactivated;
    }
}

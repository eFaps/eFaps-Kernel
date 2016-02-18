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


package org.efaps.admin;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * Class used to set the configuration of an single instance of eFaps.
 * It can only be instantiated once. If it was not instantiated before the
 * first access a default instance of <code>AppConfigHandler</code> will be
 * created.
 *
 * @author The eFaps Team
 *
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
        ACCESSCACHE_DEACTIVATE("org.efaps.application.config.AccessCache.deactivate"),

        /**
         * Deactivate the AccessCache.
         */
        QUERYCACHE_DEACTIVATE("org.efaps.application.config.QueryCache.deactivate"),

        /**
         * The id of this system as needed by the GeneralInstance mechanism.
         */
        SYSTEMID("org.efaps.application.config.SystemID"),

        /**
         * The id of this system as needed by the GeneralInstance mechanism.
         */
        TEMPFOLDER("org.efaps.application.config.TempFolder");

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
     * Singleton instance.
     */
    private static AppConfigHandler HANDLER;

    /**
     * Possibility to deactivate the AccessCache.
     */
    private final boolean accessCacheDeactivated;

    /**
     * Possibility to deactivate the AccessCache.
     */
    private final boolean queryCacheDeactivated;

    /**
     *  The id of this system as needed by the GeneralInstance mechanism.
     */
    private final int systemID;

    /**
     * URI to the temp folder used by eFaps.
     */
    private final URI tmpURI;

    /**
     * Singleton Constructor.
     * @param _values values for the init
     */
    private AppConfigHandler(final Map<String, String> _values)
    {
        this.accessCacheDeactivated =  "true".equalsIgnoreCase(_values.get(Parameter.ACCESSCACHE_DEACTIVATE.getKey()));
        this.queryCacheDeactivated =  "true".equalsIgnoreCase(_values.get(Parameter.QUERYCACHE_DEACTIVATE.getKey()));
        if (_values.containsKey(Parameter.SYSTEMID.getKey())) {
            this.systemID = Integer.parseInt(_values.get(Parameter.SYSTEMID.getKey()));
        } else {
            this.systemID = 0;
        }
        if (_values.containsKey(Parameter.TEMPFOLDER.getKey())) {
            this.tmpURI = URI.create(_values.get(Parameter.TEMPFOLDER.getKey()));
        } else {
            this.tmpURI = null;
        }
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

    /**
     * Getter method for the instance variable {@link #queryCacheDeactivated}.
     *
     * @return value of instance variable {@link #queryCacheDeactivated}
     */
    public boolean isQueryCacheDeactivated()
    {
        return this.queryCacheDeactivated;
    }

    /**
     * Getter method for the instance variable {@link #systemID}.
     *
     * @return value of instance variable {@link #systemID}
     */
    public int getSystemID()
    {
        return this.systemID;
    }

    /**
     * @return if defined the tempfolder for eFaps, else null
     */
    public File getTempFolder()
    {
        File ret;
        if (this.tmpURI == null) {
            ret = null;
        } else {
            ret = new File(this.tmpURI);
        }
        return ret;
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

    /**
     * @return the instance of the AppConfigHandler
     */
    public static AppConfigHandler get()
    {
        if (!AppConfigHandler.initialized()) {
            AppConfigHandler.init(Collections.<String, String>emptyMap());
        }
        return AppConfigHandler.HANDLER;
    }
}

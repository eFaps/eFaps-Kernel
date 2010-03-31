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

package org.efaps.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The request handler converts URL with macros to URL without macros and
 * correct application paths.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class RequestHandler
{
    /**
     * URL to the image servlet.
     */
    public static final String URL_IMAGE = "${ROOTURL}/servlet/image/";

    /**
     * URL to the static content servlet (e.g. cascade style sheets, Javascript
     * etc.)
     */
    public static final String URL_STATIC = "${ROOTURL}/servlet/static/";

    /**
     * The static map stores all replaceable URL macros.
     */
    private static Map<String, String> REPLACEABLE_MACROS = null;

    /**
     * Private constructor defined so that this utility class could not be
     * instantiated.
     */
    private RequestHandler()
    {
    }

    /**
     * The static method replaces all known URL macros by real URL's.
     *
     * @param _url  URL with URL macros
     * @return URL string with replaces URL macros
     * @see #REPLACEABLE_MACROS
     * @see #initReplacableMacros(String)
     */
    public static String replaceMacrosInUrl(final String _url)
    {
        String url = _url;
        if (RequestHandler.REPLACEABLE_MACROS != null) {
            for (final Map.Entry<String, String> entry : RequestHandler.REPLACEABLE_MACROS.entrySet()) {
                url = url.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        if (url.indexOf('?') < 0) {
            url += "?";
        }
        url = url.replaceAll("//", "/");
        return url;
    }

    /**
     * Stores the key and the value of the replaceable URL macros in the map
     * {@link #REPLACEABLE_MACROS}.
     *
     * @param _rootUrl  root URL of the application used to replace
     * @see #replaceMacrosInUrl(String)
     * @see #REPLACEABLE_MACROS
     */
    public static void initReplacableMacros(final String _rootUrl)
    {
        RequestHandler.REPLACEABLE_MACROS = new HashMap<String, String>();
        RequestHandler.REPLACEABLE_MACROS.put("\\$\\{SERVLETURL\\}", _rootUrl + "request");
        RequestHandler.REPLACEABLE_MACROS.put("\\$\\{COMMONURL\\}", _rootUrl + "common");
        RequestHandler.REPLACEABLE_MACROS.put("\\$\\{ROOTURL\\}", _rootUrl);
        RequestHandler.REPLACEABLE_MACROS.put("\\$\\{ICONURL\\}", _rootUrl + "images");
    }
}

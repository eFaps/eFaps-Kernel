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

package org.efaps.util;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class RequestHandler {

  /**
   * The static map stores all replacable url macros.
   */
  private static Map<String, String> REPLACEABLE_MACROS = null;

  public static final String URL_IMAGE = "${ROOTURL}/servlet/image/";

  public static final String URL_STATIC = "${ROOTURL}/servlet/static/";

  /**
   * The static method replaces all known url macros by real urls.
   *
   * @param _url
   *                url with url macros
   * @return url string with replaces url macros
   * @see #REPLACEABLE_MACROS
   * @see #initReplacableMacros
   */
  public static String replaceMacrosInUrl(String _url) {
    String url = _url;
    if (REPLACEABLE_MACROS != null) {
      for (final Map.Entry<String, String> entry : REPLACEABLE_MACROS
          .entrySet()) {
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
   * Stores the key and the value of the replaceable url macros in the map
   * {@link #REPLACEABLE_MACROS}
   *
   * @param _rootUrl
   *                root url of the application used to replace
   * @see #replaceMacrosInUrl
   * @see #REPLACEABLE_MACROS
   */
  public static void initReplacableMacros(String _rootUrl) {
    REPLACEABLE_MACROS = new HashMap<String, String>();
    REPLACEABLE_MACROS.put("\\$\\{SERVLETURL\\}", _rootUrl + "request");
    REPLACEABLE_MACROS.put("\\$\\{COMMONURL\\}", _rootUrl + "common");
    REPLACEABLE_MACROS.put("\\$\\{ROOTURL\\}", _rootUrl);
    REPLACEABLE_MACROS.put("\\$\\{ICONURL\\}", _rootUrl + "images");
  }

}

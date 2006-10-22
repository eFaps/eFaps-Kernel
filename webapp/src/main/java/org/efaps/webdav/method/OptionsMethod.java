/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.webdav.method;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class OptionsMethod extends AbstractMethod  {

  /**
   *
   */
  public void run(final HttpServletRequest _request, final HttpServletResponse _response) throws IOException, ServletException  {

//    resp.addHeader("DAV", "1,2");
    _response.addHeader("DAV", "1");

// CLass 1: COPY, DELETE, GET, HEAD, MKCOL, MOVE, OPTIONS, POST, PROPPATCH, PROPFIND, PUT
// Class 2: COPY, DELETE, GET, HEAD, LOCK, MKCOL, MOVE, OPTIONS, POST, PROPPATCH, PROPFIND, PUT, UNLOCK
// TRACE?
    final String methodsAllowed = "COPY, DELETE, GET, HEAD, MKCOL, MOVE, OPTIONS, POST, PROPPATCH, PROPFIND, PUT";

//        if (!(object instanceof DirContext)) {
//            methodsAllowed.append(", PUT");

    _response.addHeader("Allow", methodsAllowed);
    _response.addHeader("MS-Author-Via", "DAV");
  }
}

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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.efaps.webdav.WebDAVRequest;
import org.efaps.webdav.resource.AbstractResource;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class HeadMethod extends AbstractMethod  {

  /**
   *
   */
  public void run(final WebDAVRequest _request, 
                  final HttpServletResponse _response) throws IOException, ServletException  {

    AbstractResource resource = getResource4Path(_request.getPathInfo());
    if (resource == null)  {
      _response.setStatus(Status.NOT_FOUND.code);
    } else  {
      _response.setContentType(_request.getRequest().getSession().getServletContext().getMimeType(resource.getName()));
      _response.addHeader("Content-Disposition", "inline; filename=\""+resource.getName()+"\"");
      _response.setStatus(Status.NO_CONTENT.code);
    }
  }
}

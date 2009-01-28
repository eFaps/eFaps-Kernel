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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.webdav.method;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.efaps.ui.webdav.WebDAVRequest;
import org.efaps.ui.webdav.resource.AbstractResource;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class DeleteMethod extends AbstractMethod  {

  /**
   *
   */
  @Override
  public void run(final WebDAVRequest _request,
                  final HttpServletResponse _response) throws IOException, ServletException  {

    Status status = null;

    final AbstractResource resource = getResource4Path(_request.getPathInfo());
    if (resource != null)  {
      resource.delete();
// nicht richtig for collections....????
      status = Status.NO_CONTENT;
    } else  {
      status = Status.NOT_FOUND;
    }

    _response.setStatus(status.code);

  }
}
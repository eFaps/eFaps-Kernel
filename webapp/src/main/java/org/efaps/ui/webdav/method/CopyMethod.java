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

package org.efaps.ui.webdav.method;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.efaps.ui.webdav.WebDAVRequest;
import org.efaps.ui.webdav.resource.AbstractResource;
import org.efaps.ui.webdav.resource.CollectionResource;

/**
 *
 * @see RFC 2518 - 8.8 COPY Method
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class CopyMethod extends AbstractMethod  {

  /**
   * @todo depth header for 0 must be implemented
   */
  public void run(final WebDAVRequest _request,
                  final HttpServletResponse _response) throws IOException, ServletException  {

    Status status = null;

    String destination = _request.getDestination();
    boolean overwrite = _request.isOverwrite();

    String[] destUri = destination.split("/");
    String newName = destUri[destUri.length - 1];

    CollectionResource newParentCol = getCollection4ParentPath(destination);

    AbstractResource newRes = newParentCol.get(newName);

    if (newParentCol == null)  {
      // new parent collection does not exists
      status = Status.CONFLICT;
    } else if (!overwrite && (newRes != null))  {
      // source with given name already existing
      status = Status.PRECONDITION_FAILED;
    } else  {
      AbstractResource resource = getResource4Path(_request.getPathInfo());

      // if on the target place a resource exists
      // => delete (because overwrite!)
      if (newRes != null)  {
        newRes.delete();
      }

      // TODO: test, if the same webdav implementation is used!!
      if (resource.copy(newParentCol, newName))  {
        // new collection source created
        if (newRes != null)  {
          status = Status.NO_CONTENT;
        } else  {
          status = Status.CREATED;
        }
      } else  {
        // new collection source not creatable
        status = Status.FORBIDDEN;
      }
    }

    _response.setStatus(status.code);
  }
}

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
import org.efaps.ui.webdav.resource.CollectionResource;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class MoveMethod extends AbstractMethod  {

  /**
   *
   */
  @Override
  public void run(final WebDAVRequest _request,
                  final HttpServletResponse _response) throws IOException, ServletException  {

    Status status = null;

    final String destination = _request.getDestination();
    final boolean overwrite = _request.isOverwrite();

    final String[] destUri = destination.split("/");
    final String newName = destUri[destUri.length - 1];

    final CollectionResource newParentCol = getCollection4ParentPath(destination);

    final AbstractResource newRes = newParentCol.get(newName);

    if (newParentCol == null)  {
      // new parent collection does not exists
      status = Status.CONFLICT;
    } else if (!overwrite && (newRes != null))  {
      // source with given name already existing
      status = Status.PRECONDITION_FAILED;
    } else  {
      final AbstractResource resource = getResource4Path(_request.getPathInfo());

      // if on the target place a resource exists
      // => delete (because overwrite!)
      if (newRes != null)  {
        newRes.delete();
      }

      // TODO: test, if the same webdav implementation is used!!
      if (resource.move(newParentCol, newName))  {
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

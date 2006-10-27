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

import org.efaps.webdav.resource.CollectionResource;

/**
 * The class implements WebDAV method <i>MKCOL</i> (a new collection is
 * created).
 *
 * @author tmo
 * @version $Id$
 */
public class MkColMethod extends AbstractMethod  {

  /**
   *
   */
  public void run(final HttpServletRequest _request, final HttpServletResponse _response) throws IOException, ServletException  {
    Status status = Status.CONFLICT;
      
    String[] uri = _request.getPathInfo().split("/");
    String newName = uri[uri.length - 1];
    
    CollectionResource parentCollection 
                          = getCollection4ParentPath(_request.getPathInfo());
    if (parentCollection != null)  {
      if ((parentCollection.getCollection(newName) != null)
          || (parentCollection.getSource(newName) != null))  {
        status = Status.METHOD_NOT_ALLOWED;
      } else  {
        if (parentCollection.createCollection(newName))  {
          status = Status.CREATED;
        }
      }
    }

    _response.setStatus(status.code);
  }
}
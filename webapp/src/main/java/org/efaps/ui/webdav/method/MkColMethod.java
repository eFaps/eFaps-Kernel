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
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.efaps.ui.webdav.WebDAVRequest;
import org.efaps.ui.webdav.resource.CollectionResource;

/**
 * The class implements WebDAV method <i>MKCOL</i> (a new collection is
 * created).
 *
 * @author tmo
 * @version $Id$
 */
public class MkColMethod extends AbstractMethod  {

  /**
   * @todo the request body must be processed (currently the status is
   *                                           NOT_IMPLEMENTED)
   */
  @Override
  public void run(final WebDAVRequest _request,
                  final HttpServletResponse _response) throws IOException, ServletException  {
    Status status = null;

    final String[] uri = _request.getPathInfo().split("/");
    final String newName = uri[uri.length - 1];

    final CollectionResource parentCollection
                          = getCollection4ParentPath(_request.getPathInfo());

    if (parentCollection == null)  {
      status = Status.CONFLICT;
    } else  {
      if (_request.isInputAvailable()) {
        try {
          final Document document = _request.getDocument();
          // TODO : Process this request body
          status = Status.NOT_IMPLEMENTED;
        } catch(final IOException e)  {
          // input
          status = Status.BAD_REQUEST;
        } catch(final ParserConfigurationException e)  {
          // Parse error - assume invalid content
          status = Status.BAD_REQUEST;
        } catch(final SAXException e) {
          // Parse error - assume invalid content
          status = Status.BAD_REQUEST;
        }
      } else if ((parentCollection.getCollection(newName) != null)
          || (parentCollection.getSource(newName) != null))  {
        // source with given name already existing
        status = Status.METHOD_NOT_ALLOWED;
      } else  {
        if (parentCollection.createCollection(newName))  {
          // new collection source created
          status = Status.CREATED;
        } else  {
          // new collection source not creatable
          status = Status.FORBIDDEN;
        }
      }
    }

    _response.setStatus(status.code);
  }
}
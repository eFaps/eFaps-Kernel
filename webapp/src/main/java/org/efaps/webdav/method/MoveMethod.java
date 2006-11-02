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

import org.efaps.webdav.resource.AbstractResource;
import org.efaps.webdav.resource.CollectionResource;
import org.efaps.webdav.resource.SourceResource;

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
  public void run(final HttpServletRequest _request, 
                  final HttpServletResponse _response) throws IOException, ServletException  {

    Status status = null;

    String destination = decodeURL(_request, 
                                   _request.getHeader("Destination"));
    boolean overwrite = isOverwrite(_request);

    String[] destUri = destination.split("/");
    String newName = destUri[destUri.length - 1];

    CollectionResource newParentCol = getCollection4ParentPath(destination);

    if (newParentCol == null)  {
      // new parent collection does not exists
      status = Status.CONFLICT;
    } else if (!overwrite 
               && ((newParentCol.getCollection(newName) != null)
                    || (newParentCol.getSource(newName) != null)))  {
      // source with given name already existing
      status = Status.METHOD_NOT_ALLOWED;
    } else  {
      AbstractResource resource = getResource4Path(_request.getPathInfo());
// TODO: test, if the same webdav implementation is used!!
      if (resource.move(newParentCol, newName))  {
        // new collection source created
        status = Status.CREATED;
      } else  {
        // new collection source not creatable
        status = Status.FORBIDDEN;
      }
    }

    _response.setStatus(status.code);
  }

  /**
   * The request is checked, if the header <code>Overwrite</code> is specified.
   * If the value is set to <code>T</code> or the header is not specified, the
   * return value is <i>true</i>.<br/>
   * This is specified in "RFC2518 - 9.6 Overwrite Header"
   *
   * @param _request  http servler request
   * @return true if the request header <code>Overwrite</code> is set to 
   *         <code>T</code> or not defined.
   */
  protected boolean isOverwrite(final HttpServletRequest _request)  {
    boolean overwrite = true;
    
    String overwriteHeader = _request.getHeader("Overwrite");
    if (overwriteHeader != null) {
      if (overwriteHeader.equalsIgnoreCase("T")) {
        overwrite = true;
      } else {
        overwrite = false;
      }
    }
    return overwrite;
  }
                  
  /**
   * Return a context-relative path, beginning with a "/", that represents
   * the canonical version of the specified path after ".." and "." elements
   * are resolved out.  If the specified path attempts to go outside the
   * boundaries of the current context (i.e. too many ".." path elements
   * are present), return <code>null</code> instead.
   *
   * @param path the path to be normalized
   */
  private String decodeURL(final HttpServletRequest _request, 
                           final String path) {

    if (path == null)  {
      return null;
    }

    // Resolve encoded characters in the normalized path,
    // which also handles encoded spaces so we can skip that later.
    // Placed at the beginning of the chain so that encoded
    // bad stuff(tm) can be caught by the later checks
    String normalized = null;
    try  {
      normalized = java.net.URLDecoder.decode(path, "UTF8");
    } catch (java.io.UnsupportedEncodingException e)  {
    }

    if (normalized == null)  {
      return (null);
    }
System.out.println("normalized1="+normalized);

    // remove protocol, schema etc.
    try  {
      java.net.URL url = new java.net.URL(normalized);
      normalized = url.getFile();
    } catch (Exception e)  {
e.printStackTrace();
    }

System.out.println("normalized2="+normalized);

      // remove servlet context name and servlet path
      String servlet = "/" 
                        + _request.getSession().getServletContext()
                                               .getServletContextName() 
                        + _request.getServletPath();
      if (normalized.startsWith(servlet))  {
        normalized = normalized.substring(servlet.length());
      }
System.out.println("normalized3="+normalized);

      // add leading slash if necessary
      if (!normalized.startsWith("/"))  {
        normalized = "/" + normalized;
      }

System.out.println("normalized7="+normalized);
      // Return the normalized path that we have completed
      return (normalized);
  }

}

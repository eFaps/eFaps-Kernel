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
import java.io.Writer;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.efaps.ui.webdav.WebDAVImpl;
import org.efaps.ui.webdav.WebDAVRequest;
import org.efaps.ui.webdav.resource.AbstractResource;
import org.efaps.ui.webdav.resource.CollectionResource;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractMethod  {


  public enum Status {

    /**
     * Status code (201) indicating the request succeeded and created a new
     * resource on the server.
     */
    CREATED(HttpServletResponse.SC_CREATED, "Created"),
    /**
     * Status code (204) indicating that the request succeeded but that there
     * was no new information to return.
     */
    NO_CONTENT(HttpServletResponse.SC_NO_CONTENT, "No Content"),
    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),
    /**
     *  Status code (403) indicating the server understood the request but
     * refused to fulfill it.
     */
    FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),
    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "Not Found"),
    /**
     * Status code (405) indicating that the method specified in the
     * Request-Line is not allowed for the resource identified by the
     * Request-URI.
     */
    CONFLICT(HttpServletResponse.SC_CONFLICT, "Conflict"),
    /**
     * Status code (405) indicating that the method specified in the
     * Request-Line is not allowed for the resource identified by the
     * Request-URI.
     */
    METHOD_NOT_ALLOWED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed"),
    /**
     * Status code (412) indicating that the precondition given in one or more
     * of the request-header fields evaluated to false when it was tested on
     * the server.
     */
    PRECONDITION_FAILED(HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed"),
    /**
     * Status code (501) indicating the HTTP server does not support the
     * functionality needed to fulfill the request.
     */
    NOT_IMPLEMENTED(HttpServletResponse.SC_NOT_IMPLEMENTED, "Not Implemented");

    /**
     * The variable stores the code number of the status flag.
     */
    public final int code;

    /**
     * The variable stores the text to the code of the status flag.
     */
    public final String text;

    private Status(final int _code, final String _text)  {
      this.code = _code;
      this.text = _text;
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  private final RootCollectionResource rootCollection
                                                = new RootCollectionResource();

  /////////////////////////////////////////////////////////////////////////////
  // instance methods


/*

   } else if (property.equals("getcontentlanguage")) {
      writeElement(_writer, null, null, "getcontentlanguage", NO_CONTENT);
   } else if (property.equals("getcontenttype")) {
      writeProperty(_writer, null, null, "getcontenttype", "");
  } else if (property.equals("getetag")) {
      writeProperty(_writer, null, null, "getetag", "");
  } else if (property.equals("source")) {
    writeProperty(_writer, null, null, "source", "");
  } else if (property.equals("supportedlock")) {
    String supportedLocks = "<lockentry>"
        + "<lockscope><exclusive/></lockscope>"
        + "<locktype><write/></locktype>"
        + "</lockentry>" + "<lockentry>"
        + "<lockscope><shared/></lockscope>"
        + "<locktype><write/></locktype>"
        + "</lockentry>";
    writeElement(_writer, null, null, "supportedlock", OPENING);
    writeText(_writer, supportedLocks);
    writeElement(_writer, null, null, "supportedlock", CLOSING);
  } else if (property.equals("lockdiscovery")) {
//      if (!generateLockDiscovery(path, generatedXML))
//          propertiesNotFound.addElement(property);
  } else {
//      propertiesNotFound.addElement(property);
  }
*/
  abstract public void run(final WebDAVRequest _request,
                           final HttpServletResponse _response) throws IOException, ServletException;

  /////////////////////////////////////////////////////////////////////////////


  protected AbstractResource getResource4Path(final String _uri)  {
    final String[] uri = _uri.toString().split("/");

    AbstractResource resource = this.rootCollection;
    if (uri.length > 1)  {
      final CollectionResource collection = getCollection(uri.length - 2, uri);
      if (collection != null)  {
        resource = collection.get(uri[uri.length - 1]);
      } else  {
        resource = null;
      }
    }
    return resource;
  }

  protected CollectionResource getCollection4ParentPath(final String _uri)  {
    final String[] uri = _uri.toString().split("/");

    return getCollection(uri.length - 2, uri);
  }

  protected CollectionResource getCollection(final int _endIndex,
                                             final String[] _uri)  {

    CollectionResource collection = this.rootCollection;
    for (int i = 1; (i <= _endIndex) && (collection != null); i++)  {
      collection = collection.getCollection(_uri[i]);
    }

    return collection;
  }

  /////////////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Opening tag.
   */
  public static final int OPENING = 0;


  /**
   * Closing tag.
   */
  public static final int CLOSING = 1;


  /**
   * Element with no content.
   */
  public static final int NO_CONTENT = 2;

  /**
   * Write text.
   *
   * @param text Text to append
   */
  public void writeText(final Writer _writer, final String text) throws IOException  {
//System.out.println(text);
    _writer.write(text);
  }

  /**
   * Write an element.
   *
   * @param namespace Namespace abbreviation
   * @param namespaceInfo Namespace info
   * @param name Element name
   * @param type Element type
   */
  public void writeElement(final Writer _writer, final String name, final int type) throws IOException  {
    switch (type) {
    case OPENING:
      writeText(_writer, "<" + name + ">");
      break;
    case CLOSING:
      writeText(_writer, "</" + name + ">\n");
      break;
    case NO_CONTENT:
    default:
      writeText(_writer, "<" + name + "/>");
      break;
    }
  }

  /**
   * Write XML Header.
   */
  public void writeXMLHeader(final Writer _writer) throws IOException  {
    _writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
  }

  /////////////////////////////////////////////////////////////////////////////

  private class RootCollectionResource extends CollectionResource  {
    RootCollectionResource()  {
      this(new WebDAVImpl(), new Date());
    };

    private RootCollectionResource(final WebDAVImpl _webDAVImpl,
                                   final Date _date)  {
      super(_webDAVImpl,
              _webDAVImpl,
              "",
              null,
              _date,
              _date,
              "eFaps WebDAV Integration"
      );
    }
  };

}

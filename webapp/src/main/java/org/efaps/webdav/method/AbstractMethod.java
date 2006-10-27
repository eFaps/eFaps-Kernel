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
import java.io.Writer;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.efaps.webdav.resource.AbstractResource;
import org.efaps.webdav.resource.CollectionResource;
import org.efaps.webdav.resource.SourceResource;
import org.efaps.webdav.WebDAVImpl;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractMethod  {

  public enum DepthHeader  {
    /** 
     * The method is to be applied only to the resource. 
     */
    depth0,
    /** 
     * The method is to be applied to the resource and its immediate children. 
     */
    depth1,
    /** 
     * The method is to be applied to the resource and all its progeny.
     */
    infity;
  }

  public enum Status {

    CONFLICT(HttpServletResponse.SC_CONFLICT, "Conflict"),
    CREATED(HttpServletResponse.SC_CREATED, "Created"),
    FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),
    NO_CONTENT(HttpServletResponse.SC_NO_CONTENT, "No Content"),
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "Not Found"),
    METHOD_NOT_ALLOWED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed");


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

  public DepthHeader getDepthHeader(final HttpServletRequest _request)  {
    DepthHeader ret = DepthHeader.infity;
    
    String depthStr = _request.getHeader("Depth");
    if (depthStr != null)  {
      depthStr = depthStr.trim();
      if ("0".equals(depthStr))  {
        ret = DepthHeader.depth0;
      } else if ("1".equals(depthStr))  {
        ret = DepthHeader.depth1;
      }
    }
    return ret;
  }

/*

  if (property.equals("creationdate")) {
    writeProperty(_writer, null, null, "creationdate", creationDateFormat.format(_created));
  } else if (property.equals("displayname")) {
      writeElement(_writer, null, null, "displayname", OPENING);
      writeData(_writer, _resourceName);
      writeElement(_writer, null, null, "displayname", CLOSING);
  } else if (property.equals("getcontentlanguage")) {
      writeElement(_writer, null, null, "getcontentlanguage", NO_CONTENT);
  } else if (property.equals("getcontentlength")) {
      writeProperty(_writer, null, null, "getcontentlength", "0");
  } else if (property.equals("getcontenttype")) {
      writeProperty(_writer, null, null, "getcontenttype", "");
  } else if (property.equals("getetag")) {
      writeProperty(_writer, null, null, "getetag", "");
  } else if (property.equals("getlastmodified")) {
    writeProperty(_writer, null, null, "getlastmodified", modifedFormat.format(_modified));
  } else if (property.equals("resourcetype")) {
      writeProperty(_writer, null, null, "resourcetype", COLLECTION_TYPE);
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
  abstract public void run(final HttpServletRequest _request, HttpServletResponse _response) throws IOException, ServletException;

  /////////////////////////////////////////////////////////////////////////////

  
  protected AbstractResource getResource4Path(final String _uri)  {
    String[] uri = _uri.toString().split("/");
    
    AbstractResource resource = this.rootCollection;
    if (uri.length > 1)  {
      CollectionResource collection = getCollection(uri.length - 2, uri);
      if (collection != null)  {
        resource = collection.getCollection(uri[uri.length - 1]);
        if (resource == null)  {
          resource = collection.getSource(uri[uri.length - 1]);
        }
      } else  {
        resource = null;
      }
    }
    return resource;
  }

  protected CollectionResource getCollection4ParentPath(final String _uri)  {
    String[] uri = _uri.toString().split("/");
    
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

  /**
   * Return JAXP document builder instance.
   */
  protected DocumentBuilder getDocumentBuilder() throws ServletException {
    DocumentBuilder documentBuilder = null;
    DocumentBuilderFactory documentBuilderFactory = null;
    try {
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch(ParserConfigurationException e) {
throw new ServletException("webdavservlet.jaxpfailed");
//      throw new ServletException
//          (sm.getString("webdavservlet.jaxpfailed"));
    }
    return documentBuilder;
  }

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
  public void writeText(Writer _writer, String text) throws IOException  {
System.out.println(text);
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
  public void writeElement(Writer _writer, String name, int type) throws IOException  {
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

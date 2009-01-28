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

package org.efaps.ui.webdav;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class WebDAVRequest  {

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

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * @see #getRequest
   */
  private final HttpServletRequest request;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public WebDAVRequest(final HttpServletRequest _request)  {
    this.request = _request;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  public String getPathInfo()  {
    String ret = this.request.getPathInfo();

    if ((ret == null) || "".equals(ret))  {
      ret = "/";
    }
    return ret;
  }

  public String getRequestURI()  {
    return this.request.getRequestURI();
  }

  /**
   *
   *
   * @return input stream parsed as XML document
   * @throws IOException                  if input stream could not be read
   * @throws ParserConfigurationException if the parser could not be
   *                                      instanciated
   * @throws SAXException                 if input stream is not parseable
   * @see #getDocumentBuilder
   */
  public Document getDocument() throws IOException,
                                       ParserConfigurationException,
                                       SAXException  {
    final DocumentBuilder docBuilder = getDocumentBuilder();
    return docBuilder.parse(new InputSource(this.request.getInputStream()));
  }

  /**
   * Return JAXP document builder instance.
   */
  protected DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    return dbf.newDocumentBuilder();
  }

  /**
   * Checks if input is avaiable meaning a content is also avaiable for this
   * request.<br/>
   * If the input stream is not avaible (e.g. an exception is thrown),
   * <i>false</i> is returned.
   *
   * @return <i>true</i> if input is avaible, otherwise <i>false</i>
   */
  public boolean isInputAvailable()  {
    return this.request.getContentLength() > 0;
  }

  /**
   * The depth is evaluated from the header of this WebDAV request.<br/>
   * The default values is {@link #DepthHeader.infity}.<br/>
   * This is specified in "RFC 2518 - 9.2 Depth Header".
   *
   * @return depth header of this WebDAV request with three possible values
   *         defined in {@link #DepthHeader}
   * @see RFC 2518 - 9.2 Depth Header
   * @see #DepthHeader
   */
  public DepthHeader getDepthHeader()  {
    DepthHeader ret = DepthHeader.infity;

    String depthStr = this.request.getHeader("Depth");
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

  /**
   * Return a context-relative path, beginning with a "/", that represents
   * the canonical version of the specified path.
   * This is specified in "RFC 2518 - 9.3 Destination Header".
   *
   * @param path the path to be normalized
   * @see RFC 2518 - 9.3 Destination Header
   * @todo rework of exception handling
   */
  public String getDestination() {

    final String path = this.request.getHeader("Destination");

    if (path == null)  {
      return null;
    }

    // Resolve encoded characters in the normalized path,
    // which also handles encoded spaces so we can skip that later.
    // Placed at the beginning of the chain so that encoded
    // bad stuff(tm) can be caught by the later checks
    String normalized = null;
    try  {
      normalized = URLDecoder.decode(path, "UTF8");
    } catch (final UnsupportedEncodingException e)  {
    }

    if (normalized == null)  {
      return (null);
    }

    // remove protocol, schema etc.
    try  {
      final URL url = new URL(normalized);
      normalized = url.getFile();
    } catch (final MalformedURLException e)  {
    }

    // remove servlet context name and servlet path
    final String servlet = "/"
                      + this.request.getSession().getServletContext()
                                                 .getServletContextName()
                      + this.request.getServletPath();
    if (normalized.startsWith(servlet))  {
      normalized = normalized.substring(servlet.length());
    }

    // add leading slash if necessary
    if (!normalized.startsWith("/"))  {
      normalized = "/" + normalized;
    }

    // Return the normalized path that we have completed
    return (normalized);
  }

  /**
   * The request is checked, if the header <code>Overwrite</code> is specified.
   * If the value is set to <code>T</code> or the header is not specified, the
   * return value is <i>true</i>.<br/>
   * This is specified in "RFC 2518 - 9.6 Overwrite Header".
   *
   * @return true if the request header <code>Overwrite</code> is set to
   *         <code>T</code> or not defined.
   * @see RFC 2518 - 9.6 Overwrite Header
   */
  public boolean isOverwrite()  {
    boolean overwrite = true;

    final String overwriteHeader = this.request.getHeader("Overwrite");
    if (overwriteHeader != null) {
      if (overwriteHeader.equalsIgnoreCase("T")) {
        overwrite = true;
      } else {
        overwrite = false;
      }
    }
    return overwrite;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods

  /**
   * This is the getter method for instance variable {@link #request}.
   *
   * @return value of instance variable {@link #request}
   * @see #request
   */
  public HttpServletRequest getRequest()  {
    return this.request;
  }
}

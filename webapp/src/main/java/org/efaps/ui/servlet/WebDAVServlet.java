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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.ui.webdav.WebDAVRequest;
import org.efaps.ui.webdav.method.AbstractMethod;
import org.efaps.ui.webdav.method.CopyMethod;
import org.efaps.ui.webdav.method.DeleteMethod;
import org.efaps.ui.webdav.method.GetMethod;
import org.efaps.ui.webdav.method.HeadMethod;
import org.efaps.ui.webdav.method.MkColMethod;
import org.efaps.ui.webdav.method.MoveMethod;
import org.efaps.ui.webdav.method.OptionsMethod;
import org.efaps.ui.webdav.method.PropFindMethod;
import org.efaps.ui.webdav.method.PutMethod;

/**
 *
 *
 * @author tmo
 * @version $Id: WebDAVServlet.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb
 *          2007) jmox$
 */
public class WebDAVServlet extends HttpServlet {

  private static final long                  serialVersionUID = -589477313883705672L;

  // private static final String METHOD_HEAD = "HEAD";
  // private static final String METHOD_PROPPATCH = "PROPPATCH";
  // private static final String METHOD_LOCK = "LOCK";
  // private static final String METHOD_UNLOCK = "UNLOCK";

  private static Map<String, AbstractMethod> methods          = new HashMap<String, AbstractMethod>();

  static {
    methods.put("COPY", new CopyMethod());
    methods.put("DELETE", new DeleteMethod());
    methods.put("GET", new GetMethod());
    methods.put("HEAD", new HeadMethod());
    methods.put("MKCOL", new MkColMethod());
    methods.put("MOVE", new MoveMethod());
    methods.put("MKCOL", new MkColMethod());
    methods.put("OPTIONS", new OptionsMethod());
    methods.put("PROPFIND", new PropFindMethod());
    methods.put("PUT", new PutMethod());
  }

  /**
   * @param _config
   */
  @Override
  public void init(ServletConfig _config) throws ServletException {
    super.init(_config);
  }

  /**
   * Handles the special WebDAV methods.
   */
  @Override
  protected void service(final HttpServletRequest _request,
                         final HttpServletResponse _response)
                                                             throws ServletException,
                                                             IOException {

    System.out.println("getPath " + _request.getPathInfo());
    System.out.println("getMethod " + _request.getMethod());

    final AbstractMethod method = methods.get(_request.getMethod());
    if (method != null) {
      method.run(new WebDAVRequest(_request), _response);
    } else {
      System.out.println("method " + _request.getMethod() + " not implementet");
      super.service(_request, _response);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // MOVE

  /**
   * Return a context-relative path, beginning with a "/", that represents the
   * canonical version of the specified path after ".." and "." elements are
   * resolved out. If the specified path attempts to go outside the boundaries
   * of the current context (i.e. too many ".." path elements are present),
   * return <code>null</code> instead.
   * ///////////////////////////////////////////////////////////////////////////// //
   * MOVE
   *
   * /** Return a context-relative path, beginning with a "/", that represents
   * the canonical version of the specified path after ".." and "." elements are
   * resolved out. If the specified path attempts to go outside the boundaries
   * of the current context (i.e. too many ".." path elements are present),
   * return <code>null</code> instead.
   *
   * @param path
   *          the path to be normalized
   */
  /*
   * private String decodeURL(HttpServletRequest _request, String path) {
   *
   * if (path == null) return null;
   *  // Resolve encoded characters in the normalized path, // which also
   * handles encoded spaces so we can skip that later. // Placed at the
   * beginning of the chain so that encoded // bad stuff(tm) can be caught by
   * the later checks String normalized = null; try { normalized =
   * java.net.URLDecoder.decode(path, this.urlEncoding); } catch
   * (java.io.UnsupportedEncodingException e) { }
   *
   * if (normalized == null) return (null);
   *  // remove protocol, schema etc. int protocolIndex =
   * normalized.indexOf("://"); if (protocolIndex >= 0) { // if the Destination
   * URL contains the protocol, we can safely // trim everything upto the first
   * "/" character after "://" int firstSeparator = normalized.indexOf("/",
   * protocolIndex + 4); if (firstSeparator < 0) { normalized = "/"; } else {
   * normalized = normalized.substring(firstSeparator); } } else { String
   * hostName = _request.getServerName(); if ((hostName != null) &&
   * (normalized.startsWith(hostName))) { normalized =
   * normalized.substring(hostName.length()); }
   *
   * int portIndex = normalized.indexOf(":"); if (portIndex >= 0) { normalized =
   * normalized.substring(portIndex); }
   *
   * if (normalized.startsWith(":")) { int firstSeparator =
   * normalized.indexOf("/"); if (firstSeparator < 0) { normalized = "/"; } else {
   * normalized = normalized.substring(firstSeparator); } } }
   *  // remove servlet context name and servlet path String servlet = "/" +
   * getServletContext().getServletContextName() + _request.getServletPath(); if
   * (normalized.startsWith(servlet)) { normalized =
   * normalized.substring(servlet.length()); }
   *  // Normalize the slashes and add leading slash if necessary if
   * (normalized.indexOf('\\') >= 0) normalized = normalized.replace('\\', '/');
   * if (!normalized.startsWith("/")) normalized = "/" + normalized;
   *  // Resolve occurrences of "//" in the normalized path while (true) { int
   * index = normalized.indexOf("//"); if (index < 0) break; normalized =
   * normalized.substring(0, index) + normalized.substring(index + 1); }
   *  // Resolve occurrences of "/./" in the normalized path while (true) { int
   * index = normalized.indexOf("/./"); if (index < 0) break; normalized =
   * normalized.substring(0, index) + normalized.substring(index + 2); }
   *  // Resolve occurrences of "/../" in the normalized path while (true) { int
   * index = normalized.indexOf("/../"); if (index < 0) break; if (index == 0)
   * return (null); // Trying to go outside our context int index2 =
   * normalized.lastIndexOf('/', index - 1); normalized =
   * normalized.substring(0, index2) + normalized.substring(index + 3); }
   *  // Return the normalized path that we have completed return (normalized); }
   */
  /*
   * protected void doMove(HttpServletRequest _request, HttpServletResponse
   * _response) throws ServletException, IOException {
   *
   * try { Context context = Context.getThreadContext();
   *
   * Instance instance = getFolderInstance(context, _request.getPathInfo());
   *
   * String destination = decodeURL(_request,
   * _request.getHeader("destination"));
   *
   * String[] destUriArray = destination.split("/");
   *
   * Instance destParentInstance = getFolderInstance(context,
   * destUriArray.length - 2, destUriArray);
   * System.out.println("destParentInstance="+destParentInstance); if
   * (destParentInstance == null) { // fehler }
   *
   * Instance existsInstance = getSubFolderInstance(context, destParentInstance,
   * destUriArray[destUriArray.length - 1]); if (existsInstance != null) { //
   * fehler, existiert schon!!! }
   *
   * Update update = new Update(context, instance); update.add(context, "Name",
   * destUriArray[destUriArray.length - 1]); update.execute(); update.close();
   *  } catch (IOException e) { e.printStackTrace(); throw e; } catch
   * (ServletException e) { e.printStackTrace(); throw e; } catch (Throwable e) {
   * e.printStackTrace(); throw new ServletException(e); } }
   */
}

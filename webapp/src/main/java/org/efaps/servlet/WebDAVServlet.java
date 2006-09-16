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

package org.efaps.servlet;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.Update;
import org.efaps.webdav.method.AbstractMethod;
import org.efaps.webdav.method.DeleteMethod;
import org.efaps.webdav.method.MkColMethod;
import org.efaps.webdav.method.PropFindMethod;

/**
 *
 *
 * @author tmo
 * @version $Id$
 */
public class WebDAVServlet extends HttpServlet  {

//  private static final String METHOD_HEAD = "HEAD";
  private static final String METHOD_PROPFIND = "PROPFIND";
  private static final String METHOD_PROPPATCH = "PROPPATCH";
  private static final String METHOD_MKCOL = "MKCOL";
  private static final String METHOD_COPY = "COPY";
  private static final String METHOD_MOVE = "MOVE";
  private static final String METHOD_LOCK = "LOCK";
  private static final String METHOD_UNLOCK = "UNLOCK";

  /**
   * Simple date format for the creation date ISO representation (partial).
   */
  protected static final SimpleDateFormat creationDateFormat =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  /**
   * HTTP date format.
   */
  protected static final SimpleDateFormat modifedFormat =
      new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

  static {
      creationDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      modifedFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  static enum FindProperty {FIND_BY_PROPERTY, FIND_ALL_PROP, FIND_PROPERTY_NAMES};
  /**
   * FIND_BY_PROPERTY - Specify a property mask.
   * FIND_ALL_PROP - Display all properties.
   * FIND_PROPERTY_NAMES - Return property names.
   */

  /**
   * The URL encoding used by this webDAV servlet is stored
   */
  private String urlEncoding = null;


  private static Map<String,AbstractMethod> methods = new HashMap<String,AbstractMethod>();

  static  {
    methods.put("DELETE",   new DeleteMethod());
    methods.put("MKCOL",    new MkColMethod());
    methods.put("PROPFIND", new PropFindMethod());
  }


  /**
   * @param _config
   */
  public void init(ServletConfig _config) throws ServletException  {
    super.init(_config);

    String defaultEncoding = new java.io.InputStreamReader(System.in).getEncoding();
this.urlEncoding = defaultEncoding;
//    _urlEncoding = _default.getProperty(Property.UrlEncoding, defaultEncoding);

//org.efaps.webdav.method.AbstractMethod method = new org.efaps.webdav.method.AbstractMethod();
/*try {
System.out.println("=====================================0");
//method.test();
System.out.println("=====================================1");
} catch (Throwable e)  {
e.printStackTrace();
}
*/
}


  /**
   * Handles the special WebDAV methods.
   */
  protected void service(final HttpServletRequest _request,
      final HttpServletResponse _response) throws ServletException, IOException  {

    AbstractMethod method = methods.get(_request.getMethod());
    if (method != null)  {
      method.run(_request, _response);
    } else  {
System.out.println("method "+_request.getMethod()+" not implementet");
super.service(_request, _response);
    }

/*    if (method.equals(METHOD_PROPFIND)) {
      propFind.run(_req, _resp);
    } else if (method.equals(METHOD_PROPPATCH)) {
//        doProppatch(req, resp);
    } else if (method.equals(METHOD_MKCOL)) {
      doMkcol(_req, _resp);
    } else if (method.equals(METHOD_COPY)) {
//        doCopy(req, resp);
    } else if (method.equals(METHOD_MOVE)) {
      doMove(_req, _resp);
    } else if (method.equals(METHOD_LOCK)) {
//        doLock(req, resp);
    } else if (method.equals(METHOD_UNLOCK)) {
//        doUnlock(req, resp);
    } else {
        super.service(_req, _resp);
    }
*/
  }

  /**
   * OPTIONS Method.
   *
   * @param req The request
   * @param resp The response
   * @throws ServletException If an error occurs
   * @throws IOException If an IO error occurs
   */
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    resp.addHeader("DAV", "1,2");

    String methodsAllowed = "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPPATCH, COPY, MOVE, LOCK, UNLOCK, PROPFIND, PUT";

//        if (!(object instanceof DirContext)) {
//            methodsAllowed.append(", PUT");

    resp.addHeader("Allow", methodsAllowed);
    resp.addHeader("MS-Author-Via", "DAV");
  }

  /**
   * Process a GET request for the specified resource.
   *
   * @param _request  The http servlet request we are processing
   * @param _response The http servlet response we are creating
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet-specified error occurs
   */
  protected void doGet(HttpServletRequest _request,
                       HttpServletResponse _response)
      throws IOException, ServletException {

    try  {
      Context context = Context.getThreadContext();

      Instance instance = getFileInstance(context, _request.getPathInfo());
if (instance == null)  {
// throw Exception!!
}

      Checkout checkout = new Checkout(instance);
      checkout.preprocess();

      _response.setContentType(getServletContext().getMimeType(checkout.getFileName()));
      _response.addHeader("Content-Disposition", "inline; filename=\""+checkout.getFileName()+"\"");

      checkout.execute(_response.getOutputStream());

    } catch (IOException e)  {
      throw e;
    } catch (ServletException e)  {
      throw e;
    } catch (Throwable e)  {
      throw new ServletException(e);
    }

  }


  /////////////////////////////////////////////////////////////////////////////
  // MOVE

  /**
   * Return a context-relative path, beginning with a "/", that represents
   * the canonical version of the specified path after ".." and "." elements
   * are resolved out.  If the specified path attempts to go outside the
   * boundaries of the current context (i.e. too many ".." path elements
   * are present), return <code>null</code> instead.
   *
   * @param path the path to be normalized
   **/
  private String decodeURL(HttpServletRequest _request, String path) {

     if (path == null)
          return null;

      // Resolve encoded characters in the normalized path,
      // which also handles encoded spaces so we can skip that later.
      // Placed at the beginning of the chain so that encoded
      // bad stuff(tm) can be caught by the later checks
      String normalized = null;
      try  {
        normalized = java.net.URLDecoder.decode(path, this.urlEncoding);
      } catch (java.io.UnsupportedEncodingException e)  {
      }

      if (normalized == null)
          return (null);

      // remove protocol, schema etc.
      int protocolIndex = normalized.indexOf("://");
      if (protocolIndex >= 0) {
        // if the Destination URL contains the protocol, we can safely
        // trim everything upto the first "/" character after "://"
        int firstSeparator = normalized.indexOf("/", protocolIndex + 4);
        if (firstSeparator < 0) {
          normalized = "/";
        } else {
          normalized = normalized.substring(firstSeparator);
        }
      } else {
        String hostName = _request.getServerName();
        if ((hostName != null) && (normalized.startsWith(hostName))) {
          normalized = normalized.substring(hostName.length());
        }

        int portIndex = normalized.indexOf(":");
        if (portIndex >= 0) {
          normalized = normalized.substring(portIndex);
        }

        if (normalized.startsWith(":")) {
          int firstSeparator = normalized.indexOf("/");
          if (firstSeparator < 0) {
            normalized = "/";
          } else {
            normalized = normalized.substring(firstSeparator);
          }
        }
      }

      // remove servlet context name and servlet path
      String servlet = "/" + getServletContext().getServletContextName() + _request.getServletPath();
      if (normalized.startsWith(servlet))  {
        normalized = normalized.substring(servlet.length());
      }

      // Normalize the slashes and add leading slash if necessary
      if (normalized.indexOf('\\') >= 0)
          normalized = normalized.replace('\\', '/');
      if (!normalized.startsWith("/"))
          normalized = "/" + normalized;

      // Resolve occurrences of "//" in the normalized path
      while (true) {
          int index = normalized.indexOf("//");
          if (index < 0)
              break;
          normalized = normalized.substring(0, index) +
              normalized.substring(index + 1);
      }

      // Resolve occurrences of "/./" in the normalized path
      while (true) {
          int index = normalized.indexOf("/./");
          if (index < 0)
              break;
          normalized = normalized.substring(0, index) +
              normalized.substring(index + 2);
      }

      // Resolve occurrences of "/../" in the normalized path
      while (true) {
          int index = normalized.indexOf("/../");
          if (index < 0)
              break;
          if (index == 0)
              return (null);  // Trying to go outside our context
          int index2 = normalized.lastIndexOf('/', index - 1);
          normalized = normalized.substring(0, index2) +
              normalized.substring(index + 3);
      }

      // Return the normalized path that we have completed
      return (normalized);
  }

  protected void doMove(HttpServletRequest _request, HttpServletResponse _response) throws ServletException, IOException {

    try  {
      Context context = Context.getThreadContext();

      Instance instance = getFolderInstance(context, _request.getPathInfo());

      String destination = decodeURL(_request, _request.getHeader("destination"));

      String[] destUriArray = destination.split("/");

      Instance destParentInstance = getFolderInstance(context, destUriArray.length - 2, destUriArray);
System.out.println("destParentInstance="+destParentInstance);
      if (destParentInstance == null)  {
// fehler
      }

      Instance existsInstance = getSubFolderInstance(context, destParentInstance, destUriArray[destUriArray.length - 1]);
      if (existsInstance != null)  {
// fehler, existiert schon!!!
      }

      Update update = new Update(context, instance);
      update.add(context, "Name", destUriArray[destUriArray.length - 1]);
      update.execute();
      update.close();

    } catch (IOException e)  {
e.printStackTrace();
      throw e;
    } catch (ServletException e)  {
e.printStackTrace();
      throw e;
    } catch (Throwable e)  {
e.printStackTrace();
      throw new ServletException(e);
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // DELETE

private Instance getFileInstance(final Context _context, final String _uri) throws Exception  {
  Instance instance = null;

  String[] uri = _uri.toString().split("/");
  Instance folder =  getFolderInstance(_context, uri.length - 2, uri);
  org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
  query.setQueryTypes(_context, "TeamCenter_Document2Folder");
  query.addWhereExprEqValue(_context, "Folder", ""+folder.getId());
  query.addSelect(_context, "Document.FileName");
  query.addSelect(_context, "Document.OID");
  query.execute();
  while (query.next())  {
    String docName = query.get(_context, "Document.FileName").toString();
    if (uri[uri.length - 1].equals(docName))  {
      instance = new Instance(_context, query.get(_context,"Document.OID").toString());
      break;
    }
  }
  query.close();
  return instance;
}

private Instance getFolderInstance(final Context _context, final String _uri) throws Exception  {
  String[] uri = _uri.toString().split("/");
  return getFolderInstance(_context, uri.length - 1, uri);
}

private Instance getFolderInstance(final Context _context, final int _index, final String[] _uri) throws Exception  {
    Instance instance = null;

  if (_index < 1)  {
// throw exception1!
  } else if (_index == 1)  {

    org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
    query.setQueryTypes(_context, "TeamCenter_RootFolder");
    query.addWhereExprEqValue(_context, "Name", _uri[_index]);
    query.addSelect(_context, "OID");
    query.execute();
// TODO: was passiert wenn nicht gefunden?
    if (query.next())  {
      instance = new Instance(_context, query.get(_context,"OID").toString());
    }
    query.close();
  } else  {
    Instance parentInstance = getFolderInstance(_context, _index - 1, _uri);
    instance = getSubFolderInstance(_context, parentInstance, _uri[_index]);
  }
System.out.println("found instance="+instance);
  return instance;
}


private Instance getSubFolderInstance(final Context _context, final Instance _folderInstance, final String _name) throws Exception  {
  Instance instance = null;

  org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
  query.setQueryTypes(_context, "TeamCenter_Folder");
  query.addWhereExprEqValue(_context, "Name", _name);
  query.addWhereExprEqValue(_context, "ParentFolder", "" + _folderInstance.getId());
  query.addSelect(_context, "OID");
  query.execute();
// TODO: was passiert wenn nicht gefunden?
// nichts => gibt null zurueck
  if (query.next())  {
    instance = new Instance(_context, query.get(_context,"OID").toString());
  }
  query.close();

  return instance;
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
   * Write XML Header.
   */
  public void writeXMLHeader(final Writer _writer) throws IOException  {
    _writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
  }

  /**
   * Write text.
   *
   * @param text Text to append
   */
  public void writeText(Writer _writer, String text) throws IOException  {
    _writer.write(text);
  }

  /**
   * Write data.
   *
   * @param data Data to append
   */
  public void writeData(Writer _writer, String data) throws IOException  {
    _writer.write("<![CDATA[" + data + "]]>");
  }

  /**
   * Write property to the XML.
   *
   * @param namespace Namespace
   * @param namespaceInfo Namespace info
   * @param name Property name
   * @param value Property value
   */
  public void writeProperty(Writer _writer, String namespace, String namespaceInfo,
                            String name, String value) throws IOException  {

      writeElement(_writer, namespace, namespaceInfo, name, OPENING);
      writeText(_writer, value);
      writeElement(_writer, namespace, namespaceInfo, name, CLOSING);

  }

  /**
   * Write an element.
   *
   * @param namespace Namespace abbreviation
   * @param namespaceInfo Namespace info
   * @param name Element name
   * @param type Element type
   */
  public void writeElement(Writer _writer, String namespace, String namespaceInfo,
                           String name, int type) throws IOException  {

    if ((namespace != null) && (namespace.length() > 0)) {
      switch (type) {
      case OPENING:
        if (namespaceInfo != null) {
          _writer.write("<" + namespace + ":" + name + " xmlns:"
                        + namespace + "=\""
                        + namespaceInfo + "\">");
        } else {
          _writer.write("<" + namespace + ":" + name + ">");
        }
        break;
      case CLOSING:
        _writer.write("</" + namespace + ":" + name + ">\n");
        break;
      case NO_CONTENT:
      default:
        if (namespaceInfo != null) {
          _writer.write("<" + namespace + ":" + name + " xmlns:"
                        + namespace + "=\""
                        + namespaceInfo + "\"/>");
        } else {
          _writer.write("<" + namespace + ":" + name + "/>");
        }
        break;
      }
    } else {
      switch (type) {
      case OPENING:
        _writer.write("<" + name + ">");
        break;
      case CLOSING:
        _writer.write("</" + name + ">\n");
        break;
      case NO_CONTENT:
      default:
        _writer.write("<" + name + "/>");
        break;
      }
    }
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

  /**
   * Return the relative path associated with this servlet.
   *
   * @param request The servlet request we are processing
   */
  protected String getRelativePath(final HttpServletRequest _request) {

      // Are we being processed by a RequestDispatcher.include()?
/*      if (request.getAttribute(Globals.INCLUDE_REQUEST_URI_ATTR) != null) {
          String result = (String) request.getAttribute(
                                          Globals.INCLUDE_PATH_INFO_ATTR);
          if (result == null)
              result = (String) request.getAttribute(
                                          Globals.INCLUDE_SERVLET_PATH_ATTR);
          if ((result == null) || (result.equals("")))
              result = "/";
          return (result);
      }
*/
      // No, extract the desired path directly from the request
      String result = _request.getPathInfo();
      if (result == null) {
          result = _request.getServletPath();
      }
      if ((result == null) || (result.equals(""))) {
          result = "/";
      }
      return (result);

  }
}

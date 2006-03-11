/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.webdav.method;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.efaps.db.Context;
import org.efaps.db.Instance;

/**
 *
 */
public abstract class AbstractMethod  {

  /**
   * Simple date format for the creation date ISO representation (partial).
   */
  private static final SimpleDateFormat creationDateFormat =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  /**
   * HTTP date format used to format last modified dates.
   */
  private static final SimpleDateFormat modifiedDateFormat =
      new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);


  /**
   * The enum defines the DAV properties described in RFC2518 chapter 13.
   */
  public enum DAVProperty  {
    /**
     * Records the time and date the resource was created.
     */
    creationdate {
      String makeXML(Object _creationDate) {
        StringBuffer ret = new StringBuffer();
        ret.append('<').append(name()).append(">")
           .append(creationDateFormat.format(_creationDate))
           .append("</").append(name()).append(">");
        return ret.toString();
      }
    },

    /**
     * Provides a name for the resource that is suitable for presentation to a
     * user.
     */
    displayname {
      String makeXML(Object _name) {
        StringBuffer ret = new StringBuffer();
        ret.append('<').append(name()).append(">")
           .append("<![CDATA[").append(_name).append("]]>")
           .append("</").append(name()).append(">");
        return ret.toString();
      }
    },

    /**
     * Contains the Content-Language header returned by a GET without accept
     * headers
     */
    getcontentlanguage {
    },

    /**
     * Contains the Content-Length header returned by a GET without accept
     * headers.
     */
    getcontentlength {
    },

    /**
     * Contains the Content-Type header returned by a GET without accept
     * headers.
     */
    getcontenttype {
    },

    /**
     * Contains the ETag header returned by a GET without accept headers.
     */
    getetag {
    },

    /**
     * Contains the Last-Modified header returned by a GET method without
     * accept headers.
     */
    getlastmodified {
      String makeXML(Object _modifiedDate) {
        StringBuffer ret = new StringBuffer();
        ret.append('<').append(name()).append(">")
           .append(modifiedDateFormat.format(_modifiedDate))
           .append("</").append(name()).append(">");
        return ret.toString();
      }
    },

    /**
     * Describes the active locks on a resource.
     */
    lockdiscovery {
    },

    /**
     * Specifies the nature of the resource.
     */
    resourcetype {
    },

    /**
     * The destination of the source link identifies the resource that contains
     * the unprocessed source of the link's source.
     */
    source {
    },

    /**
     * To provide a listing of the lock capabilities supported by the resource.
     */
    supportedlock {
    };


    String makeXML(Object _text) {
      StringBuffer ret = new StringBuffer();
      ret.append('<').append(name()).append(">")
         .append(_text)
         .append("</").append(name()).append(">");
      return ret.toString();
    }
  }


  public enum Status {

    NO_CONTENT(HttpServletResponse.SC_NO_CONTENT, "No Content");

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

  protected Instance getFileInstance(final Context _context, final String _uri) throws Exception  {
    Instance instance = null;

    String[] uri = _uri.toString().split("/");
    Instance folder =  getFolderInstance(_context, uri.length - 2, uri);
    org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
    query.setQueryTypes(_context, "TeamCenter_Document2Folder");
    query.addWhereExprEqValue(_context, "Folder", ""+folder.getId());
    query.addSelect(_context, "Document.FileName");
    query.addSelect(_context, "Document.OID");
    query.execute(_context);
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

  protected Instance getFolderInstance(final Context _context, final String _uri) throws Exception  {
    String[] uri = _uri.toString().split("/");
    return getFolderInstance(_context, uri.length - 1, uri);
  }

  protected Instance getFolderInstance(final Context _context, final int _index, final String[] _uri) throws Exception  {
      Instance instance = null;

    if (_index < 1)  {
// throw exception1!
    } else if (_index == 1)  {

      org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
      query.setQueryTypes(_context, "TeamCenter_RootFolder");
      query.addWhereExprEqValue(_context, "Name", _uri[_index]);
      query.addSelect(_context, "OID");
      query.execute(_context);
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

  protected Instance getSubFolderInstance(final Context _context, final Instance _folderInstance, final String _name) throws Exception  {
    Instance instance = null;

    org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
    query.setQueryTypes(_context, "TeamCenter_Folder");
    query.addWhereExprEqValue(_context, "Name", _name);
    query.addWhereExprEqValue(_context, "ParentFolder", "" + _folderInstance.getId());
    query.addSelect(_context, "OID");
    query.execute(_context);
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

  /**
   * Write XML Header.
   */
  public void writeXMLHeader(final Writer _writer) throws IOException  {
    _writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
  }
}

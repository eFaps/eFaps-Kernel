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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.efaps.db.Context;
import org.efaps.db.Instance;

/**
 *
 */
public class PropFindMethod extends AbstractMethod  {


  /**
   * FIND_BY_PROPERTY - Specify a property mask.
   * FIND_ALL_PROP - Display all properties.
   * FIND_PROPERTY_NAMES - Return property names.
   */
  static enum FindProperty {FIND_BY_PROPERTY, FIND_ALL_PROP, FIND_PROPERTY_NAMES};



  public void run(final HttpServletRequest _request, HttpServletResponse _response) throws IOException, ServletException  {
try  {
Writer writer = _response.getWriter();

    FindProperty type = FindProperty.FIND_ALL_PROP;
    Node propNode = null;

String depthStr = _request.getHeader("Depth");
System.out.println("depthStr="+depthStr);


    DocumentBuilder documentBuilder = getDocumentBuilder();

    try {
      Document document = documentBuilder.parse(new InputSource(_request.getInputStream()));

      // Get the root element of the document
      Element rootElement = document.getDocumentElement();
      NodeList childList = rootElement.getChildNodes();

      for (int i=0; i < childList.getLength(); i++) {
        Node currentNode = childList.item(i);
        switch (currentNode.getNodeType()) {
        case Node.TEXT_NODE:
          break;
        case Node.ELEMENT_NODE:
          if (currentNode.getNodeName().endsWith("prop")) {
              type = FindProperty.FIND_BY_PROPERTY;
              propNode = currentNode;
          }
          if (currentNode.getNodeName().endsWith("propname")) {
              type = FindProperty.FIND_PROPERTY_NAMES;
          }
          if (currentNode.getNodeName().endsWith("allprop")) {
              type = FindProperty.FIND_ALL_PROP;
          }
          break;
        }
      }
    } catch(Exception e) {
        // Most likely there was no content : we use the defaults.
        // TODO : Enhance that !
    }

System.out.println("type="+type);

    // Properties which are to be displayed.
    DAVProperty[] properties = null;
    List<String>      unDefinedProperties = new ArrayList<String>();


    if (type == FindProperty.FIND_BY_PROPERTY) {
      List<DAVProperty> propertiesList = new ArrayList<DAVProperty>();
      NodeList childList = propNode.getChildNodes();

      for (int i=0; i < childList.getLength(); i++) {
        Node currentNode = childList.item(i);
        switch (currentNode.getNodeType()) {
        case Node.TEXT_NODE:
          break;
        case Node.ELEMENT_NODE:
          String nodeName = currentNode.getNodeName();
          String propertyName = null;
          if (nodeName.indexOf(':') != -1) {
            propertyName = nodeName.substring(nodeName.indexOf(':') + 1);
          } else {
            propertyName = nodeName;
          }
          try  {
            propertiesList.add(DAVProperty.valueOf(propertyName));
          } catch (IllegalArgumentException e)  {
            unDefinedProperties.add(propertyName);
          }
        }
      }

      properties = propertiesList.toArray(new DAVProperty[propertiesList.size()]);
    } else if (type == FindProperty.FIND_ALL_PROP)  {
      properties = DAVProperty.values();
    }
System.out.println("properties="+properties);

writeXMLHeader(writer);
writeElement(writer, "multistatus xmlns=\"DAV:\"", OPENING);

Context context = Context.getThreadContext();




if (_request.getPathInfo()==null || "/".equals(_request.getPathInfo()))  {
  org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
  query.setQueryTypes(context, "TeamCenter_RootFolder");
  query.addSelect(context, "Name");
  query.addSelect(context, "Modified");
  query.addSelect(context, "Created");
  query.execute(context);
  while (query.next())  {
    String resourceName = makeResourceName(_request.getRequestURI(), query.get(context, "Name").toString());
    Date modified = (Date) query.get(context, "Modified");
    Date created = (Date) query.get(context, "Created");
    writeOneFolder(writer, resourceName, properties, created, modified);
  }
  query.close();
} else  {
  Instance instance = getFolderInstance(context, _request.getPathInfo());
//  if (isFolder(instance))  {

    org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
    query.setExpand(context, instance, "TeamCenter_Folder\\ParentFolder");
    query.addSelect(context, "Name");
    query.addSelect(context, "Created");
    query.addSelect(context, "Modified");
    query.execute(context);
    while (query.next())  {
      String resourceName = makeResourceName(_request.getRequestURI(), query.get(context, "Name").toString());
      Date modified = (Date) query.get(context, "Modified");
      Date created = (Date) query.get(context, "Created");
      writeOneFolder(writer, resourceName, properties, created, modified);
    }
    query.close();


    query = new org.efaps.db.SearchQuery();
    query.setExpand(context, instance, "TeamCenter_Document2Folder\\Folder.Document");
    query.addSelect(context, "FileName");
    query.addSelect(context, "FileLength");
    query.addSelect(context, "Created");
    query.addSelect(context, "Modified");
    query.execute(context);
    while (query.next())  {
      String resourceName = makeResourceName(_request.getRequestURI(), query.get(context, "FileName").toString());
      Date modified   = (Date) query.get(context, "Modified");
      Date created    = (Date) query.get(context, "Created");
      Long fileLength = (Long) query.get(context, "FileLength");
      writeOneFile(writer, resourceName, properties, created, modified, fileLength);
    }
    query.close();
//  }
}


writeElement(writer, "multistatus", CLOSING);
} catch (Exception e)  {
  e.printStackTrace();
}



  }

  String makeResourceName(String _path, String _name)  {
    StringBuffer ret = new StringBuffer(_path.length() + _name.length() + 1);
    ret.append(_path);
    if (!_path.endsWith("/"))  {
      ret.append("/");
    }
    ret.append(_name);
    return ret.toString();
  }


/**
 * Collection type.
 */
public static final String COLLECTION_TYPE = "<collection/>";


  protected void writeOneFolder(final Writer _writer, final String _resourceName, final DAVProperty[] _properties, final Date _created, final Date _modified) throws IOException  {
    writeElement(_writer, "response", OPENING);

    writeElement(_writer, "href", OPENING);
    writeText(_writer, _resourceName);
    writeElement(_writer, "href", CLOSING);

    writeElement(_writer, "propstat", OPENING);

    writeElement(_writer, "prop", OPENING);

    for (DAVProperty property : _properties)  {
      if (property.equals(DAVProperty.creationdate))  {
        writeText(_writer, property.makeXML(_created));
      } else if (property.equals(DAVProperty.displayname))  {
        writeText(_writer, property.makeXML(_resourceName));
      } else if (property.equals(DAVProperty.getlastmodified))  {
        writeText(_writer, property.makeXML(_modified));
      } else if (property.equals(DAVProperty.resourcetype))  {
        writeText(_writer, property.makeXML(COLLECTION_TYPE));
      } else  {
        writeText(_writer, property.makeXML(""));
      }
    }

    writeElement(_writer, "prop", CLOSING);

    writeElement(_writer, "status", OPENING);
    writeText(_writer, "HTTP/1.1 " + HttpServletResponse.SC_OK + " OK");
    writeElement(_writer, "status", CLOSING);

    writeElement(_writer, "propstat", CLOSING);
    writeElement(_writer, "response", CLOSING);
  }

  protected void writeOneFile(final Writer _writer, final String _resourceName, final DAVProperty[] _properties, final Date _created, final Date _modified, final long _length) throws IOException  {
    writeElement(_writer, "response", OPENING);

    writeElement(_writer, "href", OPENING);
    writeText(_writer, _resourceName);
    writeElement(_writer, "href", CLOSING);

    writeElement(_writer, "propstat", OPENING);

    writeElement(_writer, "prop", OPENING);

    for (DAVProperty property : _properties)  {
      if (property.equals(DAVProperty.creationdate))  {
        writeText(_writer, property.makeXML(_created));
      } else if (property.equals(DAVProperty.displayname))  {
        writeText(_writer, property.makeXML(_resourceName));
        break;
      } else if (property.equals(DAVProperty.getcontentlength))  {
        writeText(_writer, property.makeXML(""+_length));
        break;
      } else if (property.equals(DAVProperty.getlastmodified))  {
        writeText(_writer, property.makeXML(_modified));
      } else  {
        writeText(_writer, property.makeXML(""));
      }
    }

/*
property.equals("supportedlock"
String supportedLocks = "<lockentry>"
        + "<lockscope><exclusive/></lockscope>"
        + "<locktype><write/></locktype>"
        + "</lockentry>" + "<lockentry>"
        + "<lockscope><shared/></lockscope>"
        + "<locktype><write/></locktype>"
        + "</lockentry>";
    writeElement(_writer, "supportedlock", OPENING);
    writeText(_writer, supportedLocks);
    writeElement(_writer, "supportedlock", CLOSING);
*/

    writeElement(_writer, "prop", CLOSING);

    writeElement(_writer, "status", OPENING);
    writeText(_writer, "HTTP/1.1 " + HttpServletResponse.SC_OK + " OK");
    writeElement(_writer, "status", CLOSING);

    writeElement(_writer, "propstat", CLOSING);
    writeElement(_writer, "response", CLOSING);
  }

}
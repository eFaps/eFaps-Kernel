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

package org.efaps.esjp.ui.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.ui.xml.XMLExport;
import org.efaps.util.EFapsException;



/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("04ace828-1692-46fb-8ed7-6e2c42122394")
@EFapsRevision("$Rev$")
public class ExportUI implements EventExecution {

  /**
   * Method doing the actual export.
   *
   * @param   _parameter parameter as from eFaps
   * @return Return with a file
   * @throws EFapsException on error
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final Instance instance = _parameter.getInstance();
    final SearchQuery query = new SearchQuery();
    query.setObject(instance);
    query.addSelect("UUID");
    query.addSelect("Name");
    query.execute();
    String uuid = null;
    String name = null;
    if (query.next()) {
      uuid = (String) query.get("UUID");
      name = (String) query.get("Name");
    }

    Document xmlDoc = null;
    try {
      // Create a XML Document
      final DocumentBuilderFactory dbFactory
                                    = DocumentBuilderFactoryImpl.newInstance();
      dbFactory.setNamespaceAware(true);

      final DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

      xmlDoc = docBuilder.newDocument();
    } catch (final Exception e) {
      throw new EFapsException(this.getClass(), "execute.documentBuilder", e);
    }
    final Element root = xmlDoc.createElement("ui-command");
    xmlDoc.appendChild(root);
    root.setAttribute("xmlns", "http://www.efaps.org/xsd");
    root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    root.setAttribute("xsi:schemaLocation",
            "http://www.efaps.org/xsd http://www.efaps.org/xsd/eFaps_1.0.xsd");


    final Element uuidEl = xmlDoc.createElement("uuid");
    uuidEl.appendChild(xmlDoc.createTextNode(uuid));
    root.appendChild(uuidEl);

    final Element appEl = xmlDoc.createElement("file-application");
    appEl.appendChild(xmlDoc.createTextNode("eFaps-Kernel"));
    root.appendChild(appEl);

    final Element revEl = xmlDoc.createElement("file-revision");
    revEl.appendChild(xmlDoc.createTextNode("1"));
    root.appendChild(revEl);

    final Element definitionEl = xmlDoc.createElement("definition");
    root.appendChild(definitionEl);

    final Element versionEl = xmlDoc.createElement("version-expression");
    versionEl.appendChild(xmlDoc.createTextNode("(version==latest)"));
    definitionEl.appendChild(versionEl);

    final Element nameEl = xmlDoc.createElement("name");
    nameEl.appendChild(xmlDoc.createTextNode(name));
    definitionEl.appendChild(nameEl);

    final Element targetEl = xmlDoc.createElement("target");

    final List<Element> targets = evaluateLinks(instance, xmlDoc);
    for (final Element propEl : targets) {
      if ("image".equals(propEl.getTagName())) {
        definitionEl.appendChild(propEl);
      } else {
        targetEl.appendChild(propEl);
      }
    }

    addAccessElement(instance, xmlDoc, definitionEl);
    definitionEl.appendChild(targetEl);

    final List<Element> events = evaluateEvents(instance, xmlDoc);
    for (final Element propEl : events) {
      targetEl.appendChild(propEl);
    }

    addPropertyElements(instance, xmlDoc, definitionEl);

    xmlDoc.normalizeDocument();

    final String xml = XMLExport.generateXMLString(xmlDoc);

    final File sessionFolder
            = XMLExport.getSessionFolder(XMLExport.getDefaultFileStoreFolder(),
                                         "-export");
    final File file = new File(sessionFolder, name + ".xml");

    try {
      final BufferedWriter output = new BufferedWriter(new FileWriter(file));
      output.write(xml);
      output.close();

    } catch (final IOException e) {
      throw new EFapsException(this.getClass(), "execute.writter", e);
    }

    ret.put(ReturnValues.VALUES, file);
    return ret;
  }

  /**
   * @param _instance   Instance that will be expanded
   * @param _xmlDoc     xml document used for creation of the new elements
   * @param _parent     parent element the new elements will be added to
   * @throws EFapsException on error
   */
  private void addPropertyElements(final Instance _instance,
                                   final Document _xmlDoc,
                                   final Element _parent)
      throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setExpand(_instance, "Admin_Common_Property\\Abstract");
    query.addSelect("Name");
    query.addSelect("Value");
    query.execute();
    while (query.next()) {
      final Element propEl = _xmlDoc.createElement("property");
      propEl.setAttribute("name", (String) query.get("Name"));
      propEl.appendChild(_xmlDoc.createTextNode((String) query.get("Value")));
      _parent.appendChild(propEl);
    }
  }

  /**
   * @param _instance   Instance that will be expanded
   * @param _xmlDoc     xml document used for creation of the new elements
   * @param _parent     parent element the new elements will be added to
   * @throws EFapsException on error
   */
  private void addAccessElement(final Instance _instance,
                                final Document _xmlDoc,
                                final Element _parent)
      throws EFapsException {
    final Element element = _xmlDoc.createElement("access");
    boolean add = false;
    final SearchQuery query = new SearchQuery();
    query.setExpand(_instance, "Admin_UI_Access\\UILink.UserLink");
    query.addSelect("Name");
    query.addSelect("Type");
    query.execute();
    while (query.next()) {
      String elementType = "";
      final Type type = (Type) query.get("Type");
      if (type.equals(Type.get(EFapsClassNames.USER_ROLE))) {
        elementType = "role";
      } else if (type.equals(Type.get(EFapsClassNames.USER_PERSON))) {
        elementType = "person";
      } else if (type.equals(Type.get(EFapsClassNames.USER_GROUP))) {
        elementType = "group";
      }

      final Element propEl = _xmlDoc.createElement(elementType);
      propEl.appendChild(_xmlDoc.createTextNode((String) query.get("Name")));
      element.appendChild(propEl);
      add = true;
    }
    if (add) {
      _parent.appendChild(element);
    }
  }

  /**
   * @param _instance   Instance that will be expanded
   * @param _xmlDoc     xml document used for creation of the new elements
   * @return list with elements
   * @throws EFapsException on error
   */
  private List<Element> evaluateLinks(final Instance _instance,
                                      final Document _xmlDoc)
      throws EFapsException {
    final List<Element> ret = new ArrayList<Element>();
    final SearchQuery query = new SearchQuery();
    query.setExpand(_instance, "Admin_UI_Link\\From");
    query.addSelect("To.Type");
    query.addSelect("To.Name");
    query.execute();
    while (query.next()) {
      String element = "";
      final Type type = (Type) query.get("To.Type");
      if (type.equals(Type.get(EFapsClassNames.FORM))) {
        element = "form";
      } else if (type.equals(Type.get(EFapsClassNames.MENU))) {
        element = "menu";
      } else if (type.equals(Type.get(EFapsClassNames.TABLE))) {
        element = "table";
      } else if (type.equals(Type.get(EFapsClassNames.IMAGE))) {
        element = "image";
      } else if (type.equals(Type.get(EFapsClassNames.SEARCH))) {
        element = "search";
      }

      final Element propEl = _xmlDoc.createElement(element);
      propEl.appendChild(_xmlDoc.createTextNode((String) query.get("To.Name")));
      ret.add(propEl);
    }

    return ret;
  }

  /**
   * @param _instance   Instance that will be expanded
   * @param _xmlDoc     xml document used for creation of the new elements
   * @return list with elements
   * @throws EFapsException on error
   */
  private List<Element> evaluateEvents(final Instance _instance,
                                       final Document _xmlDoc)
      throws EFapsException {
    final List<Element> ret = new ArrayList<Element>();
    final SearchQuery query = new SearchQuery();
    query.setExpand(_instance, "Admin_Event_Definition\\Abstract");
    query.addSelect("OID");
    query.addSelect("Type");
    query.addSelect("Method");
    query.addSelect("JavaProg.Name");
    query.execute();
    while (query.next()) {
      String element = "";
      final Type type = (Type) query.get("Type");
      if (type.equals(Type.get("Admin_UI_TableEvaluateEvent"))) {
        element = "evaluate";
      } else if (type.equals(Type.get("Admin_UI_ValidateEvent"))) {
        element = "validate";
      } else if (type.equals(Type.get("Admin_UI_CommandExecuteEvent"))) {
        element = "execute";
      }
      final Element propEl = _xmlDoc.createElement(element);
      propEl.setAttribute("program", (String) query.get("JavaProg.Name"));
      propEl.setAttribute("method", (String) query.get("Method"));

      final Instance inst = Instance.get((String) query.get("OID"));
      addPropertyElements(inst, _xmlDoc, propEl);
      ret.add(propEl);
    }
    return ret;
  }
}

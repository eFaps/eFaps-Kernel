/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.ui.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.efaps.ui.wicket.models.AbstractModel;
import org.efaps.ui.wicket.models.FieldTableModel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.HeadingModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.FormModel.ElementType;
import org.efaps.ui.wicket.models.FormModel.FormElementModel;
import org.efaps.ui.wicket.models.FormModel.FormRowModel;
import org.efaps.ui.wicket.models.TableModel.RowModel;
import org.efaps.ui.wicket.models.cell.FormCellModel;
import org.efaps.ui.wicket.models.cell.TableCellModel;

public class XMLExport {// XML tag's

  public enum TAG {
    FORM("form"),
    FORM_CELL("f_cell"),
    FORM_ROW("f_row"),
    HEADING("heading"),
    LABEL("label"),
    ROOT("eFaps"),
    TABLE("table"),
    TABLE_BODY("t_body"),
    TABLE_CELL("t_cell"),
    TABLE_HEADER("t_header"),
    TABLE_ROW("t_row"),
    TITLE("title"),
    TIMESTAMP("TimeStamp"),
    VALUE("value");

    public String value;

    private TAG(final String _value) {
      this.value = _value;
    }
  }

  public enum XML {
    VERSION("1.0"),
    ENCODING("UTF-8");

    public String value;

    private XML(final String _value) {
      this.value = _value;
    }

  }

  // Format definitions
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  // Variables
  private Date msgTimeStamp = null;

  private String xmlStr = null;

  // Constructor
  public XMLExport(AbstractModel _model) {

    this.msgTimeStamp = new Date();

    // Generate the XML Document using DOM
    Document xmlDoc = this.generateXMLDocument(_model);

    // Generate a XML String
    this.generateXMLString(xmlDoc);
    System.out.print(this.xmlStr);

    // configure fopFactory as desired
    FopFactory fopFactory = FopFactory.newInstance();

    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    // configure foUserAgent as desired

    try {
      OutputStream out =
          new FileOutputStream(new File("/Users/janmoxter/documents",
              "ResultXML2FO.fo"));

      OutputStream pdf =
          new FileOutputStream(new File("/Users/janmoxter/documents",
              "ResultXML2PDF.pdf"));

      // Construct fop with desired output format
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdf);

      // Setup XSLT
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer =
          factory
              .newTransformer(new StreamSource(
                  new File(
                      "/Users/janmoxter/Documents/workspace/efaps/webapp/src/main/java/org/efaps/ui/xml",
                      "eFapsFO.xsl")));

      Source src = new DOMSource(xmlDoc);

      // Resulting SAX events (the generated FO) must be piped through to FOP
      Result res = new StreamResult(out);

      transformer.transform(src, res);

      Result res2 = new SAXResult(fop.getDefaultHandler());
      transformer.transform(src, res2);
      // Start XSLT transformation and FOP processing

      out.close();
      pdf.close();
    } catch (TransformerConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FOPException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally {

    }

  }

  // Retrive probe message as XML string
  public String getXMLString() {
    return this.xmlStr;
  }

  // Generate a DOM XML document
  private Document generateXMLDocument(AbstractModel _model) {
    Document xmlDoc = null;
    try {
      // Create a XML Document
      DocumentBuilderFactory dbFactory =
          DocumentBuilderFactoryImpl.newInstance();
      DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
      xmlDoc = docBuilder.newDocument();
    } catch (Exception e) {
      System.out.println("Error " + e);
    }

    // Create the root element
    Element root = xmlDoc.createElement(TAG.ROOT.value);
    xmlDoc.appendChild(root);

    // Add TimeStamp Element and its value
    Element item = xmlDoc.createElement(TAG.TIMESTAMP.value);
    item.appendChild(xmlDoc.createTextNode((new SimpleDateFormat(
        DATE_TIME_FORMAT)).format(this.msgTimeStamp)));
    root.appendChild(item);

    root.appendChild(xmlDoc.createComment("titel"));
    Element title = xmlDoc.createElement(TAG.TITLE.value);
    title.appendChild(xmlDoc.createTextNode(_model.getTitle()));
    root.appendChild(title);

    if (_model instanceof TableModel) {
      // add a table
      root.appendChild(xmlDoc.createComment("table"));
      root.appendChild(getTableElement(xmlDoc, (TableModel) _model));
    } else if (_model instanceof FormModel) {
      for (FormModel.Element formelement : ((FormModel) _model).getElements()) {
        if (formelement.getType().equals(ElementType.FORM)) {
          root.appendChild(xmlDoc.createComment("form"));
          root.appendChild(getFormElement(xmlDoc,
              (FormElementModel) formelement.getModel()));
        } else if (formelement.getType().equals(ElementType.HEADING)) {
          root.appendChild(getHeadingElement(xmlDoc, (HeadingModel) formelement
              .getModel()));
        } else if (formelement.getType().equals(ElementType.TABLE)) {
          root.appendChild(xmlDoc.createComment("table"));
          root.appendChild(getTableElement(xmlDoc,
              (FieldTableModel) formelement.getModel()));
        }
      }
    }
    return xmlDoc;
  }

  private Element getHeadingElement(final Document _xmlDoc,
                                    final HeadingModel _model) {
    Element heading = _xmlDoc.createElement(TAG.HEADING.value);
    Element value = _xmlDoc.createElement(TAG.VALUE.value);
    heading.setAttribute("level", ((Integer) _model.getLevel()).toString());
    heading.appendChild(value);
    value.appendChild(_xmlDoc.createTextNode(_model.getLabel()));
    return heading;
  }

  private Element getFormElement(final Document _xmlDoc,
                                 final FormElementModel _model) {
    Element form = _xmlDoc.createElement(TAG.FORM.value);
    for (FormRowModel rowmodel : _model.getRowModels()) {
      Element f_row = _xmlDoc.createElement(TAG.FORM_ROW.value);
      form.appendChild(f_row);
      for (FormCellModel formcellmodel : rowmodel.getValues()) {
        Element f_cell = _xmlDoc.createElement(TAG.FORM_CELL.value);
        f_row.appendChild(f_cell);

        Element f_label = _xmlDoc.createElement(TAG.LABEL.value);
        f_cell.appendChild(f_label);
        f_label.appendChild(_xmlDoc
            .createTextNode(formcellmodel.getCellLabel()));

        Element value = _xmlDoc.createElement(TAG.VALUE.value);
        f_cell.appendChild(value);
        value.appendChild(_xmlDoc.createTextNode(formcellmodel.getCellValue()));
      }
    }
    return form;
  }

  private Element getTableElement(final Document _xmlDoc,
                                  final TableModel _model) {

    Element table = _xmlDoc.createElement(TAG.TABLE.value);

    Element table_header = _xmlDoc.createElement(TAG.TABLE_HEADER.value);
    table.appendChild(table_header);

    for (HeaderModel headermodel : _model.getHeaders()) {
      Element t_cell = _xmlDoc.createElement(TAG.TABLE_CELL.value);
      t_cell.setAttribute("name", headermodel.getName());
      table_header.appendChild(t_cell);

      Element value = _xmlDoc.createElement(TAG.VALUE.value);
      value.appendChild(_xmlDoc.createTextNode(headermodel.getLabel()));

      t_cell.appendChild(value);
    }
    boolean addBody = true;
    Element t_body = _xmlDoc.createElement(TAG.TABLE_BODY.value);
    for (RowModel rowmodel : _model.getValues()) {
      if (addBody) {
        table.appendChild(t_body);
        addBody = false;
      }
      Element t_row = _xmlDoc.createElement(TAG.TABLE_ROW.value);
      t_body.appendChild(t_row);

      for (TableCellModel tablecellmodel : rowmodel.getValues()) {
        Element t_cell = _xmlDoc.createElement(TAG.TABLE_CELL.value);
        Element value = _xmlDoc.createElement(TAG.VALUE.value);
        t_cell.appendChild(value);
        value
            .appendChild(_xmlDoc.createTextNode(tablecellmodel.getCellValue()));
        t_cell.setAttribute("name", tablecellmodel.getName());
        t_row.appendChild(t_cell);
      }

    }
    return table;
  }

  // Generate String out of the XML document object
  private void generateXMLString(Document _xmlDoc) {

    StringWriter strWriter = null;
    XMLSerializer probeMsgSerializer = null;
    OutputFormat outFormat = null;

    try {
      probeMsgSerializer = new XMLSerializer();
      strWriter = new StringWriter();
      outFormat = new OutputFormat();

      // Setup format settings
      outFormat.setEncoding(XML.ENCODING.value);
      outFormat.setVersion(XML.VERSION.value);
      outFormat.setIndenting(true);
      outFormat.setIndent(2);

      // Define a Writer
      probeMsgSerializer.setOutputCharStream(strWriter);

      // Apply the format settings
      probeMsgSerializer.setOutputFormat(outFormat);

      // Serialize XML Document
      probeMsgSerializer.serialize(_xmlDoc);
      this.xmlStr = strWriter.toString();
      strWriter.close();

    } catch (IOException ioEx) {
      System.out.println("Error " + ioEx);
    }
  }

}

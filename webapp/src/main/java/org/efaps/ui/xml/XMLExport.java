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
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIHeading;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.ui.wicket.resources.XSLResource;
import org.efaps.ui.wicket.util.MimeTypes;
import org.efaps.util.EFapsException;

/**
 * This class provides functionalities to produce a PDF-Document to be shown to
 * the user
 *
 * @author jmox
 * @version $Id$
 */
public class XMLExport {

  public enum XML {
    VERSION("1.0"),
    ENCODING("UTF-8");

    public String value;

    private XML(final String _value) {
      this.value = _value;
    }

  }

  /**
   * Format definitions for the Date
   */
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * the date to be added as a TimeStamp
   */
  private Date msgTimeStamp = null;

  /**
   * the folder the file lies in
   */
  private File fileStoreFolder;

  /**
   * the file to be shown to the user
   */
  private File file;

  private MimeTypes mimeType;

  /**
   * this variable contains the model used in this XMLExport
   */
  private AbstractUIObject model;

  /**
   * the document
   */
  private Document xmlDocument;

  private static String APPNAME = Application.get().getApplicationKey();

  // Constructor
  public XMLExport(final AbstractUIObject _model) throws EFapsException {
    initialise(_model);
  }

  public XMLExport(final Object object) throws EFapsException {
    if (object instanceof Component) {
      initialise((AbstractUIObject) ((Component) object).getPage().getDefaultModelObject());
    }
  }

  /**
   * this method creates the Document
   *
   * @param _mimeType
   * @throws EFapsException
   */
  public void generateDocument(final MimeTypes _mimeType) throws EFapsException {
    OutputStream out = null;
    try {
      this.mimeType = _mimeType;

      // configure fopFactory as desired
      final FopFactory fopFactory = FopFactory.newInstance();

      final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
      // configure foUserAgent as desired

      final File sessionFolder = getSessionFolder(this.fileStoreFolder,"-print");

      this.file =
          new File(sessionFolder, "print-"
              + this.model.getInstanceKey()
              + "."
              + this.mimeType.getEnding());

      out = new FileOutputStream(this.file);

      // Construct fop with desired output format
      final Fop fop =
          fopFactory.newFop(this.mimeType.getContentType(), foUserAgent, out);

      // Setup XSLT
      final TransformerFactory factory = TransformerFactory.newInstance();
      final XSLResource resource = XSLResource.get("xsl.eFapsFO.xsl");

      final Transformer transformer =
          factory.newTransformer(new StreamSource(resource.getResourceStream()
              .getInputStream()));

      final Source src = new DOMSource(this.xmlDocument);

      final Result res2 = new SAXResult(fop.getDefaultHandler());
      transformer.transform(src, res2);
      // Start XSLT transformation and FOP processing

    } catch (final FileNotFoundException e) {
      throw new EFapsException(this.getClass(),
          "generateDocument.FileNotFoundException", e, this.file);
    } catch (final FOPException e) {
      throw new EFapsException(this.getClass(), "generateDocument", e,
          this.file);
    } catch (final TransformerConfigurationException e) {
      throw new EFapsException(this.getClass(), "generateDocument", e,
          this.file);
    } catch (final TransformerException e) {
      throw new EFapsException(this.getClass(), "generateDocument", e,
          this.file);
    } catch (final ResourceStreamNotFoundException e) {
      throw new EFapsException(this.getClass(), "generateDocument", e,
          this.file);
    }
    finally {
      try {
        out.close();
      } catch (final IOException e) {
        throw new EFapsException(this.getClass(), "generateDocument", e,
            this.file);
      }
    }
  }

  /**
   * method to initialise this XMLExport
   *
   * @param _model
   * @throws EFapsException
   */
  private void initialise(final AbstractUIObject _model) throws EFapsException {
    this.msgTimeStamp = new Date();
    this.model = _model;
    // Generate the XML Document using DOM
    // Generate a XML String
    this.xmlDocument = generateXMLDocument(_model);
    // Generate a XML String
    this.xmlDocument.normalizeDocument();

    // System.out.print(generateXMLString(this.xmlDocument));

    this.fileStoreFolder = getDefaultFileStoreFolder();
    this.fileStoreFolder.mkdirs();
  }

  /**
   * Generate a DOM XML document
   *
   * @param _uiObject
   * @return
   * @throws EFapsException
   */
  protected Document generateXMLDocument(final AbstractUIObject _uiObject)
                                                              throws EFapsException {
    Document xmlDoc = null;
    try {
      // Create a XML Document
      final DocumentBuilderFactory dbFactory =
          DocumentBuilderFactoryImpl.newInstance();
      dbFactory.setNamespaceAware(true);
      final DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
      xmlDoc = docBuilder.newDocument();
    } catch (final Exception e) {
      throw new EFapsException(this.getClass(), "generateXMLDocument", e,
          this.model);
    }

    // Create the root element
    final Element root = xmlDoc.createElement(TAG.ROOT.value);
    xmlDoc.appendChild(root);

    // Add TimeStamp Element and its value
    final Element item = xmlDoc.createElement(TAG.TIMESTAMP.value);
    item.appendChild(xmlDoc.createTextNode((new SimpleDateFormat(
        DATE_TIME_FORMAT)).format(this.msgTimeStamp)));
    root.appendChild(item);

    root.appendChild(xmlDoc.createComment("titel"));
    final Element title = xmlDoc.createElement(TAG.TITLE.value);
    title.appendChild(xmlDoc.createTextNode(_uiObject.getTitle()));
    root.appendChild(title);

    if (_uiObject instanceof UITable) {
      // add a table
      root.appendChild(xmlDoc.createComment("table"));
      root.appendChild(getTableElement(xmlDoc, (UITable) _uiObject));
    } else if (_uiObject instanceof UIForm) {
      for (final UIForm.Element formelement : ((UIForm) _uiObject)
          .getElements()) {
        if (formelement.getType().equals(ElementType.FORM)) {
          root.appendChild(xmlDoc.createComment("form"));
          root.appendChild(getFormElement(xmlDoc, (UIForm) _uiObject,
              (FormElement) formelement.getElement()));
        } else if (formelement.getType().equals(ElementType.HEADING)) {
          root.appendChild(getHeadingElement(xmlDoc, (UIHeading) formelement
              .getElement()));
        } else if (formelement.getType().equals(ElementType.TABLE)) {
          root.appendChild(xmlDoc.createComment("table"));
          root.appendChild(getTableElement(xmlDoc,
              (UIFieldTable) formelement.getElement()));
        }
      }
    }
    return xmlDoc;
  }

  /**
   * create a DOM-Element for a Heading
   *
   * @param _xmlDoc
   *                Document this DOMElemnt should be added
   * @param _model
   * @return the Element for a Heading
   */
  protected Element getHeadingElement(final Document _xmlDoc,
                                      final UIHeading _model) {
    final Element heading = _xmlDoc.createElement(TAG.HEADING.value);
    final Element value = _xmlDoc.createElement(TAG.VALUE.value);
    heading.setAttribute("level", ((Integer) _model.getLevel()).toString());
    heading.appendChild(value);
    value.appendChild(_xmlDoc.createTextNode(_model.getLabel()));
    return heading;
  }

  /**
   * create a DOM-Element for a Form
   *
   * @param _xmlDoc
   *                Document this DOMElemnt should be added
   * @param _model
   * @return the Element for a Form
   */
  protected Element getFormElement(final Document _xmlDoc,
                                   final UIForm _formmodel,
                                   final FormElement _model) {
    final Element form = _xmlDoc.createElement(TAG.FORM.value);
    form.setAttribute("maxGroupCount",
        ((Integer) _formmodel.getMaxGroupCount()).toString());

    for (final FormRow rowmodel : _model.getRowModels()) {

      final Element f_row = _xmlDoc.createElement(TAG.FORM_ROW.value);
      form.appendChild(f_row);
      for (final UIFormCell formcellmodel : rowmodel.getValues()) {
        final Integer colspan =
            2 * (_formmodel.getMaxGroupCount() - rowmodel.getGroupCount()) + 1;

        final Element f_cell = _xmlDoc.createElement(TAG.FORM_CELL.value);
        f_row.appendChild(f_cell);
        f_cell.setAttribute("type", "Label");

        final Element f_label = _xmlDoc.createElement(TAG.VALUE.value);
        f_cell.appendChild(f_label);
        f_label.appendChild(_xmlDoc
            .createTextNode(formcellmodel.getCellLabel()));

        final Element f_cellvalue = _xmlDoc.createElement(TAG.FORM_CELL.value);
        f_cellvalue.setAttribute("type", "Value");
        f_cellvalue.setAttribute("column-span", colspan.toString());
        f_row.appendChild(f_cellvalue);
        final Element value = _xmlDoc.createElement(TAG.VALUE.value);
        f_cellvalue.appendChild(value);
        if (formcellmodel.getCellValue() == null) {
          value.appendChild(_xmlDoc.createTextNode(""));
        } else {
          value.appendChild(_xmlDoc
              .createTextNode(formcellmodel.getCellValue()));
        }
      }
    }
    return form;
  }

  /**
   * create a DOM-Element for a Table
   *
   * @param _xmlDoc
   *                Document this DOMElemnt should be added
   * @param _model
   * @return the Element for a Table
   */
  protected Element getTableElement(final Document _xmlDoc,
                                    final UITable _model) {

    final Element table = _xmlDoc.createElement(TAG.TABLE.value);

    final Element table_header = _xmlDoc.createElement(TAG.TABLE_HEADER.value);
    table.appendChild(table_header);

    for (final UITableHeader headermodel : _model.getHeaders()) {
      final Element t_cell = _xmlDoc.createElement(TAG.TABLE_CELL.value);
      t_cell.setAttribute("name", headermodel.getName());
      String width;
      if (headermodel.isFixedWidth()) {
        width = headermodel.getWidth() + "pt";
      } else {
        width = 100 / _model.getWidthWeight() * headermodel.getWidth() + "%";
      }
      t_cell.setAttribute("width", width);
      table_header.appendChild(t_cell);

      final Element value = _xmlDoc.createElement(TAG.VALUE.value);
      value.appendChild(_xmlDoc.createTextNode(headermodel.getLabel()));

      t_cell.appendChild(value);
    }
    boolean addBody = true;
    final Element t_body = _xmlDoc.createElement(TAG.TABLE_BODY.value);
    for (final UIRow rowmodel : _model.getValues()) {
      if (addBody) {
        table.appendChild(t_body);
        addBody = false;
      }
      final Element t_row = _xmlDoc.createElement(TAG.TABLE_ROW.value);
      t_body.appendChild(t_row);

      for (final UITableCell tablecellmodel : rowmodel.getValues()) {
        final Element t_cell = _xmlDoc.createElement(TAG.TABLE_CELL.value);
        final Element value = _xmlDoc.createElement(TAG.VALUE.value);
        t_cell.appendChild(value);
        value
            .appendChild(_xmlDoc.createTextNode(tablecellmodel.getCellValue()));
        t_cell.setAttribute("name", tablecellmodel.getName());
        t_row.appendChild(t_cell);
      }

    }
    return table;
  }

  /**
   * Generate String out of the XML document object
   *
   * @param _xmlDoc
   * @return
   * @throws EFapsException
   */
  public static String generateXMLString(final Document _xmlDoc) throws EFapsException {
    String ret = null;
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
      ret = strWriter.toString();
      strWriter.close();

    } catch (final IOException e) {
      throw new EFapsException(XMLExport.class, "generateXMLString", e);
    }
    return ret;
  }

  /**
   * get the FileStore
   *
   * @return
   */
  public static File getDefaultFileStoreFolder() {
    final File dir =
        (File) ((EFapsApplication) Application.get()).getServletContext()
            .getAttribute("javax.servlet.context.tempdir");
    if (dir != null) {
      return dir;
    } else {
      try {
        return File.createTempFile("file-prefix", null).getParentFile();
      } catch (final IOException e) {
        throw new WicketRuntimeException(e);
      }
    }
  }

  /**
   * Get the SessionFolder.
   *
   * @param sessionId
   * @return
   */
  public static File getSessionFolder(final File _fileStoreFolder,
                                      final String _append) {
    final File storeFolder = new File(_fileStoreFolder, APPNAME + _append);
    final File sessionFolder = new File(storeFolder, Session.get().getId());
    if (!sessionFolder.exists()) {
      sessionFolder.mkdirs();
    }
    return sessionFolder;
  }

  /**
   * This is the getter method for the instance variable {@link #file}.
   *
   * @return value of instance variable {@link #file}
   */
  public File getFile() {
    return this.file;
  }

}

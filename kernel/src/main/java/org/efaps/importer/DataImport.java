/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.update.Install.ImportInterface;

/**
 * Class wich contains the method to launch the import of Data into a efaps
 * connected Database.
 *
 * @author jmox
 * @version $Id$
 */
public class DataImport implements ImportInterface {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(DataImport.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * contains the RootObject wich is the base for all other Objects
   */
  private RootObject root = null;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   * DefaultConstructor
   */
  public DataImport() {
    super();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * @param _xml
   *                String representing the file name including path to the
   *                XML-File
   * @see #readXMLFile(File)
   * @see #readXMLFile(URL)
   */
  public void readXMLFile(final String _xml) throws MalformedURLException {
    readXMLFile(new File(_xml));
  }

  /**
   * @param _xml
   *                XML-File
   * @see #readXMLFile(URL)
   */
  public void readXMLFile(final File _xml) throws MalformedURLException {
    readXMLFile(_xml.toURL());
  }

  /**
   * Method that uses the {@link org.apache.commons.digester.Digester} to read
   * the objects from the given xml-File an build the java-Objects in a
   * parent-child Hirachy.
   *
   * @param _url
   *                URL to the XML-File
   */
  public static DataImport readXMLFile(final URL _url) {

    DataImport ret = new DataImport();
    try {
      final Digester digester = new Digester();
      digester.setValidating(false);

      digester.addObjectCreate("import", RootObject.class);

      final String def = "import/definition";

      // Read the Definitions
      digester.addCallMethod(def + "/date", "setDateFormat", 1);
      digester.addCallParam(def + "/date", 0, "format");

      // Read OrderObject
      digester.addFactoryCreate(def + "/order", new OrderObjectFactory(), false);
      digester.addCallMethod(def + "/order/attribute",
          "addAttribute", 3, new Class[] { Integer.class, String.class, String.class });
      digester.addCallParam(def + "/order/attribute", 0, "index");
      digester.addCallParam(def + "/order/attribute", 1, "name");
      digester.addCallParam(def + "/order/attribute", 2, "criteria");
      digester.addSetNext(def + "/order", "addOrder", "org.efaps.importer.OrderObject");

      // Read DefaultObject
      digester.addObjectCreate(def + "/default", DefaultObject.class);
      digester.addCallMethod(def + "/default", "addDefault", 3);
      digester.addCallParam(def + "/default", 0, "type");
      digester.addCallParam(def + "/default", 1, "name");
      digester.addCallParam(def + "/default", 2);

      digester.addObjectCreate(def + "/default/linkattribute", ForeignObject.class);
      digester.addCallMethod(def + "/default/linkattribute", "setLinkAttribute", 2);
      digester.addCallParam(def + "/default/linkattribute", 0, "name");
      digester.addCallParam(def + "/default/linkattribute", 1,"type");

      digester.addCallMethod(def + "/default/linkattribute/queryattribute", "addAttribute", 2);
      digester.addCallParam(def + "/default/linkattribute/queryattribute", 0, "name");
      digester.addCallParam(def + "/default/linkattribute/queryattribute", 1);

      digester.addSetNext(def + "/default/linkattribute", "addLink", "org.efaps.importer.ForeignObject");

      // Create the Objects
      digester.addFactoryCreate("*/object", new InsertObjectFactory(), false);

      digester.addCallMethod("*/object/attribute", "addAttribute", 3);
      digester.addCallParam("*/object/attribute", 0, "name");
      digester.addCallParam("*/object/attribute", 1);
      digester.addCallParam("*/object/attribute", 2, "unique");

      digester.addCallMethod("*/object/file", "setCheckinObject", 2);
      digester.addCallParam("*/object/file", 0, "name");
      digester.addCallParam("*/object/file", 1, "url");

      digester.addCallMethod("*/object/parentattribute", "setParentAttribute", 2);
      digester.addCallParam("*/object/parentattribute", 0, "name");
      digester.addCallParam("*/object/parentattribute", 1, "unique");

      digester.addCallMethod("*/object/linkattribute", "addUniqueAttribute", 2);
      digester.addCallParam("*/object/linkattribute", 0, "unique");
      digester.addCallParam("*/object/linkattribute", 1, "name");

      digester.addSetNext("*/object", "addChild", "org.efaps.importer.InsertObject");

      digester.addObjectCreate("*/object/linkattribute", ForeignObject.class);
      digester.addCallMethod("*/object/linkattribute", "setLinkAttribute", 2);
      digester.addCallParam("*/object/linkattribute", 0, "name");
      digester.addCallParam("*/object/linkattribute", 1, "type");

      digester.addCallMethod("*/object/linkattribute/queryattribute", "addAttribute", 2);
      digester.addCallParam("*/object/linkattribute/queryattribute", 0, "name");
      digester.addCallParam("*/object/linkattribute/queryattribute", 1);

      digester.addSetNext("*/object/linkattribute", "addLink", "org.efaps.importer.ForeignObject");

      ret.root = (RootObject) digester.parse(_url);

      if (!ret.hasData()) {
        ret = null;
      }

    } catch (final IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (final SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  /**
   * Method that starts the Insertion of the Objects into the Database
   */
  public void updateInDB() {
    if (hasData()) {
      this.root.dbAddChilds();
    }
  }

  /**
   * has the root recieved Data from the digester that must be inserted
   *
   * @return true, if there is Data
   */
  public boolean hasData() {
    return this.root != null;
  }
}

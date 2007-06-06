/*
 * Copyright 2003 - 2007 The eFaps Team
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.db.transaction.VFSStoreFactoryBean;
import org.xml.sax.SAXException;

/**
 * Class wich contains the method to launch the import of Data into a efaps
 * connected Database.
 * 
 * @author jmo
 * @version $Id$
 * 
 */
public class DataImport {
  /**
   * Logger for this class
   */
  private static final Log LOG      = LogFactory.getLog(DataImport.class);

  /**
   * contains the RootObject wich is the base for all other Objects
   */
  private RootObject       root     = null;

  /**
   * contains the Path of the Base for the Context
   */
  private String           baseName = null;

  /**
   * DefaultConstructor used by Shell -create
   */
  public DataImport() {

  }

  /**
   * Constructor setting the BaseName
   * 
   * @param _basename
   */
  public DataImport(final String _basename) {
    this.baseName = _basename;
  }

  /**
   * initialises the Context for the VFS
   */
  public void initialise() {
    System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
        "org.efaps.importer.InitialContextFactory");
    VFSStoreFactoryBean bean = new VFSStoreFactoryBean();
    bean.setBaseName(this.baseName);
    bean
        .setProvider("org.apache.commons.vfs.provider.local.DefaultLocalFileProvider");

    Context ctx;
    try {
      ctx = new InitialContext();
      ctx.bind("java:comp/env", ctx);
      ctx.bind("eFaps/store/documents", bean);
    } catch (NamingException e) {
      LOG.error("initialise()", e);
    }

  }

  /**
   * Method that uses the {@link org.apache.commons.digester.Digester} to read
   * the objects from the given xml-File an build the java-Objects in a
   * parent-child Hirachy.
   * 
   * @param _xml
   *          String representing the Path to the XML-File
   */
  public void importFromXML(final String _xml) {
    importFromXML(new File(_xml));
  }

  /**
   * Method that uses the {@link org.apache.commons.digester.Digester} to read
   * the objects from the given xml-File an build the java-Objects in a
   * parent-child Hirachy.
   * 
   * @param _xml
   *          XML-File
   */
  public void importFromXML(final File _xml) {
    Digester digester = new Digester();

    digester.setValidating(false);

    digester.addObjectCreate("import", RootObject.class);

    // Read the Definitions
    digester.addCallMethod("import/definition/date", "setDateFormat", 1);
    digester.addCallParam("import/definition/date", 0, "format");

    // Read OrderObject
    digester.addFactoryCreate("import/definition/order",
        new OrderObjectFactory(), false);
    digester.addCallMethod("import/definition/order/attribute", "addAttribute",
        3, new Class[] { Integer.class, String.class, String.class });
    digester.addCallParam("import/definition/order/attribute", 0, "index");
    digester.addCallParam("import/definition/order/attribute", 1, "name");
    digester.addCallParam("import/definition/order/attribute", 2, "criteria");
    digester.addSetNext("import/definition/order", "addOrder",
        "org.efaps.importer.OrderObject");

    // Read DefaultObject
    digester.addObjectCreate("import/definition/default", DefaultObject.class);
    digester.addCallMethod("import/definition/default", "addDefault", 3);
    digester.addCallParam("import/definition/default", 0, "type");
    digester.addCallParam("import/definition/default", 1, "name");
    digester.addCallParam("import/definition/default", 2);

    digester.addObjectCreate("import/definition/default/linkattribute",
        ForeignObject.class);
    digester.addCallMethod("import/definition/default/linkattribute",
        "setLinkAttribute", 2);
    digester.addCallParam("import/definition/default/linkattribute", 0, "name");
    digester.addCallParam("import/definition/default/linkattribute", 1, "type");

    digester.addCallMethod(
        "import/definition/default/linkattribute/queryattribute",
        "addAttribute", 2);
    digester.addCallParam(
        "import/definition/default/linkattribute/queryattribute", 0, "name");
    digester.addCallParam(
        "import/definition/default/linkattribute/queryattribute", 1);

    digester.addSetNext("import/definition/default/linkattribute", "addLink",
        "org.efaps.importer.ForeignObject");

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

    digester.addSetNext("*/object", "addChild",
        "org.efaps.importer.InsertObject");

    digester.addObjectCreate("*/object/linkattribute", ForeignObject.class);
    digester.addCallMethod("*/object/linkattribute", "setLinkAttribute", 2);
    digester.addCallParam("*/object/linkattribute", 0, "name");
    digester.addCallParam("*/object/linkattribute", 1, "type");

    digester.addCallMethod("*/object/linkattribute/queryattribute",
        "addAttribute", 2);
    digester.addCallParam("*/object/linkattribute/queryattribute", 0, "name");
    digester.addCallParam("*/object/linkattribute/queryattribute", 1);

    digester.addSetNext("*/object/linkattribute", "addLink",
        "org.efaps.importer.ForeignObject");

    try {
      this.root = (RootObject) digester.parse(_xml);
    } catch (IOException e) {
      e.printStackTrace(System.err);
    } catch (SAXException e) {
      e.printStackTrace(System.err);
    }

  }

  /**
   * Method that starts the Insertion of the Objects into the Database
   */
  public void insertDB() {
    this.root.dbAddChilds();
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

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.digester.Digester;
import org.efaps.db.transaction.VFSStoreFactoryBean;
import org.efaps.util.EFapsException;
import org.xml.sax.SAXException;

/**
 * Class wich contains the main method to launch the import of Data into a efaps
 * connected Database.<br>
 * <br>
 * 
 * To start the import there must be three parameters:
 * <li>1. the path to the Bootstrap.xml e.g.
 * "/Users/xyz/Documents/workspace/bootstrap.xml</li>
 * <li>2. the Basname as defined in the Webapplication e.g.
 * file:///Users/xyz/Documents/webapps/ydss/docs/efaps/store/documents</li>
 * <li>3. the path to the xml-File to be imported e.g.
 * "/Users/xyz/Documents/workspace/Import.xml"</li>
 * <br>
 * 
 * @author jmo
 * 
 */
public class StartImport extends AbstractTransaction {
  /**
   * Logger for this class
   */
  private static final Log LOG  = LogFactory.getLog(StartImport.class);

  /**
   * contains the RootObject wich is the base for all other Objects
   */
  private RootObject       root = null;

  /**
   * Method for starting the import with e.g. a shell-Script
   * 
   * @param _args
   *          Paramters: 1.Bootstrap.xml 2.Basename 3.Import.xml
   */
  public static void main(String[] _args) {
    if (_args.length == 3) {
      (new StartImport()).execute(_args);
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("main(String[]) - Usage: Bootstrap.xml Basename Import.xml");
      }
    }

  }

  /**
   * Method that starts the import and is called directly by the main-Method
   * 
   * @param _args
   *          The given Parmeters of the main-Method
   */
  public void execute(final String... _args) {

    // e.g."/Users/janmoxter/Documents/workspace/ydss/bootstrap.xml"
    setBootstrap(_args[0]);
    if (LOG.isDebugEnabled()) {
      LOG.debug("execute(String) - " + _args[0]);
    }

    // e.g.
    // "file:///Users/janmoxter/Documents/apache-tomcat-5.5.20/webapps/ydss/docs/efaps/store/documents"
    String BaseName = _args[1];
    if (LOG.isDebugEnabled()) {
      LOG.debug("execute(String) - " + _args[1]);
    }

    // e.g.
    // "/Users/janmoxter/Documents/workspace/ydss/kernel/src/main/java/org/efaps/importer/Import.xml"
    String ImportFrom = _args[2];
    if (LOG.isDebugEnabled()) {
      LOG.debug("execute(String) - " + _args[2]);
    }
    loadRunLevel();

    try {
     
      System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
          "org.efaps.importer.InitialContextFactory");
      VFSStoreFactoryBean bean = new VFSStoreFactoryBean();
      bean.setBaseName(BaseName);
      bean
          .setProvider("org.apache.commons.vfs.provider.local.DefaultLocalFileProvider");

      Context ctx = new InitialContext();
      ctx.bind("java:comp/env", ctx);
      ctx.bind("eFaps/store/documents", bean);

      importFromXML(ImportFrom);
      
      super.login("Administrator", "");
      super.startTransaction();

      // TODO Administrator klein schreiben
      
      insertDB();

      super.commitTransaction();

    } catch (EFapsException e) {

      LOG.error("execute(String)", e);
    } catch (Exception e) {

      LOG.error("execute(String)", e);
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
    Digester digester = new Digester();

    digester.setValidating(false);

    digester.addObjectCreate("import", RootObject.class);

    // Read the Definitions
    digester.addCallMethod("import/definition/date", "setDateFormat", 1);
    digester.addCallParam("import/definition/date", 0, "format");

    digester.addFactoryCreate("import/definition/order",
        new OrderObjectFactory(), false);
    digester.addCallMethod("import/definition/order/attribute", "addAttribute",
        3, new Class[] { Integer.class, String.class, String.class });
    digester.addCallParam("import/definition/order/attribute", 0, "index");
    digester.addCallParam("import/definition/order/attribute", 1, "name");
    digester.addCallParam("import/definition/order/attribute", 2, "criteria");

    digester.addObjectCreate("import/definition/default", DefaultObject.class);
    digester.addCallMethod("import/definition/default", "addDefault", 3);
    digester.addCallParam("import/definition/default", 0, "type");
    digester.addCallParam("import/definition/default", 1, "name");
    digester.addCallParam("import/definition/default", 2);

    digester.addSetNext("import/definition/order", "addOrder",
        "org.efaps.importer.OrderObject");

    // Create the Objects
    digester.addFactoryCreate("*/object", new InsertObjectFactory(), false);

    digester.addCallMethod("*/object/attribute", "addAttribute", 3);
    digester.addCallParam("*/object/attribute", 0, "name");
    digester.addCallParam("*/object/attribute", 1);
    digester.addCallParam("*/object/attribute", 2, "unique");

    digester.addCallMethod("*/file", "setCheckinObject", 2);
    digester.addCallParam("*/file", 0, "name");
    digester.addCallParam("*/file", 1, "url");

    digester.addCallMethod("*/parentattribute", "setParentAttribute", 2);
    digester.addCallParam("*/parentattribute", 0, "name");
    digester.addCallParam("*/parentattribute", 1, "unique");

    digester.addCallMethod("*/linkattribute", "addUniqueAttribute", 2);
    digester.addCallParam("*/linkattribute", 0, "unique");
    digester.addCallParam("*/linkattribute", 1, "name");

    digester.addSetNext("*/object", "addChild",
        "org.efaps.importer.InsertObject");

    digester.addObjectCreate("*/linkattribute", ForeignObject.class);
    digester.addCallMethod("*/linkattribute", "setLinkAttribute", 2);
    digester.addCallParam("*/linkattribute", 0, "name");
    digester.addCallParam("*/linkattribute", 1, "type");

    digester.addCallMethod("*/queryattribute", "addAttribute", 2);
    digester.addCallParam("*/queryattribute", 0, "name");
    digester.addCallParam("*/queryattribute", 1);

    digester.addSetNext("*/linkattribute", "addLink",
        "org.efaps.importer.ForeignObject");

    try {
      this.root = (RootObject) digester.parse(new File(_xml));
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

}

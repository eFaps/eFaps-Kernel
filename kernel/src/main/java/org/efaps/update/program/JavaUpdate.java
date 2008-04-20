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

package org.efaps.update.program;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.ESJPImporter;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The class updates java program from type <code>Admin_Program_Java</code>
 * inside the eFaps database.
 *
 * @author tmo
 * @version $Id$
 */
public class JavaUpdate extends AbstractUpdate {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(JavaUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to initialize the type of Java programs.
   */
  public JavaUpdate() {
    super("Admin_Program_Java");
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Sets the root path in which the Class file is located. The value is set for
   * each single definition of the JavaUpdate.
   *
   * @param _root   name of the path where the image file is located
   */
  protected void setRoot(final String _root) {
    for (AbstractDefinition def : getDefinitions()) {
      ((JavaDefinition) def).setRoot(_root);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * If the extension of the file is <code>.java</code>, the method returns
   * an instance of this class. The instance of this class owns one definition
   * instance where the code and the name is defined.
   *
   * @param _file
   *                instance of the file to read
   */
  public static JavaUpdate readXMLFile(final URL _url) {
    JavaUpdate update = null;
    try {
      final Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("esjp", JavaUpdate.class);

      digester.addObjectCreate("esjp/definition", JavaDefinition.class);
      digester.addSetNext("esjp/definition", "addDefinition");

      digester.addCallMethod("esjp/definition/version", "setVersion", 4);
      digester.addCallParam("esjp/definition/version/application", 0);
      digester.addCallParam("esjp/definition/version/global", 1);
      digester.addCallParam("esjp/definition/version/local", 2);
      digester.addCallParam("esjp/definition/version/mode", 3);

      digester.addCallMethod("esjp/definition/file", "setFile", 1);
      digester.addCallParam("esjp/definition/file", 0);

      update = (JavaUpdate) digester.parse(_url);

      if (update != null) {
        String urlStr = _url.toString();
        final int i = urlStr.lastIndexOf("/");
        urlStr = urlStr.substring(0, i + 1);
        update.setRoot(urlStr);
        update.setURL(_url);
      }

    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return update;
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  /**
   * The Java definition holds the code and the name of the Java class.
   */
  public static class JavaDefinition extends AbstractDefinition {

    ///////////////////////////////////////////////////////////////////////////
    // instance variables

    /**
     * Name of the Java file (without the path) to import.
     */
    private String file = null;

    /**
     * Name of the root path used to initialize the path for the Java File.
     */
    private String root = null;

    /**
     *
     */
    private ESJPImporter javaCode = null;

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    @Override
    public void createInDB(final Type _dataModelType,
                           final String _uuid,
                           final boolean _abstractType)
        throws EFapsException
    {
      readJavaCode();
      setName(this.javaCode.getClassName());
      // if no instance exists, a new insert must be done
      Instance instance = this.javaCode.searchInstance();
      Insert insert = null;
      if (instance == null) {
        insert = new Insert(_dataModelType);
        insert.add("Name", this.javaCode.getClassName());
        insert.executeWithoutAccessCheck();
      }
    }

    /**
     * The method overwrites the method from the super class, because Java
     * programs are searched by the name (and not by UUID like in the super
     * class).
     *
     * @param _dataModelType  instance of the type of the object which must be
     *                        updated
     * @param _uuid           uuid of the object to update
     * @param _allLinkTypes   all link types to update
     */
    @Override
    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes,
                           final boolean _abstractType)
        throws EFapsException
    {
      readJavaCode();
      setName(this.javaCode.getClassName());
      final Instance instance = updateInDB(this.javaCode.searchInstance(), _allLinkTypes);
      // checkin source code
      this.javaCode.updateDB(instance);
    }

    /**
     * Reads the Java source code which is in the path {@link #root} with file
     * name {@link #file}.
     *
     * @throws EFapsException if the Java source code could not be read or the
     *                        file could not be accessed because of the wrong
     *                        URL
     * @see #javaCode
     * @see #root
     * @see #file
     */
    protected void readJavaCode()
        throws EFapsException
    {
      if (this.javaCode == null)  {
        try {
          this.javaCode = new ESJPImporter(new URL(this.root + this.file));
        } catch (MalformedURLException e) {
          throw new EFapsException(getClass(),
                                   "readJavaCode.MalformedURLException",
                                   e,
                                   this.root + this.file);
        }
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    // instance getter / setter methods

    /**
     * This is the setter method for instance variable {@link #file}.
     *
     * @param _number new value for instance variable {@link #file}
     * @see #file
     */
    public void setFile(final String _file)
    {
      this.file = _file;
    }

    /**
     * This is the setter method for instance variable {@link #root}.
     *
     * @param _root   new value for instance variable {@link #root}
     * @see #root
     */
    public void setRoot(final String _root)
    {
      this.root = _root;
    }

    @Override
    public String toString()
    {
      return new ToStringBuilder(this)
              .appendSuper(super.toString())
              .append("file", this.file)
              .append("root", this.root)
              .toString();
    }
  }
}

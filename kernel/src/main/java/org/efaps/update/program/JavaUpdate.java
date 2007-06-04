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

package org.efaps.update.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;
import org.xml.sax.SAXException;

/**
 * The class updates java program from type <code>Admin_Program_Java</code>
 * inside the eFaps database.
 * 
 * @author tmo
 * @version $Id$
 */
public class JavaUpdate extends AbstractUpdate {
  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(JavaUpdate.class);

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to initiliase the type of Java programs.
   */
  public JavaUpdate() {
    super("Admin_Program_Java");
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Wrapper method for {@link #readXMLFile(File)}.
   * 
   * @param _fileName
   *          name of the Java file to read
   * @see #readXMLFile(File);
   */
  public static JavaUpdate readXMLFile(final String _fileName)
                                                              throws IOException {
    return readXMLFile(new File(_fileName));
  }

  /**
   * If the extension of the file is <code>.java</code>, the method returns
   * an instance of this class. The instance of this class owns one definition
   * instance where the code and the name is defined.
   * 
   * @param _file
   *          instance of the file to read
   */
  public static JavaUpdate readXMLFile(final File _file) throws IOException {
    JavaUpdate update = null;
    String ext = _file.getName().substring(_file.getName().lastIndexOf('.'));

    if (".java".equals(ext)) {
      update = new JavaUpdate();
      update.addDefinition(new JavaDefinition(_file));
    } else if (".xml".equals(ext)) {
      try {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("program", JavaUpdate.class);

        digester.addObjectCreate("program/definition", JavaDefinition.class);
        digester.addSetNext("program/definition", "addDefinition");

        digester.addCallMethod("program/definition/version", "setVersion", 4);
        digester.addCallParam("program/definition/version/application", 0);
        digester.addCallParam("program/definition/version/global", 1);
        digester.addCallParam("program/definition/version/local", 2);
        digester.addCallParam("program/definition/version/mode", 3);

        digester.addCallMethod("program/definition/file", "setFile", 1);
        digester.addCallParam("program/definition/file", 0);

        update = (JavaUpdate) digester.parse(_file);

        if (update != null) {
          update.setRootPath(_file.getParent());
          update.setFile(_file);
        }

      } catch (SAXException e) {
        LOG.error(_file.getName() + "' seems to be invalide XML", e);
      }
    }
    return update;
  }

  /**
   * Sets the root path in which the Class file is located. The value is set for
   * each single definition of the JavaUpdate.
   * 
   * @param _rootPath
   *          name of the path where the Class file is located
   */
  protected void setRootPath(final String _rootPath) {
    for (DefinitionAbstract def : getDefinitions()) {
      ((JavaDefinition) def).setRootPath(_rootPath);
    }
  }

  /**
   * The Java definition holds the code and the name of the Java class.
   */
  public static class JavaDefinition extends DefinitionAbstract {

    /**
     * Name of the Java file (incl. the path) to import.
     */
    private String  file           = null;

    /**
     * Name of the root path used to initialise the path for the Java File.
     */
    private String  rootPath       = null;

    /**
     * Boolean that stores, if the ClassName was allready set by
     * {@link setClassName}
     */
    private boolean classNameIsSet = false;

    /**
     * default constructor
     */
    public JavaDefinition() {
    }

    /**
     * The constructor reads the code in the file, extracts the package name and
     * sets the name of this Java definition (the name is the package name
     * together with the name of the file exluding the <code>.java</code>).
     * 
     * @param _file
     *          file with the Java code
     */
    public JavaDefinition(final File _file) throws IOException {
      setVersion("eFaps", "1", "1", "true");
      setClassName(_file);
    }

    /**
     * This is the setter method for instance variable {@link #file}.
     * 
     * @param _number
     *          new value for instance variable {@link #file}
     * @see #file
     */
    public void setFile(final String _file) {
      this.file = _file;
    }

    /**
     * This Method reads the code in the file, extracts the package name and
     * sets the name of this Java definition (the name is the package name
     * together with the name of the file exluding the <code>.java</code>).
     * 
     * @param _file
     *          file with the Java code
     */
    private void setClassName(final File _file) throws IOException {
      final char[] buf = new char[1024];

      Reader r = new InputStreamReader(new FileInputStream(_file));

      StringBuilder code = new StringBuilder();
      int length;
      while ((length = r.read(buf)) > 0) {
        code.append(buf, 0, length);
      }

      addValue("Code", code.toString());

      String name = _file.getName();
      name = name.substring(0, name.lastIndexOf('.'));

      // regular expression for the package name
      Pattern pattern = Pattern.compile("package +[^;]+;");
      Matcher matcher = pattern.matcher(code);
      if (matcher.find()) {
        String pkg = matcher.group();
        pkg = pkg.replaceFirst("^(package) +", "");
        pkg = pkg.replaceFirst(";$", "");
        name = pkg + "." + name;
      }

      setName(name);
      this.classNameIsSet = true;
    }

    /**
     * The method overwrites the method from the super class, because Java
     * programs are searched by the name (and not by UUID like in the super
     * class).
     * 
     * @param _dataModelType
     *          instance of the type of the object which must be updated
     * @param _uuid
     *          uuid of the object to update
     * @param _allLinkTypes
     *          all link types to update
     */
    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes)
                                                         throws EFapsException,
                                                         Exception {
      if (!this.classNameIsSet) {
        setClassName(new File(this.rootPath + "/" + this.file));
      }
      Instance instance = null;
      Insert insert = null;

      // search for the instance
      SearchQuery query = new SearchQuery();
      query.setQueryTypes(_dataModelType.getName());
      query.addWhereExprEqValue("Name", getValue("Name"));
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        instance = new Instance((String) query.get("OID"));
      }
      query.close();

      // if no instance exists, a new insert must be done
      if (instance == null) {
        insert = new Insert(_dataModelType);
        insert.add("Name", getValue("Name"));
      }

      updateInDB(instance, _allLinkTypes, insert);
    }

    /**
     * This is the setter method for instance variable {@link #rootPath}.
     * 
     * @param _number
     *          new value for instance variable {@link #rootPath}
     * @see #rootPath
     */
    public void setRootPath(final String _rootPath) {
      this.rootPath = _rootPath;
    }

    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "file", this.file).append("rootPath", this.rootPath).toString();
    }
  }
}

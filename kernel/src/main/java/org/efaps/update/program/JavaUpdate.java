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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * The class updates java program from type <code>Admin_Program_Java</code>
 * inside the eFaps database.
 *
 * @author tmo
 * @version $Id$
 * @todo encoding from java files!
 */
public class JavaUpdate extends AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(JavaUpdate.class);

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to initiliase the type of Java programs.
   */
  public JavaUpdate() {
    super("Admin_Program_Java");
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Sets the root path in which the Class file is located. The value is set for
   * each single definition of the JavaUpdate.
   *
   * @param _root
   *                name of the path where the image file is located
   */
  protected void setRoot(final String _root) {
    for (DefinitionAbstract def : getDefinitions()) {
      ((JavaDefinition) def).setRoot(_root);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
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

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  /**
   * The Java definition holds the code and the name of the Java class.
   */
  public static class JavaDefinition extends DefinitionAbstract {

    // /////////////////////////////////////////////////////////////////////////
    // instance variables

    /**
     * Name of the Java file (without the path) to import.
     */
    private String file = null;

    /**
     * Name of the root path used to initialise the path for the Java File.
     */
    private String root = null;

    /**
     * Code of the Program is stored.
     */
    private StringBuilder code = null;

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Read the code from the file defined through filename.
     */
    private void readCode() throws IOException {
      final char[] buf = new char[1024];
      this.code = new StringBuilder();

      final InputStream input = new URL(this.root + this.file).openStream();

      final Reader reader = new InputStreamReader(input);
      int length;
      while ((length = reader.read(buf)) > 0) {
        this.code.append(buf, 0, length);
      }
      reader.close();
    }

    /**
     * This Method extracts the package name and sets the name of this Java
     * definition (the name is the package name together with the name of the
     * file exluding the <code>.java</code>).
     */
    private void setClassName() {

      String name = this.file.substring(0, this.file.lastIndexOf('.'));

      // regular expression for the package name
      final Pattern pattern = Pattern.compile("package +[^;]+;");
      final Matcher matcher = pattern.matcher(this.code);
      if (matcher.find()) {
        String pkg = matcher.group();
        pkg = pkg.replaceFirst("^(package) +", "");
        pkg = pkg.replaceFirst(";$", "");
        name = pkg + "." + name;
      }
      setName(name);
    }

    /**
     * The method overwrites the method from the super class, because Java
     * programs are searched by the name (and not by UUID like in the super
     * class).
     *
     * @param _dataModelType
     *                instance of the type of the object which must be updated
     * @param _uuid
     *                uuid of the object to update
     * @param _allLinkTypes
     *                all link types to update
     */
    @Override
    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes,
                           final boolean _abstractType) throws EFapsException,
                                                       Exception {

      readCode();
      setClassName();

      Instance instance = null;
      Insert insert = null;

      // search for the instance
      final SearchQuery query = new SearchQuery();
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

      instance = updateInDB(instance, _allLinkTypes, insert);

      // checkin source code
      final Checkin checkin = new Checkin(instance);
      checkin.executeWithoutAccessCheck(this.file, new ByteArrayInputStream(
          this.code.toString().getBytes("UTF8")), this.code.length());
    }

    // /////////////////////////////////////////////////////////////////////////
    // instance getter / setter methods

    /**
     * This is the setter method for instance variable {@link #file}.
     *
     * @param _number
     *                new value for instance variable {@link #file}
     * @see #file
     */
    public void setFile(final String _file) {
      this.file = _file;
    }

    /**
     * This is the setter method for instance variable {@link #root}.
     *
     * @param _root
     *                new value for instance variable {@link #root}
     * @see #root
     */
    public void setRoot(final String _root) {
      this.root = _root;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "file", this.file).append("root", this.root).toString();
    }
  }
}

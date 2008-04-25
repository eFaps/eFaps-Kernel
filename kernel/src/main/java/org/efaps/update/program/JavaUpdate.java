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

import java.net.URL;
import java.util.Set;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.ESJPImporter;
import org.efaps.util.EFapsException;

/**
 * The class updates java program from type <code>Admin_Program_Java</code>
 * inside the eFaps database.
 *
 * @author tmo
 * @version $Id$
 */
public class JavaUpdate extends AbstractSourceUpdate {

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to initialize the type of Java programs.
   */
  public JavaUpdate() {
    super("Admin_Program_Java");
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
  public static JavaUpdate readXMLFile(final URL _root, final URL _url) {
    final JavaUpdate ret = new JavaUpdate();
    ret.setURL(_url);
    final JavaDefinition definition = new JavaDefinition(_root, _url);
    ret.addDefinition(definition);

    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  /**
   * The Java definition holds the code and the name of the Java class.
   */
  public static class JavaDefinition extends SourceDefinition {

    ///////////////////////////////////////////////////////////////////////////
    // instance variables

    /**
     *
     */
    private ESJPImporter javaCode = null;

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * @param _rootUrl
     * @param _fileUrl
     */
    protected JavaDefinition(URL _rootUrl, URL _fileUrl) {
      super(_rootUrl, _fileUrl);
    }

    /**
     * Reads the Java source code which is in the path {@link #root} with file
     * name {@link #file}.
     *
     * @throws EFapsException if the Java source code could not be read or the
     *                        file could not be accessed because of the wrong
     *                        URL
     */
    @Override
    protected void searchInstance(final Type _dataModelType,
                                  final String _uuid)
        throws EFapsException
    {
      if (this.javaCode == null)  {
//        try {
          this.javaCode = new ESJPImporter(getUrl());
/*        } catch (MalformedURLException e) {
          throw new EFapsException(getClass(),
                                   "readJavaCode.MalformedURLException",
                                   e,
                                   getUrl().toString());
        }
*/      }
      setName(this.javaCode.getClassName());
if (this.javaCode.eFapsUUID != null)  {
      this.addValue("UUID", this.javaCode.eFapsUUID.toString());
}
      if (this.instance == null)  {
        this.instance = this.javaCode.searchInstance();
      }
    }

    /**
     * The method overwrites the method from the super class, because a checkin
     * of the Java source code is needed after the update in the database.
     *
     * @param _allLinkTypes   all link types to update
     */
    @Override
    public void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException
    {
      super.updateInDB(_allLinkTypes);
      this.javaCode.updateDB(this.instance);
    }
  }
}

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

package org.efaps.admin.program.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Delete;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * The class is used to compile all checked in Java programs. Because the
 * depencies of a class are not known, all Java programs stored in eFaps are
 * compiled.<br/> The compiler uses <a href="http://java.sun.com">Sun's Javac</a>.
 * 
 * @author tmo
 * @author jmo
 * @version $Id: Compiler.java 764 2007-04-07 14:07:50 +0000 (Sat, 07 Apr 2007)
 *          tmo $
 * @todo exception handling in the resource reader
 * 
 */
public class Compiler {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log        LOG      = LogFactory.getLog(Compiler.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Type instance of Java program.
   */
  private final Type              javaType;

  /**
   * Type instance of compile Java program.
   */
  private final Type              classType;

  /**
   * Mapping between Java file name and id of internal eFaps Java program.
   */
  private final Map<String, Long> file2id  = new HashMap<String, Long>();

  /**
   * Mapping between Java file name and compiled Java program.
   */
  private final Map<String, Long> class2id = new HashMap<String, Long>();

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * The constructor initiliase the two type instances {@link #javaType} and
   * {@link #classType}.
   * 
   * @see #javaType
   * @see #classType
   */
  public Compiler() {
    this.javaType = Type.get("Admin_Program_Java");
    this.classType = Type.get("Admin_Program_JavaClass");

  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * All stored Java programs in eFaps are compiled. Sun's javac is used for the
   * compilitation. All old not needed comiled Java classes are automatically
   * removed. The compiler error and warning are logged (errors are using
   * error-level, warnings are using info-level).
   * 
   * @see #readJavaPrograms
   * @see #readJavaClasses
   */
  public void compile() throws EFapsException {
    readJavaPrograms();
    readJavaClasses();

    ResourceReader reader = new EFapsResourceReader();
    ResourceStore store = new EFapsResourceStore(this);

    JavaCompiler compiler = new JavaCompilerFactory().createCompiler("javac");

    // all checked in files must be compiled!
    final String[] resource = file2id.keySet().toArray(
        new String[file2id.size()]);

    if (LOG.isInfoEnabled()) {
      for (int i = 0; i < resource.length; i++) {
        LOG.info("compiling " + resource[i]);
      }
    }

    final CompilationResult result = compiler.compile(resource, reader, store,
        Compiler.class.getClassLoader());

    for (Long id : this.class2id.values()) {
      (new Delete(this.classType, id)).executeWithoutAccessCheck();
    }

    if (result.getErrors().length > 0) {
      LOG.error(result.getErrors().length + " errors:");
      for (int i = 0; i < result.getErrors().length; i++) {
        LOG.error(result.getErrors()[i]);
      }
    }

    if (LOG.isInfoEnabled()) {
      if (result.getWarnings().length > 0) {
        LOG.info(result.getWarnings().length + " warnings:");
        for (int i = 0; i < result.getWarnings().length; i++) {
          LOG.info(result.getWarnings()[i]);
        }
      }
    }
  }

  /**
   * All Java programs in the eFaps database are read and stored in the mapping
   * {@link #file2id} for further using.
   * 
   * @see #file2id
   */
  protected void readJavaPrograms() throws EFapsException {
    SearchQuery query = new SearchQuery();
    query.setQueryTypes(this.javaType.getName());
    query.addSelect("ID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      String name = (String) query.get("Name");
      Long id = (Long) query.get("ID");
      File file = new File(File.separator
          + name.replaceAll("\\.", File.separator) + ".java");
      String absName = file.getAbsolutePath();
      this.file2id.put(absName, id);
    }
  }

  /**
   * All stored compiled Java clases in the eFaps database are stored in the
   * mapping {@link @class2id}. If a Java program is compiled and stored with
   * {@link EFapsResourceStore#write}, the class is removed. After the compile,
   * {@link #compile} removed all stored classes which are not needed anymore.
   * 
   * @see #class2id
   * @see FapsResourceStore#write
   * @see #compile
   */
  protected void readJavaClasses() throws EFapsException {
    SearchQuery query = new SearchQuery();
    query.setQueryTypes(this.classType.getName());
    query.addSelect("ID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      String name = (String) query.get("Name");
      Long id = (Long) query.get("ID");
      this.class2id.put(name, id);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods
  /**
   * get the Map containing the Mapping between Java file name and compiled Java
   * program.
   * 
   * @return Map
   */
  public Map<String, Long> getclass2id() {
    return this.class2id;
  }

  /**
   * get the Map containing the Mapping between Java file name and id of
   * internal eFaps Java program.
   * 
   * @return Map
   */
  public Map<String, Long> getfile2id() {
    return this.file2id;
  }

  /**
   * get the Type instance of compile Java program.
   * 
   * @return Type
   */
  public Type getclassType() {
    return this.classType;
  }

  /**
   * get the Type instance of Java program.
   * 
   * @return Type
   */
  public Type getJavaType() {
    return this.javaType;
  }

  /**
   * Reader class to read the source code within Java programs.
   */
  public class EFapsResourceReader implements ResourceReader {

    /**
     * The method checks if given resource name is avaible by eFaps.
     * 
     * @param _resourceName
     *          Java program name (as file name!)
     * @return <i>true</i> if Java program exists in eFaps, otherwise <i>false</i>
     *         is returned
     * @see Compiler#fileId
     */
    public boolean isAvailable(final String _resourceName) {
      return file2id.containsKey(_resourceName);
    }

    /**
     * The source code for given Java program (parameter <i>_resourceName</i>)
     * is returned.<br/> Because also compiled Java classes in the class path
     * must be readable, the related file is opened from the file system (if the
     * extension of the resource name ends with <code>.class</code>).
     * 
     * @param _resourceName
     *          Java program name (as file name!)
     * @return source code of the Java program
     * @see Compiler#fileId
     * @todo exception handling
     */
    public byte[] getBytes(final String _resourceName) {
      byte[] ret = null;
      int index = _resourceName.lastIndexOf('.');
      String extension = _resourceName.substring(index);

      if (".class".equals(extension)) {
        try {
          InputStream is = new FileInputStream(_resourceName);
          ret = new byte[is.available()];
          is.read(ret);
          is.close();
        } catch (IOException e) {
          LOG.error(e);
        }
      } else {
        try {
          SearchQuery query = new SearchQuery();
          query.setObject(javaType, file2id.get(_resourceName));
          query.addSelect("Code");
          query.executeWithoutAccessCheck();
          if (query.next()) {
            String code = (String) query.get("Code");
            ret = code.getBytes();
          }
          query.close();
        } catch (EFapsException e) {
          LOG.error(e);
        }
      }
      return ret;
    }
  }

}
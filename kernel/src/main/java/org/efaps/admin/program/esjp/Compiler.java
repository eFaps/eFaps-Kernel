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

package org.efaps.admin.program.esjp;

import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_JAVA;
import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_JAVACLASS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkout;
import org.efaps.db.Delete;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is used to compile all checked in Java programs. Because the
 * depencies of a class are not known, all Java programs stored in eFaps are
 * compiled.<br/>
 * Following compilers could be used and defined via the Java system property
 * <code>org.efaps.admin.program.java.Compiler.compiler</code>:
 * <ul>
 * <li>javac - uses <a href="http://java.sun.com">Sun's Javac</a>.</li>
 * <li>eclipse</li>
 * <li>groovy</li>
 * <li>rhino</li>
 * <li>janino</li>
 * </ul>
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 * @todo exception handling in the resource reader
 *
 */
public class Compiler {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Compiler.class);

  /**
   * Name of the system property to get the name of the used Java compiler.
   *
   * @see #compile
   */
  private static final String PROPERTY_COMPILER
          = "org.efaps.admin.program.java.Compiler.compiler";

  /**
   * Defines the default Java compiler used to compile Java code in eFaps.
   *
   * @see #compile
   */
  private static final String DEFAULT_COMPILER = "javac";

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Type instance of Java program.
   */
  private final Type esjpType;

  /**
   * Type instance of compile Java program.
   */
  private final Type classType;

  /**
   * Mapping between Java file name and id of internal eFaps Java program.
   */
  private final Map<String, Long> file2id  = new HashMap<String, Long>();

  /**
   * Mapping between Java file name and compiled Java program.
   */
  private final Map<String, Long> class2id = new HashMap<String, Long>();

  /**
   * Stores the list of classpath needed to compile (if needed).
   */
  private final List<String> classPathElements;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * The constructor initiliase the two type instances {@link #esjpType} and
   * {@link #classType}.
   *
   * @see #esjpType
   * @see #classType
   */
  public Compiler() {
    this(null);
  }

  /**
   * The constructor initiliase the two type instances {@link #esjpType} and
   * {@link #classType}.
   *
   * @param _classPathElements  list of class path elements
   * @see #esjpType
   * @see #classType
   */
  public Compiler(final List<String> _classPathElements) {
    this.esjpType = Type.get(ADMIN_PROGRAM_JAVA);
    this.classType = Type.get(ADMIN_PROGRAM_JAVACLASS);
    this.classPathElements = _classPathElements;
  }

  /////////////////////////////////////////////////////////////////////////////
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

    final ResourceReader reader = new EFapsResourceReader();
    final ResourceStore store = new EFapsResourceStore(this);

    final String compName = System.getProperty(PROPERTY_COMPILER, DEFAULT_COMPILER);
    final JavaCompiler compiler = new JavaCompilerFactory().createCompiler(compName);

    if (compiler == null)  {
      LOG.error("no compiler found for compiler " + compName + "!");
      LOG.error("please define system property " + PROPERTY_COMPILER);
    } else  {
      // output of used compiler
      if (LOG.isInfoEnabled()) {
        LOG.info("using compiler " + compName);
      }

      // all checked in files must be compiled!
      final String[] resource
            = this.file2id.keySet().toArray(new String[this.file2id.size()]);
      String[] args = resource;

      // if javac compiler then set classpath!
      // (the list of programs to compile is given to the javac as argument
      // array, so the classpath could be set in front of the programs to
      // compile)
      if (DEFAULT_COMPILER.equals(compName) && (this.classPathElements != null))  {
        args = new String[resource.length + 2];
        for (int i = 0; i < resource.length; i++) {
          args[i + 2] = resource[i];
        }

        final String sep = System.getProperty("os.name").startsWith("Windows")
                           ? ";"
                           : ":";

        final StringBuilder classPath = new StringBuilder();
        for (final String classPathElement : this.classPathElements)  {
          classPath.append(classPathElement).append(sep);
        }

        args[0] = "-classpath";
        args[1] = classPath.toString();
      }

      if (LOG.isInfoEnabled()) {
        for (int i = 0; i < resource.length; i++) {
          LOG.info("compiling " + resource[i]);
        }
      }

      final CompilationResult result = compiler.compile(args, reader, store,
          Compiler.class.getClassLoader());

      for (final Long id : this.class2id.values()) {
        (new Delete(this.classType, id)).executeWithoutAccessCheck();
      }

      if (result.getErrors().length > 0) {
        LOG.error(result.getErrors().length + " errors:");
        for (int i = 0; i < result.getErrors().length; i++) {
          LOG.error(result.getErrors()[i].toString());
        }
      }

      if (LOG.isInfoEnabled()) {
        if (result.getWarnings().length > 0) {
          LOG.info(result.getWarnings().length + " warnings:");
          for (int i = 0; i < result.getWarnings().length; i++) {
            LOG.info(result.getWarnings()[i].toString());
          }
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
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(this.esjpType.getName());
    query.addSelect("ID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String name = (String) query.get("Name");
      final Long id = (Long) query.get("ID");
      final File file = new File(File.separator
          + name.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".java");
      final String absName = file.getAbsolutePath();
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
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(this.classType.getName());
    query.addSelect("ID");
    query.addSelect("Name");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      final String name = (String) query.get("Name");
      final Long id = (Long) query.get("ID");
      this.class2id.put(name, id);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
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
  public Type getClassType() {
    return this.classType;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class EFapsResourceReader

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
      return Compiler.this.file2id.containsKey(_resourceName);
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
      final int index = _resourceName.lastIndexOf('.');
      final String extension = _resourceName.substring(index);
      final String resourceName = (new File(_resourceName)).getAbsolutePath();
      if (".class".equals(extension)) {
        try {
          final InputStream is = new FileInputStream(_resourceName);
          ret = new byte[is.available()];
          is.read(ret);
          is.close();
        } catch (final IOException e) {
          LOG.error("compile error", e);
        }
      } else {
        try {
          final Checkout checkout = new Checkout(new Instance(Compiler.this.esjpType,
                                                        Compiler.this.file2id.get(resourceName)));
          final InputStream is = checkout.executeWithoutAccessCheck();
          ret = new byte[is.available()];
          is.read(ret);
          is.close();
        } catch (final IOException e) {
          LOG.error("compile error", e);
        } catch (final EFapsException e) {
          LOG.error("compile error", e);
        }
      }
      return ret;
    }
  }
}
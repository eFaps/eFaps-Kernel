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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is used to compile all checked in ESJP programs. Because the
 * dependencies of a class are not known, all ESJP programs stored in eFaps are
 * compiled.
 *
 * @author The eFaps Team
 * @version $Id$
 * @todo exception handling in the resource reader
 *
 */
public class Compiler
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Compiler.class);

    /**
     * Type instance of Java program.
     */
    private final Type esjpType;

    /**
     * Type instance of compile EJSP program.
     */
    private final Type classType;

    /**
     * Mapping between ESJP name and the related ESJP source object.
     *
     * @see #readJavaPrograms()
     */
    private final Map<String, ESJPSourceObject> file2id  = new HashMap<String, ESJPSourceObject>();

    /**
     * Mapping between already existing compiled ESJP class name and the
     * related eFaps id in the database.
     *
     * @see #readJavaClasses()
     */
    private final Map<String, Long> class2id = new HashMap<String, Long>();

    /**
     * Mapping between the class name and the related ESJP class which must be
     * stored.
     */
    private final Map<String, Compiler.ESJPStoreObject> classFiles
        = new HashMap<String, Compiler.ESJPStoreObject>();

    /**
     * Stores the list of class path needed to compile (if needed).
     */
    private final List<String> classPathElements;

    /**
     * The constructor initialize the two type instances {@link #esjpType} and
     * {@link #classType}.
     *
     * @param _classPathElements  list of class path elements
     * @see #esjpType
     * @see #classType
     */
    public Compiler(final List<String> _classPathElements)
    {
        this.esjpType = Type.get(EFapsClassNames.ADMIN_PROGRAM_JAVA);
        this.classType = Type.get(EFapsClassNames.ADMIN_PROGRAM_JAVACLASS);
        this.classPathElements = _classPathElements;
    }

  /**
   * All stored Java programs in eFaps are compiled. Sun's javac is used for the
   * compilitation. All old not needed comiled Java classes are automatically
   * removed. The compiler error and warning are logged (errors are using
   * error-level, warnings are using info-level).
   *
   * @see #readJavaPrograms
   * @see #readJavaClasses
   */
    public void compile()
        throws EFapsException
    {
        readJavaPrograms();
        readJavaClasses();

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();


        if (compiler == null)  {
            Compiler.LOG.error("no compiler found for compiler !");
        } else  {
            // output of used compiler
            if (Compiler.LOG.isInfoEnabled()) {
                Compiler.LOG.info("    Using compiler " + compiler.toString());
            }

            // options for the compiler
            final List<String> optionList = new ArrayList<String>();

            // set classpath!
            // (the list of programs to compile is given to the javac as argument
            // array, so the classpath could be set in front of the programs to
            // compile)
            if (this.classPathElements != null)  {
                // different class path separators depending on the OS
                final String sep = System.getProperty("os.name").startsWith("Windows") ? ";" : ":";

                final StringBuilder classPath = new StringBuilder();
                for (final String classPathElement : this.classPathElements)  {
                    classPath.append(classPathElement).append(sep);
                }
                optionList.addAll(Arrays.asList("-classpath", classPath.toString()));
            } else  {
                // set compiler's class path to be same as the runtime's
                optionList.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
            }

            // logging of compiling classes
            if (Compiler.LOG.isInfoEnabled()) {
                for (final ESJPSourceObject obj : this.file2id.values()) {
                    Compiler.LOG.info("    Compiling " + obj.javaName);
                }
            }

            final ESJPFileManager fm = new ESJPFileManager(compiler.getStandardFileManager(null, null, null));
            final boolean noErrors = compiler.getTask(new ErrorWriter(),
                                                      fm,
                                                      null,
                                                      optionList,
                                                      null,
                                                      this.file2id.values())
                                             .call();

            if (!noErrors)  {
                throw new Error("error");
            }

            // store all compiled ESJP's
            for (final Compiler.ESJPStoreObject obj : this.classFiles.values())  {
                obj.write();
            }

            // delete not needed compiled ESJP classes
            for (final Long id : this.class2id.values()) {
                (new Delete(this.classType, id)).executeWithoutAccessCheck();
            }
        }
    }

    /**
     * All Java programs in the eFaps database are read and stored in the
     * mapping {@link #file2id} for further using.
     *
     * @see #file2id
     * @throws EFapsException if ESJP Java programs could not be read
     */
    protected void readJavaPrograms()
        throws EFapsException
    {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes(this.esjpType.getName());
        query.addSelect("ID");
        query.addSelect("Name");
        query.executeWithoutAccessCheck();
        while (query.next()) {
            final String name = (String) query.get("Name");
            final Long id = (Long) query.get("ID");
            final File file = new File(File.separator,
                                       name.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + ".java");
            try {
                this.file2id.put(name, new ESJPSourceObject(name, file, id));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * All stored compiled ESJP's classes in the eFaps database are stored in
     * the mapping {@link @class2id}. If a ESJP's program is compiled and
     * stored with {@link ESJPStoreObject#write()}, the class is removed.
     * After the compile, {@link #compile()} removed all stored classes which
     * are not needed anymore.
     *
     * @throws EFapsException if read of the Java classes failed
     * @see #class2id
     */
    protected void readJavaClasses()
        throws EFapsException
    {
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

    /**
     * Error writer to show all errors to the
     * {@link Compiler#LOG compiler logger}.
     */
    private final class ErrorWriter
        extends Writer
    {
        /**
         * Stub method because only required to derive from {@link Writer}.
         */
        @Override()
        public void close()
        {
        }

        /**
         * Stub method because only required to derive from {@link Writer}.
         */
        @Override
        public void flush()
        {
        }

        /**
         * Writes given message to the error log of the compiler.
         *
         * @param _cbuf     buffer with the message
         * @param _off      offset within the buffer
         * @param _len      len of the message within the buffer
         */
        @Override()
        public void write(final char[] _cbuf,
                          final int _off,
                          final int _len)
        {
            final String msg = new StringBuilder().append(_cbuf, _off, _len).toString().trim();
            if (!"".equals(msg))  {
                for (final String line : msg.split("\n"))  {
                    Compiler.LOG.error(line);
                }
            }
        }

    }

    /**
     * ESJP file manager to handle the compiled ESJP classes.
     */
    private final class ESJPFileManager
        extends ForwardingJavaFileManager<StandardJavaFileManager>
    {
        /**
         * Defined the forwarding Java file manager.
         *
         * @param _sfm      original Java file manager to forward
         */
        public ESJPFileManager(final StandardJavaFileManager _sfm)
        {
            super(_sfm);
        }

        /**
         * The method returns always <code>null</code> to be sure the no file
         * is written.
         *
         * @param _location     location for which the file output is searched
         * @param _packageName  name of the package
         * @param _relativeName relative name
         * @param _fileObject   file object to be used as hint for placement
         * @return always <code>null</code>
         */
        @Override()
        public FileObject getFileForOutput(final Location _location,
                                           final String _packageName,
                                           final String _relativeName,
                                           final FileObject _fileObject)
        {
            return null;
        }

        @Override()
        public JavaFileObject getJavaFileForOutput(final Location _location,
                                                   final String _className,
                                                   final Kind _kind,
                                                   final FileObject _fileobject)
        {
            final Compiler.ESJPStoreObject ret = new Compiler.ESJPStoreObject(_fileobject.toUri(), _className);
            Compiler.this.classFiles.put(_className, ret);
            return ret;
        }

        /**
         * Checks if given <code>_location</code> is handled by this Java file
         * manager.
         *
         * @param _location     location to prove
         * @return <i>true</i> if the <code>_location</code> is the source path or
         *         the forwarding standard Java file manager handles the
         *         <code>_location</code>; otherwise <i>false</i>
         */
        @Override()
        public boolean hasLocation(final JavaFileManager.Location _location)
        {
            return StandardLocation.SOURCE_PATH.getName().equals(_location.getName()) || super.hasLocation(_location);
        }

        @Override()
        public String inferBinaryName(final JavaFileManager.Location _location,
                                      final JavaFileObject _javafileobject)
        {
            final String ret;
            if (StandardLocation.SOURCE_PATH.getName().equals(_location.getName()))  {
                ret = new StringBuilder()
                        .append(_javafileobject.getName())
                        .append(JavaFileObject.Kind.CLASS.extension)
                        .toString();
            } else  {
                ret = super.inferBinaryName(_location, _javafileobject);
            }
            return ret;
        }

        @Override
        public Iterable<JavaFileObject> list(final Location _location,
                                             final String _packageName,
                                             final Set<JavaFileObject.Kind> _kinds,
                                             final boolean _recurse)
            throws IOException
        {
            final Iterable<JavaFileObject> rt;
            if (StandardLocation.SOURCE_PATH.getName().equals(_location.getName())
                    && _kinds.contains(JavaFileObject.Kind.SOURCE))  {
                final List<JavaFileObject> pckObjs = new ArrayList<JavaFileObject>();
                final int pckLength = _packageName.length();
                for (final Map.Entry<String, Compiler.ESJPSourceObject> entry : Compiler.this.file2id.entrySet())  {
                    if (entry.getKey().startsWith(_packageName)
                            && (entry.getKey().substring(pckLength + 1).indexOf('.') < 0))  {
                        pckObjs.add(entry.getValue());
                    }
                }
                rt = pckObjs;
            } else  {
                rt = super.list(_location, _packageName, _kinds, _recurse);
            }
            return rt;
        }
    }

    public class ESJPSourceObject
        extends SimpleJavaFileObject
    {
        /**
         * Name of the ESJP program.
         */
        private final String javaName;

        /**
         * Used internal id in eFaps.
         */
        private final long id;

        private ESJPSourceObject(final String _javaName,
                                 final File _file,
                                 final long _id)
            throws URISyntaxException
        {
            super(new URI("efaps", null, _file.getAbsolutePath(), null, null),
                  JavaFileObject.Kind.SOURCE);
            this.javaName = _javaName;
            this.id = _id;
        }

        /**
         * Returns the Java name of this ESJP source object.
         *
         * @return Java name
         * @see #javaName
         */
        public String getJavaName()
        {
            return this.javaName;
        }

        /**
         * Returns the eFaps id of the ESJP source object.
         *
         * @return id of the ESJP source object
         * @see #id
         */
        public long getId()
        {
            return this.id;
        }

        @Override()
        public CharSequence getCharContent(final boolean _arg0)
            throws IOException
        {
            final StringBuilder ret = new StringBuilder();
            try {
                final Checkout checkout = new Checkout(Instance.get(Compiler.this.esjpType, this.id));
                final InputStream is = checkout.executeWithoutAccessCheck();
                final byte[] bytes = new byte[is.available()];
                is.read(bytes);
                is.close();
                ret.append(new String(bytes));
            } catch (final EFapsException e) {
                throw new IOException("could not checkout class '" + this.javaName + "'", e);
            }
            return ret;
        }
    }

    /**
     * The class is used to store the result of a Java compilation.
     */
    private final class ESJPStoreObject
        extends SimpleJavaFileObject
    {
        /**
         * Name of the class to compile.
         */
        private final String className;

        /**
         * Byte array output stream to store the result of the compilation.
         *
         * @see #openOutputStream()
         */
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        /**
         *
         * @param _uri          URI of the class to store
         * @param _className    name of the class to store
         */
        private ESJPStoreObject(final URI _uri,
                                final String _className)
        {
            super(_uri, JavaFileObject.Kind.CLASS);
            this.className = _className;
        }

        /**
         *
         * @return {@link #out} as output stream
         */
        @Override()
        public OutputStream openOutputStream()
        {
            return this.out;
        }

        /**
         * The compiled class in <i>_resourceData</i> is stored with the name
         * <i>_resourceName</i> in the eFaps database (checked in). If the class
         * instance already exists in eFaps, the class data is updated. Otherwise, the
         * compiled class is new inserted in eFaps (related to the original Java
         * program).
         */
        public void write()
        {
            if (Compiler.LOG.isDebugEnabled()) {
                Compiler.LOG.debug("write '" + this.className + "'");
            }
            try {
                final Long id = Compiler.this.class2id.get(this.className);
                Instance instance;
                if (id == null) {
                    final String parent = this.className.replaceAll(".class$", "").replaceAll("\\$.*", "");

                    final Compiler.ESJPSourceObject parentId = Compiler.this.file2id.get(parent);

                    final Insert insert = new Insert(Compiler.this.classType);
                    insert.add("Name", this.className);
                    insert.add("ProgramLink", "" + parentId.getId());
                    insert.executeWithoutAccessCheck();
                    instance = insert.getInstance();
                    insert.close();
                } else {
                    instance = Instance.get(Compiler.this.classType, id);
                    Compiler.this.class2id.remove(this.className);
                }

                final Checkin checkin = new Checkin(instance);
                checkin.executeWithoutAccessCheck(this.className,
                                                  new ByteArrayInputStream(this.out.toByteArray()),
                                                  this.out.toByteArray().length);
            } catch (final Exception e) {
                Compiler.LOG.error("unable to write to eFaps ESJP class '" + this.className + "'", e);
            }
        }
    }
}
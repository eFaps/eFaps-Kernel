/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.update.schema.program.esjp;

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

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.update.util.InstallationException;
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
 */
public class ESJPCompiler
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ESJPCompiler.class);

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
     * @see #readESJPPrograms()
     */
    private final Map<String, SourceObject> name2Source  = new HashMap<String, SourceObject>();

    /**
     * Mapping between already existing compiled ESJP class name and the
     * related eFaps id in the database.
     *
     * @see #readESJPClasses()
     */
    private final Map<String, Long> class2id = new HashMap<String, Long>();

    /**
     * Mapping between the class name and the related ESJP class which must be
     * stored.
     *
     * @see StoreObject
     */
    private final Map<String, ESJPCompiler.StoreObject> classFiles
        = new HashMap<String, ESJPCompiler.StoreObject>();

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
    public ESJPCompiler(final List<String> _classPathElements)
    {
        this.esjpType = CIAdminProgram.Java.getType();
        this.classType = CIAdminProgram.JavaClass.getType();
        this.classPathElements = _classPathElements;
    }

  /**
   * All stored ESJP programs in eFaps are compiled. The system Java compiler
   * defined from the {@link ToolProvider tool provider} is used for the
   * compiler. All old not needed compiled Java classes are automatically
   * removed. The compiler error and warning are logged (errors are using
   * error-level, warnings are using info-level).<br>
   * Debug:<br>
   * <ul>
   * <li><code>null</code>: By default, only line number and source file information is generated.</li>
   * <li><code>"none"</code>: Do not generate any debugging information</li>
   * <li>Generate only some kinds of debugging information, specified by a comma separated
   * list of keywords. Valid keywords are:
   * <ul>
   * <li><code>"source"</code>: Source file debugging information</li>
   * <li><code>"lines"</code>: Line number debugging information</li>
   * <li><code>"vars"</code>: Local variable debugging information</li>
   * </ul>
   * </li>
   * </ul>
   *
   * @param _debug                  String for the debug option
   * @param _addRuntimeClassPath    Must the classpath from the runtime added
   *                                to the compiler, default: <code>false</code>
   * @throws InstallationException if the compile failed
   */
    public void compile(final String _debug,
                        final boolean _addRuntimeClassPath)
        throws InstallationException
    {
        readESJPPrograms();
        readESJPClasses();

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null)  {
            ESJPCompiler.LOG.error("no compiler found for compiler !");
        } else  {
            // output of used compiler
            if (ESJPCompiler.LOG.isInfoEnabled()) {
                ESJPCompiler.LOG.info("    Using compiler " + compiler.toString());
            }

            // options for the compiler
            final List<String> optionList = new ArrayList<String>();

            // set classpath!
            // (the list of programs to compile is given to the javac as
            // argument array, so the class path could be set in front of the
            // programs to compile)
            if (this.classPathElements != null)  {
                // different class path separators depending on the OS
                final String sep = System.getProperty("os.name").startsWith("Windows") ? ";" : ":";

                final StringBuilder classPath = new StringBuilder();
                for (final String classPathElement : this.classPathElements)  {
                    classPath.append(classPathElement).append(sep);
                }
                if (_addRuntimeClassPath) {
                    classPath.append(System.getProperty("java.class.path"));
                }
                optionList.addAll(Arrays.asList("-classpath", classPath.toString()));
            } else  {
                // set compiler's class path to be same as the runtime's
                optionList.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
            }
            //Set the source file encoding name, such as EUCJIS/SJIS. If -encoding is not specified,
            //the platform default converter is used.
            optionList.addAll(Arrays.asList("-encoding", "UTF-8"));

            if (_debug != null) {
                optionList.addAll(Arrays.asList("-g", _debug));
            }

            // logging of compiling classes
            if (ESJPCompiler.LOG.isInfoEnabled()) {
                for (final SourceObject obj : this.name2Source.values()) {
                    ESJPCompiler.LOG.info("    Compiling ESJP '" + obj.javaName + "'");
                }
            }

            final FileManager fm = new FileManager(compiler.getStandardFileManager(null, null, null));
            final boolean noErrors = compiler.getTask(new ErrorWriter(),
                                                      fm,
                                                      null,
                                                      optionList,
                                                      null,
                                                      this.name2Source.values())
                                             .call();

            if (!noErrors)  {
                throw new InstallationException("error");
            }

            // store all compiled ESJP's
            for (final ESJPCompiler.StoreObject obj : this.classFiles.values())  {
                obj.write();
            }

            // delete not needed compiled ESJP classes
            for (final Long id : this.class2id.values()) {
                try {
                    (new Delete(this.classType, id)).executeWithoutAccessCheck();
                } catch (final EFapsException e)  {
                    throw new InstallationException("Could not delete ESJP class with id " + id, e);
                }
            }
        }
    }

    /**
     * All EJSP programs in the eFaps database are read and stored in the
     * mapping {@link #name2Source} for further using.
     *
     * @see #name2Source
     * @throws InstallationException if ESJP Java programs could not be read
     */
    protected void readESJPPrograms()
        throws InstallationException
    {
        try  {
            final QueryBuilder queryBldr = new QueryBuilder(this.esjpType);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute("Name");
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final String name = multi.<String>getAttribute("Name");
                final Long id = multi.getCurrentInstance().getId();
                final File file = new File(File.separator,
                                           name.replaceAll("\\.", Matcher.quoteReplacement(File.separator))
                                                   + JavaFileObject.Kind.SOURCE.extension);
                final URI uri;
                try {
                    uri = new URI("efaps", null, file.getAbsolutePath(), null, null);
                } catch (final URISyntaxException e) {
                    throw new InstallationException("Could not create an URI for " + file, e);
                }
                this.name2Source.put(name, new SourceObject(uri, name, id));
            }
        } catch (final EFapsException e) {
            throw new InstallationException("Could not fetch the information about installed ESJP's", e);
        }
    }

    /**
     * All stored compiled ESJP's classes in the eFaps database are stored in
     * the mapping {@link #class2id}. If a ESJP's program is compiled and
     * stored with {@link ESJPCompiler.StoreObject#write()}, the class is
     * removed. After the compile, {@link ESJPCompiler#compile(String)} removes
     * all stored classes which are not needed anymore.
     *
     * @throws InstallationException if read of the ESJP classes failed
     * @see #class2id
     */
    protected void readESJPClasses()
        throws InstallationException
    {
        try  {
            final QueryBuilder queryBldr = new QueryBuilder(this.classType);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute("Name");
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final String name = multi.<String>getAttribute("Name");
                final Long id = multi.getCurrentInstance().getId();
                this.class2id.put(name, id);
            }
        } catch (final EFapsException e) {
            throw new InstallationException("Could not fetch the information about compiled ESJP's", e);
        }
    }

    /**
     * Error writer to show all errors to the
     * {@link ESJPCompiler#LOG compiler logger}.
     */
    private final class ErrorWriter
        extends Writer
    {
        /**
         * Stub method because only required to derive from {@link Writer}.
         */
        @Override
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
        @Override
        public void write(final char[] _cbuf,
                          final int _off,
                          final int _len)
        {
            final String msg = new StringBuilder().append(_cbuf, _off, _len).toString().trim();
            if (!"".equals(msg))  {
                for (final String line : msg.split("\n"))  {
                    ESJPCompiler.LOG.error(line);
                }
            }
        }

    }

    /**
     * ESJP file manager to handle the compiled ESJP classes.
     */
    private final class FileManager
        extends ForwardingJavaFileManager<StandardJavaFileManager>
    {
        /**
         * Defined the forwarding Java file manager.
         *
         * @param _sfm      original Java file manager to forward
         */
        public FileManager(final StandardJavaFileManager _sfm)
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
        @Override
        public FileObject getFileForOutput(final Location _location,
                                           final String _packageName,
                                           final String _relativeName,
                                           final FileObject _fileObject)
        {
            return null;
        }

        /**
         * Returns the related Java file object used from the Java compiler to
         * store the compiled ESJP.
         *
         * @param _location     location (not used)
         * @param _className    name of the ESJP class
         * @param _kind         kind of the source (not used)
         * @param _fileObject   file object to update (used to get the URI)
         * @return Java file object for ESJP used to store the compiled class
         * @see ESJPCompiler
         */
        @Override
        public JavaFileObject getJavaFileForOutput(final Location _location,
                                                   final String _className,
                                                   final JavaFileObject.Kind _kind,
                                                   final FileObject _fileObject)
        {
            final ESJPCompiler.StoreObject ret = new ESJPCompiler.StoreObject(_fileObject.toUri(), _className);
            ESJPCompiler.this.classFiles.put(_className, ret);
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
        @Override
        public boolean hasLocation(final JavaFileManager.Location _location)
        {
            return StandardLocation.SOURCE_PATH.getName().equals(_location.getName()) || super.hasLocation(_location);
        }

        /**
         * If the <code>_location</code> is the source path a dummy binary name
         * for the ESJP class is returned. The dummy binary name is the name of
         * the <code>_javaFileObject</code> and the extension for
         * {@link JavaFileObject.Kind#CLASS Java classes}. If the
         * <code>_location</code> is not the source path, the binary name from
         * the forwarded
         * {@link StandardJavaFileManager standard Java file manager} is
         * returned.
         *
         * @param _location         location
         * @param _javaFileObject   java file object
         * @return name of the binary object for the ESJP or from forwarded
         *         {@link StandardJavaFileManager standard Java file manager}
         */
        @Override
        public String inferBinaryName(final JavaFileManager.Location _location,
                                      final JavaFileObject _javaFileObject)
        {
            final String ret;
            if (StandardLocation.SOURCE_PATH.getName().equals(_location.getName()))  {
                ret = new StringBuilder()
                        .append(_javaFileObject.getName())
                        .append(JavaFileObject.Kind.CLASS.extension)
                        .toString();
            } else  {
                ret = super.inferBinaryName(_location, _javaFileObject);
            }
            return ret;
        }

        /**
         * <p>If the <code>_location</code> is the source path and the
         * <code>_kinds</code> includes sources an investigation in the cached
         * {@link ESJPCompiler#name2Source ESJP programs} is done and the list of
         * ESJP's for given <code>_packageName</code> is returned.</p>
         * <p>In all other case the list of found Java programs from the
         * forwarded {@link StandardJavaFileManager standard Java file manager}
         * is returned.</p>
         *
         * @param _location     location which must be investigated
         * @param _packageName  name of searched package
         * @param _kinds        kinds of file object
         * @param _recurse      must be searched recursive including sub
         *                      packages (ignored, because not used)
         * @return list of found ESJP programs for given
         *         <code>_packageName</code> or if not from source path the
         *         list of Java classes from forwarded standard Java file
         *         manager
         * @throws IOException from forwarded standard Java file manager
         */
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
                for (final Map.Entry<String, ESJPCompiler.SourceObject> entry
                        : ESJPCompiler.this.name2Source.entrySet())  {

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

    /**
     * Holds the information about the ESJP source program which must be
     * compiled (and from which the source code is fetched).
     */
    private final class SourceObject
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

        /**
         * Initializes the source object.
         *
         * @param _uri          URI of the ESJP
         * @param _javaName     Java name of the ESJP
         * @param _id           id used from eFaps within database
         */
        private SourceObject(final URI _uri,
                             final String _javaName,
                             final long _id)
        {
            super(_uri, JavaFileObject.Kind.SOURCE);
            this.javaName = _javaName;
            this.id = _id;
        }

        /**
         * Returns the char sequence of the ESJP source code.
         *
         * @param _ignoreEncodingErrors     ignore encoding error (not used)
         * @return source code from the ESJP
         * @throws IOException if source could not be read from the eFaps
         *                     database
         */
        @Override
        public CharSequence getCharContent(final boolean _ignoreEncodingErrors)
            throws IOException
        {
            final StringBuilder ret = new StringBuilder();
            try {
                final Checkout checkout = new Checkout(Instance.get(ESJPCompiler.this.esjpType, this.id));
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
    private final class StoreObject
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
         * Initializes this store object.
         *
         * @param _uri          URI of the class to store
         * @param _className    name of the class to store
         */
        private StoreObject(final URI _uri,
                            final String _className)
        {
            super(_uri, JavaFileObject.Kind.CLASS);
            this.className = _className;
        }

        /**
         * Returns this {@link #out} which is used as buffer for the compiled
         * ESJP {@link #className}.
         *
         * @return {@link #out} as output stream
         * @see #out
         */
        @Override
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
            if (ESJPCompiler.LOG.isDebugEnabled()) {
                ESJPCompiler.LOG.debug("write '" + this.className + "'");
            }
            try {
                final Long id = ESJPCompiler.this.class2id.get(this.className);
                Instance instance;
                if (id == null) {
                    final String parent = this.className.replaceAll(".class$", "").replaceAll("\\$.*", "");

                    final ESJPCompiler.SourceObject parentId = ESJPCompiler.this.name2Source.get(parent);

                    final Insert insert = new Insert(ESJPCompiler.this.classType);
                    insert.add("Name", this.className);
                    insert.add("ProgramLink", "" + parentId.id);
                    insert.executeWithoutAccessCheck();
                    instance = insert.getInstance();
                    insert.close();
                } else {
                    instance = Instance.get(ESJPCompiler.this.classType, id);
                    ESJPCompiler.this.class2id.remove(this.className);
                }

                final Checkin checkin = new Checkin(instance);
                checkin.executeWithoutAccessCheck(this.className,
                                                  new ByteArrayInputStream(this.out.toByteArray()),
                                                  this.out.toByteArray().length);
                //CHECKSTYLE:OFF
            } catch (final Exception e) {
              //CHECKSTYLE:ON
                ESJPCompiler.LOG.error("unable to write to eFaps ESJP class '" + this.className + "'", e);
            }
        }
    }
}

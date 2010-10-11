/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.update.version;

import static org.efaps.admin.EFapsClassNames.ADMIN_COMMON_VERSION;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.tools.ant.DirectoryScanner;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.schema.program.esjp.ESJPCompiler;
import org.efaps.update.schema.program.staticsource.AbstractStaticSourceCompiler;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public final class Application
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * Default Mapping of a a file extension to a Type for import and update.
     */
    private static final Map<String, String> DEFAULT_TYPE_MAPPING = new HashMap<String, String>();
    static {
        Application.DEFAULT_TYPE_MAPPING.put("css", FileType.CSS.getType());
        Application.DEFAULT_TYPE_MAPPING.put("java", FileType.JAVA.getType());
        Application.DEFAULT_TYPE_MAPPING.put("js", FileType.JS.getType());
        Application.DEFAULT_TYPE_MAPPING.put("jrxml", FileType.JRXML.getType());
        Application.DEFAULT_TYPE_MAPPING.put("wiki", FileType.WIKI.getType());
        Application.DEFAULT_TYPE_MAPPING.put("xml", FileType.XML.getType());
        Application.DEFAULT_TYPE_MAPPING.put("xsl", FileType.XSL.getType());
    }

    /**
     * Default list of includes used to evaluate the files to copy.
     *
     * @see #getFiles
     */
    private static final Set<String> DEFAULT_INCLUDES = new HashSet<String>();
    static {
        Application.DEFAULT_INCLUDES.add("**/*.css");
        Application.DEFAULT_INCLUDES.add("**/*.java");
        Application.DEFAULT_INCLUDES.add("**/*.js");
        Application.DEFAULT_INCLUDES.add("**/*.jrxml");
        Application.DEFAULT_INCLUDES.add("**/*.wiki");
        Application.DEFAULT_INCLUDES.add("**/*.xml");
        Application.DEFAULT_INCLUDES.add("**/*.xsl");
    }

    /**
     * Default list of excludes used to evaluate the files to copy.
     *
     * @see #getFiles
     */
    private static final Set<String> DEFAULT_EXCLUDES = new HashSet<String>();
    static {
        Application.DEFAULT_EXCLUDES.add("**/versions.xml");
    }

    /**
     * Stores the name of the application.
     *
     * @see #setApplication
     */
    private String application = null;

    /**
     * Stores all versions of this application which must be installed.
     *
     * @see #getVersions()
     */
    private final Set<ApplicationVersion> versions = new TreeSet<ApplicationVersion>();

    /**
     * Install instance holding all XML definition / update files.
     *
     * @see #addURL(URL, String)
     */
    private final Install install = new Install();

    /**
     * Caches not stores versions (because if the kernel install is made, the
     * version could not be updated till the SQL tables and the data model is
     * already installed and the cache is reloaded).
     */
    private final List<Long> notStoredVersions = new ArrayList<Long>();

    /**
     * Stores the highest or maximum number of the versions to be installed.
     */
    private Long maxVersion;

    /**
     * Project class path.
     *
     * @see #Application(URL, List)
     * @see #getClassPathElements()
     */
    private final List<String> classpathElements;

    /**
     * Dependencies to other applications for this application ordered by the
     * key (which represents the order number of the dependency).
     *
     * @see #defineDependency(String, String, String)
     */
    private final Map<Integer, Dependency> dependencies = new TreeMap<Integer, Dependency>();

    /**
     * Root URL where the source files are located. Could be a file directory (
     * for local installation) or a jar file.
     *
     * @see #Application(URL, List)
     * @see #getRootUrl()
     */
    private final URL rootUrl;

    /**
     * Stores the name of the rootPackage.
     */
    private String rootPackageName;

    /**
     * Initializes the {@link #rootUrl root URL} of this application.
     *
     * @param _rootUrl root URL of the source
     * @param _classpathElements elements of the class path
     * @see #rootUrl
     */
    private Application(final URL _rootUrl,
                        final List<String> _classpathElements)
    {
        this.rootUrl = _rootUrl;
        this.classpathElements = _classpathElements;
    }

    /**
     * <code>null</code> is returned, of the version file could not be opened
     * and read.
     *
     * @param _versionUrl URL of the version file which defines the application
     * @param _rootUrl root URL where the source files are located (for local
     *            files); URL of the class file (if source is in a Jar file)
     * @param _classpathElements elements of the class path
     * @return application instance with all version information
     * @throws InstallationException if version XML file could not be parsed
     *             TODO: description TODO: better definition of include dir /
     *             file
     */
    public static Application getApplication(final URL _versionUrl,
                                             final URL _rootUrl,
                                             final List<String> _classpathElements)
        throws InstallationException
    {
        Application appl = null;
        try {
            final Digester digester = new Digester();
            digester.setValidating(false);

            digester.addRule("install", new Rule() {

                /**
                 * Process the beginning of this element.
                 *
                 * @param attributes The attribute list of this element
                 */
                @Override()
                public void begin(final Attributes _attributes)
                                            {
                    this.digester.push(new Application(_rootUrl, _classpathElements));
                }

                /**
                 * Process the end of this element.
                 */
                @Override()
                public void end()
                                            {
                    this.digester.pop();
                }
            });

            digester.addCallMethod("install/application", "setApplication", 1);
            digester.addCallParam("install/application", 0);

            digester.addCallMethod("install/rootPackage", "setRootPackageName", 1);
            digester.addCallParam("install/rootPackage", 0, "name");

            digester.addCallMethod("install/dependencies/dependency", "defineDependency", 4,
                            new Class[] { String.class, String.class, String.class, Integer.class });
            digester.addCallParam("install/dependencies/dependency/groupId", 0);
            digester.addCallParam("install/dependencies/dependency/artifactId", 1);
            digester.addCallParam("install/dependencies/dependency/version", 2);
            digester.addCallParam("install/dependencies/dependency", 3, "order");

            digester.addCallMethod("install/files/file", "addClassPathFile", 2);
            digester.addCallParam("install/files/file", 0, "name");
            digester.addCallParam("install/files/file", 1, "type");

            digester.addObjectCreate("install/version", ApplicationVersion.class);
            digester.addSetNext("install/version", "addVersion");

            digester.addCallMethod("install/version", "setNumber", 1, new Class[] { Long.class });
            digester.addCallParam("install/version", 0, "number");

            digester.addCallMethod("install/version", "setCompile", 1, new Class[] { Boolean.class });
            digester.addCallParam("install/version", 0, "compile");

            digester.addCallMethod("install/version", "setReloadCacheNeeded", 1, new Class[] { Boolean.class });
            digester.addCallParam("install/version", 0, "reloadCache");

            digester.addCallMethod("install/version", "setLoginNeeded", 1, new Class[] { Boolean.class });
            digester.addCallParam("install/version", 0, "login");

            digester.addCallMethod("install/version/description", "appendDescription", 1);
            digester.addCallParam("install/version/description", 0);

            digester.addCallMethod("install/version/lifecyle/ignore", "addIgnoredStep", 1);
            digester.addCallParam("install/version/lifecyle/ignore", 0, "step");

            digester.addCallMethod("install/version/script", "addScript", 4);
            digester.addCallParam("install/version/script", 0);
            digester.addCallParam("install/version/script", 1, "type");
            digester.addCallParam("install/version/script", 2, "name");
            digester.addCallParam("install/version/script", 3, "function");

            appl = (Application) digester.parse(_versionUrl);

            for (final ApplicationVersion applVers : appl.getVersions()) {
                applVers.setApplication(appl);
                appl.setMaxVersion(applVers.getNumber());
            }
            /*
             * } catch (final InvocationTargetException e) { if (e.getCause()
             * instanceof InstallationException) { throw (InstallationException)
             * e.getCause(); } else { throw new
             * InstallationException("Could not parsing the version file '" +
             * _versionUrl + "'", e); }
             */
        } catch (final IOException e) {
            if (e.getCause() instanceof InstallationException) {
                throw (InstallationException) e.getCause();
            } else {
                throw new InstallationException("Could not open / read version file '" + _versionUrl + "'");
            }
        } catch (final Exception e) {
            throw new InstallationException("Error while parsing file '" + _versionUrl + "'", e);
        }
        return appl;
    }

    /**
     * Returns the application definition read from a source directory.
     *
     * @param _versionFile version file which defines the application
     * @param _classpathElements class path elements (required to compile)
     * @param _eFapsDir root directory with the XML installation files
     * @param _includes list of includes; if <code>null</code>
     *            {@link #DEFAULT_INCLUDES} are used
     * @param _excludes list of excludes; if <code>null</code>
     *            {@link #DEFAULT_EXCLUDES} are used
     * @param _file2typeMapping mapping of file extension to type; if
     *            <code>null</code> {@link #DEFAULT_TYPE_MAPPING} is used
     * @return application instance with all version information
     * @throws InstallationException if version file could not be read or opened
     */
    public static Application getApplicationFromSource(final File _versionFile,
                                                       final List<String> _classpathElements,
                                                       final File _eFapsDir,
                                                       final List<String> _includes,
                                                       final List<String> _excludes,
                                                       final Map<String, String> _file2typeMapping)
        throws InstallationException
    {
        final Map<String, String> file2typeMapping = (_file2typeMapping == null)
                        ? Application.DEFAULT_TYPE_MAPPING
                        : _file2typeMapping;
        final Application appl;
        try {
            appl = Application.getApplication(_versionFile.toURI().toURL(),
                                _eFapsDir.toURI().toURL(),
                                _classpathElements);

            for (final String fileName : Application.getFiles(_eFapsDir, _includes, _excludes)) {
                final String type = file2typeMapping.get(fileName.substring(fileName.lastIndexOf(".") + 1));
                appl.addURL(new File(_eFapsDir, fileName).toURI().toURL(), type);
            }
        } catch (final IOException e) {
            throw new InstallationException("Could not open / read version file " + "'" + _versionFile + "'", e);
        } catch (final Exception e) {
            throw new InstallationException("Read version file '" + _versionFile + "' failed", e);
        }
        return appl;
    }

    /**
     * Uses the <code_>includes</code> and <code>_excludes</code> together with
     * the root directory <code>_eFapsDir</code> to get all related and matched
     * files.
     *
     * @param _eFapsDir root directory where the file are located
     * @param _includes defines includes; if not specified the default value is
     *            <code>**&#x002f;*.xml</code>
     * @param _excludes defines excludes; if not specified the default value is
     *            <code>**&#x002f;version.xml</code>
     * @return array of file names
     * @see #DEFAULT_INCLUDES
     * @see #DEFAULT_EXCLUDES
     */
    protected static String[] getFiles(final File _eFapsDir,
                                       final List<String> _includes,
                                       final List<String> _excludes)
    {
        final DirectoryScanner ds = new DirectoryScanner();
        final String[] included = (_includes == null)
                        ? Application.DEFAULT_INCLUDES.toArray(new String[Application.DEFAULT_INCLUDES.size()])
                        : _includes.toArray(new String[_includes.size()]);
        final String[] excluded = (_excludes == null)
                        ? Application.DEFAULT_EXCLUDES.toArray(new String[Application.DEFAULT_EXCLUDES.size()])
                        : _excludes.toArray(new String[_excludes.size()]);
        ds.setIncludes(included);
        ds.setExcludes(excluded);
        ds.setBasedir(_eFapsDir.toString());
        ds.setCaseSensitive(true);
        ds.scan();

        return ds.getIncludedFiles();
    }

    /**
     * Method to get the applications from the class path.
     *
     * @param _application searched application in the class path
     * @param _classpath class path (list of the complete class path)
     * @return List of applications
     * @throws InstallationException if the install.xml file in the class path
     *             could not be accessed
     */
    public static Application getApplicationFromClassPath(final String _application,
                                                          final List<String> _classpath)
        throws InstallationException
    {
        final ClassLoader cl = Application.class.getClassLoader();

        // get install application (read from all install xml files)
        final Map<String, Application> appls = new HashMap<String, Application>();
        try {
            final Enumeration<URL> urlEnum = cl.getResources("META-INF/efaps/install.xml");
            while (urlEnum.hasMoreElements()) {
                // TODO: why class path?
                final URL url = urlEnum.nextElement();
                final Application appl = Application.getApplication(url,
                                new URL(url, "../../../"),
                                _classpath);
                appls.put(appl.getApplication(), appl);
            }
        } catch (final IOException e) {
            throw new InstallationException("Could not access the install.xml file "
                            + "(in path META-INF/efaps/ path of each eFaps install jar).", e);
        }

        return appls.get(_application);
    }

    /**
     * Returns the application read from given JAR file <code>_jarFile</code>.
     *
     * @param _jarFile JAR file with the application to install
     * @param _classpath class path (required to compile)
     * @return application instance
     * @throws InstallationException if application could not be fetched from
     *             the JAR file or the version XML file could not be parsed
     */
    public static Application getApplicationFromJarFile(final File _jarFile,
                                                        final List<String> _classpath)
        throws InstallationException
    {
        try {
            final URL url = new URL("jar", null, 0, _jarFile.toURI().toURL().toString() + "!/");
            final URL url2 = new URL(url, "/META-INF/efaps/install.xml");
            return Application.getApplication(url2, new URL(url2, "../../../"), _classpath);
        } catch (final IOException e) {
            throw new InstallationException("URL could not be parsed", e);
        }
    }

    /**
     * Compiles the ESJP's and all Cascade Styles Sheets within eFaps.
     *
     * @param _userName name of logged in user for which the compile is done
     *            (could be also <code>null</code>)
     * @param _classpath class path elements
     * @throws InstallationException if reload cache of compile failed
     * @see #compileAll(String)
     */
    public static void compileAll(final String _userName,
                                  final List<String> _classpath)
        throws InstallationException
    {
        (new Application((URL) null, _classpath)).compileAll(_userName);
    }

    /**
     * Compiles the ESJP's and all Cascade Styles Sheets within eFaps.
     *
     * @param _userName name of logged in user for which the compile is done
     *            (could be also <code>null</code>)
     * @throws InstallationException if reload cache of compile failed
     */
    public void compileAll(final String _userName)
        throws InstallationException
    {
        if (Application.LOG.isInfoEnabled()) {
            Application.LOG.info("..Compiling");
        }

        reloadCache();

        try {
            Context.begin(_userName);
            (new ESJPCompiler(this.classpathElements)).compile(null);
            AbstractStaticSourceCompiler.compileAll(this.classpathElements);
            Context.commit();
        } catch (final EFapsException e) {
            throw new InstallationException("Compile failed", e);
        }
    }

    /**
     * Installs current application including existing {@link #dependencies}.
     *
     * @param _userName name of logged in user
     * @param _password password of logged in user
     * @throws InstallationException for all cases the installation failed
     * @see #install(String, String, boolean)
     */
    public void install(final String _userName,
                        final String _password)
        throws InstallationException
    {
        this.install(_userName, _password, true);
    }

    /**
     * For each version in {@link #versions} is tested, if it is already
     * installed. If not already installed, the version is installed. Only if
     * <code>_withDependency</code> is defined, also the {@link #dependencies}
     * are installed.
     *
     * @param _userName name of the installation user
     * @param _password password of the installation user
     * @param _withDependency must the dependency also installed?
     * @throws InstallationException if installation failed
     */
    protected void install(final String _userName,
                           final String _password,
                           final boolean _withDependency)
        throws InstallationException
    {
        // install dependency if required
        if (_withDependency) {
            for (final Dependency dependency : this.dependencies.values()) {
                dependency.resolve();
                final Application appl = Application.getApplicationFromJarFile(
                                dependency.getJarFile(), this.classpathElements);
                appl.install(_userName, _password, false);
            }
        }

        // reload cache (if possible)
        reloadCache();

        // load latest installed versions
        final Map<String, Long> latestVersions;
        try {
            Context.begin();
            latestVersions = this.install.getLatestVersions();
            Context.rollback();
        } catch (final EFapsException e) {
            throw new InstallationException("Could not get information about installed versions", e);
        }
        final Long latestVersion = latestVersions.get(this.application);

        Application.LOG.info("Install application '" + this.application + "'");

        for (final ApplicationVersion version : this.versions) {
            if (Application.LOG.isInfoEnabled()) {
                Application.LOG.info("Check version " + version.getNumber());
            }
            if ((latestVersion != null) && (version.getNumber() < latestVersion)) {
                if (Application.LOG.isInfoEnabled()) {
                    Application.LOG.info("Version " + version.getNumber() + " already installed");
                }
            } else {
                if (Application.LOG.isInfoEnabled()) {
                    Application.LOG.info("Starting installation of version " + version.getNumber());
                    final String desc = version.getDescription();
                    if (!"".equals(desc)) {
                        Application.LOG.info(desc);
                    }
                }
                try {
                    // TODO: correct exception handling in the installation
                    version.install(this.install, getLastVersion().getNumber(), _userName, _password);
                } catch (final Exception e) {
                    throw new InstallationException("Installation failed", e);
                }
                storeVersion(_userName, version.getNumber());

                if (Application.LOG.isInfoEnabled()) {
                    Application.LOG.info("Finished installation of version " + version.getNumber());
                }
            }
        }

        // reload cache (if possible)
        reloadCache();
    }

    /**
     * Updates the last installed version.
     *
     * @param _userName name of logged in user
     * @param _password password of logged in user TODO: throw Exceptions
     *            instead of logging errors
     */
    public void updateLastVersion(final String _userName,
                                  final String _password)
        throws EFapsException, Exception
    {
        // reload cache (if possible)
        reloadCache();

        // load installed versions
        Context.begin();
        final Map<String, Long> latestVersions = this.install.getLatestVersions();
        Context.rollback();
        final long latestVersion = latestVersions.get(this.application);

        final ApplicationVersion version = getLastVersion();
        if (version.getNumber() == latestVersion) {
            if (Application.LOG.isInfoEnabled()) {
                Application.LOG.info("Update version "
                                + version.getNumber()
                                + " of application '"
                                + this.application
                                + "'");
            }
            version.install(this.install, version.getNumber(), _userName, _password);
            if (Application.LOG.isInfoEnabled()) {
                Application.LOG.info("Finished update of version " + version.getNumber());
            }
        } else {
            Application.LOG.error("Version "
                            + version.getNumber()
                            + " of application '"
                            + this.application
                            + "' not installed and could not updated!");
        }
    }

    /**
     * Store for this application that the version is already installed. If data
     * model in the local type cache is not loaded (because, e.g., it is a new
     * kernel install), the version numbers are cached.<br/>
     * The first time, the version type could be get from the type cache, all
     * cached versions are stored in eFaps.
     *
     * @param _userName logged in user name
     * @param _version version id to store
     * @throws InstallationException if version could not be stored
     */
    protected void storeVersion(final String _userName,
                                final Long _version)
        throws InstallationException
    {
        final Type versionType = Type.get(ADMIN_COMMON_VERSION);

        if (versionType != null) {
            try {
                Context.begin(_userName);

                // store cached versions
                for (final Long version : this.notStoredVersions) {
                    final Insert insert = new Insert(versionType.getName());
                    insert.add("Name", this.application);
                    insert.add("Revision", "" + version);
                    insert.execute();
                }
                this.notStoredVersions.clear();

                final SearchQuery query = new SearchQuery();
                query.setQueryTypes(versionType.getName());
                query.addWhereExprEqValue("Name", this.application);
                query.addWhereExprEqValue("Revision", _version);
                query.addSelect("OID");
                query.execute();
                if (!query.next()) {
                    // store current version
                    final Insert insert = new Insert(versionType.getName());
                    insert.add("Name", this.application);
                    insert.add("Revision", "" + _version);
                    insert.execute();
                }
                Context.commit();
            } catch (final EFapsException e) {
                throw new InstallationException("Update of the version information failed", e);
            }
        } else {
            // if version could not be stored, cache the version information
            this.notStoredVersions.add(_version);
        }
    }

    /**
     *
     * @param _groupId group id of the dependency
     * @param _artifactId artifact id of the dependency
     * @param _version version of the dependency
     * @param _order number order (used to define in which order the dependent
     *            applications are installed)
     * @throws InstallationException if an order number is defined more than one
     *             time
     */
    public void defineDependency(final String _groupId,
                                 final String _artifactId,
                                 final String _version,
                                 final int _order)
        throws InstallationException
    {
        if (this.dependencies.containsKey(_order)) {
            throw new InstallationException("Order " + _order + " defined for '" + _groupId + "' '"
                            + _artifactId + "' '" + _version + "' is already defined within the dependencies for "
                            + this.dependencies.get(_order));
        }
        this.dependencies.put(_order, new Dependency(_groupId, _artifactId, _version));
    }

    /**
     * Reloads the eFaps cache.
     *
     * @throws InstallationException if reload of the cache failed
     */
    protected void reloadCache()
        throws InstallationException
    {
        try {
            Context.begin();
            if (RunLevel.isInitialisable()) {
                RunLevel.init("shell");
                RunLevel.execute();
            }
            Context.rollback();
        } catch (final EFapsException e) {
            throw new InstallationException("Reload cache failed", e);
        }
    }

    /**
     * Adds a n ew application version to this application which should be
     * installed.
     *
     * @param _version new application version to add
     */
    public void addVersion(final ApplicationVersion _version)
    {
        this.versions.add(_version);
    }

    /**
     * Returns the last application version which must be installed.
     *
     * @return last application version to install
     */
    public ApplicationVersion getLastVersion()
    {
        return (ApplicationVersion) this.versions.toArray()[this.versions.size() - 1];
    }

    /**
     * Adds a new URL with the XML definition file.
     *
     * @param _url url of XML definition files used to install
     * @param _type type of the URL
     * @see #install(String, String)
     */
    public void addURL(final URL _url,
                       final String _type)
    {
        this.install.addFile(_url, _type);
    }

    /**
     * Searches for the given file name (parameter _classPathFile) in the class
     * path and adds them as URL to the list of XML installation / update /
     * definition files ({@link #install}).
     *
     * @param _classPathFile file name from the class path to add
     * @throws MalformedURLException
     * @see #addURL(URL, String)
     */
    public void addClassPathFile(final String _classPathFile,
                                 final String _type)
        throws MalformedURLException
    {
        addURL(new URL(this.rootUrl, _classPathFile), _type);
    }

    /**
     * This is the setter method for instance variable {@link #application}.
     *
     * @param _application new value for instance variable {@link #application}
     * @see #application
     */
    public void setApplication(final String _application)
    {
        this.application = _application;
    }

    /**
     * This is the getter method for instance variable {@link #application}.
     *
     * @return value of instance variable {@link #application}
     * @see #application
     */
    public String getApplication()
    {
        return this.application;
    }

    /**
     * Returns all class path elements required to compile.
     *
     * @return class path elements
     * @see #classpathElements
     */
    public List<String> getClassPathElements()
    {
        return this.classpathElements;
    }

    /**
     * Returns the root URL where the source is located. For local sources it is
     * an URL to a directory, for a Jar file it is the URL to the Jar file.
     *
     * @return value of instance variable {@link #rootUrl}
     */
    public URL getRootUrl()
    {
        return this.rootUrl;
    }

    /**
     * This is the getter method for instance variable {@link #versions}.
     *
     * @return value of instance variable {@link #versions}
     * @see #versions
     */
    public Set<ApplicationVersion> getVersions()
    {
        return this.versions;
    }

    /**
     * This is the setter method for the instance variable {@link #maxVersion}.
     *
     * @param _maxVersion the maxVersion to set
     */
    public void setMaxVersion(final Long _maxVersion)
    {
        this.maxVersion = _maxVersion;
    }

    /**
     * This is the getter method for the instance variable {@link #maxVersion}.
     *
     * @return value of instance variable {@link #maxVersion}
     */
    public Long getMaxVersion()
    {
        return this.maxVersion;
    }

    /**
     * Setter method for instance variable {@link #rootPackageName}.
     *
     * @param _rootPackageName value for instance variable
     *            {@link #rootPackageName}
     */
    public void setRootPackageName(final String _rootPackageName)
    {
        this.rootPackageName = _rootPackageName;
    }

    /**
     * Getter method for instance variable {@link #rootPackageName}.
     *
     * @return value of instance variable {@link #rootPackageName}
     */
    public String getRootPackageName()
    {
        return this.rootPackageName;
    }

    /**
     * Returns a string representation with values of all instance variables.
     *
     * @return string representation of this Application
     */
    @Override()
    public String toString()
    {
        return new ToStringBuilder(this)
                        .append("application", this.application)
                        .append("dependencies", this.dependencies)
                        .append("versions", this.versions)
                        .append("install", this.install)
                        .toString();
    }
}

/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.annotations.FromAnnotationsRuleModule;
import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.CallMethod;
import org.apache.commons.digester3.annotations.rules.CallParam;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetNext;
import org.apache.commons.digester3.annotations.rules.SetProperty;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.tools.ant.DirectoryScanner;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.Profile;
import org.efaps.update.schema.program.esjp.ESJPCompiler;
import org.efaps.update.schema.program.staticsource.AbstractStaticSourceCompiler;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
@ObjectCreate(pattern = "install")
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
        Application.DEFAULT_TYPE_MAPPING.put("bpmn2", FileType.BPM.getType());
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
        Application.DEFAULT_INCLUDES.add("**/*.bpmn2");
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
        Application.DEFAULT_EXCLUDES.add("**/package-info.java");
        Application.DEFAULT_EXCLUDES.add("**/.svn/**");
    }

    /**
     * Stores the name of the application.
     *
     * @see #setApplication
     */
    @BeanPropertySetter(pattern = "install/application")
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
    private List<String> classpathElements;

    /**
     * Dependencies to other applications for this application ordered.
     */
    private final List<Dependency> dependencies = new ArrayList<Dependency>();

    /**
     * Root URL where the source files are located. Could be a file directory (
     * for local installation) or a jar file.
     *
     * @see #Application(URL, List)
     * @see #getRootUrl()
     */
    private URL rootUrl;

    /**
     * Stores the name of the rootPackage.
     */
    @SetProperty(pattern = "install/rootPackage", attributeName = "name")
    private String rootPackageName;

    /**
     * Used in combination with the digester.
     */
    private Map<String, String> tmpElements = new HashMap<String, String>();

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
     * Constructor used by Digester.
     */
    public Application()
    {
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
     */
    public static Application getApplication(final URL _versionUrl,
                                             final URL _rootUrl,
                                             final List<String> _classpathElements)
        throws InstallationException
    {
        Application appl = null;
        try {
            final DigesterLoader loader = DigesterLoader.newLoader(new FromAnnotationsRuleModule()
            {
                @Override
                protected void configureRules()
                {
                    bindRulesFrom(Application.class);
                }
            });
            final Digester digester = loader.newDigester();
            appl = (Application) digester.parse(_versionUrl);
            appl.rootUrl = _rootUrl;
            appl.classpathElements = _classpathElements;
            for (final Entry<String, String> entry : appl.tmpElements.entrySet()) {
                appl.addURL(new URL(_rootUrl, entry.getKey()), entry.getValue());
            }
            appl.tmpElements = null;
            Collections.sort(appl.dependencies, new Comparator<Dependency>()
            {

                @Override
                public int compare(final Dependency _dependency0,
                                   final Dependency _dependency1)
                {
                    return _dependency0.getOrder().compareTo(_dependency1.getOrder());
                }
            });
            for (final ApplicationVersion applVers : appl.getVersions()) {
                applVers.setApplication(appl);
                appl.setMaxVersion(applVers.getNumber());
            }

        } catch (final IOException e) {
            if (e.getCause() instanceof InstallationException) {
                throw (InstallationException) e.getCause();
            } else {
                throw new InstallationException("Could not open / read version file '" + _versionUrl + "'");
            }
          //CHECKSTYLE:OFF
        } catch (final Exception e) {
          //CHECKSTYLE:ON
            throw new InstallationException("Error while parsing file '" + _versionUrl + "'", e);
        }
        return appl;
    }

    /**
     * Returns the application definition read from a source directory.
     *
     * @param _versionFile          version file which defines the application
     * @param _classpathElements    class path elements (required to compile)
     * @param _eFapsDir             root directory with the XML installation
     *                              files
     * @param _outputDir            directory used as target for generated code
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
                                                       final File _outputDir,
                                                       final List<String> _includes,
                                                       final List<String> _excludes,
                                                       final Map<String, String> _file2typeMapping)
        throws InstallationException
    {
        final Map<String, String> file2typeMapping = _file2typeMapping == null
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
            if (_outputDir.exists()) {
                for (final String fileName : Application.getFiles(_outputDir, _includes, _excludes)) {
                    final String type = file2typeMapping.get(fileName.substring(fileName.lastIndexOf(".") + 1));
                    appl.addURL(new File(_outputDir, fileName).toURI().toURL(), type);
                }
            }
        } catch (final IOException e) {
            throw new InstallationException("Could not open / read version file " + "'" + _versionFile + "'", e);
          //CHECKSTYLE:OFF
        } catch (final Exception e) {
          //CHECKSTYLE:ON
            throw new InstallationException("Read version file '" + _versionFile + "' failed", e);
        }
        return appl;
    }

    /**
     * Uses the <code>_includes</code> and <code>_excludes</code> together with
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
        final String[] included = _includes == null
                        ? Application.DEFAULT_INCLUDES.toArray(new String[Application.DEFAULT_INCLUDES.size()])
                        : _includes.toArray(new String[_includes.size()]);
        final String[] excluded = _excludes == null
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
        return Application.getApplicationsFromClassPath(_application, _classpath).get(_application);
    }


    /**
     * Method to get all applications from the class path.
     *
     * @param _application searched application in the class path
     * @param _classpath class path (list of the complete class path)
     * @return List of applications
     * @throws InstallationException if the install.xml file in the class path
     *             could not be accessed
     */
    public static Map<String, Application> getApplicationsFromClassPath(final String _application,
                                                                        final List<String> _classpath)
        throws InstallationException
    {
        final Map<String, Application> appls = new HashMap<String, Application>();
        try {
            final ClassLoader parent = Application.class.getClassLoader();
            final List<URL> urls = new ArrayList<>();
            for (final String pathElement : _classpath) {
                urls.add(new File(pathElement).toURI().toURL());
            }
            final URLClassLoader cl = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), parent);
            // get install application (read from all install xml files)
            final Enumeration<URL> urlEnum = cl.getResources("META-INF/efaps/install.xml");
            while (urlEnum.hasMoreElements()) {
                // TODO: why class path?
                final URL url = urlEnum.nextElement();
                final Application appl = Application.getApplication(url, new URL(url, "../../../"), _classpath);
                appls.put(appl.getApplication(), appl);
            }
        } catch (final IOException e) {
            throw new InstallationException("Could not access the install.xml file "
                            + "(in path META-INF/efaps/ path of each eFaps install jar).", e);
        }
        return appls;
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
     * @param _addRuntimeClassPath must the classpath from the runtime be added
     *                  to the classpath also
     * @throws InstallationException if reload cache of compile failed
     * @see #compileAll(String)
     */
    public static void compileAll(final String _userName,
                                  final List<String> _classpath,
                                  final boolean _addRuntimeClassPath)
        throws InstallationException
    {
        new Application((URL) null, _classpath).compileAll(_userName, _addRuntimeClassPath);
    }

    /**
     * Compiles the ESJP's and all Cascade Styles Sheets within eFaps.
     *
     * @param _userName name of logged in user for which the compile is done
     *            (could be also <code>null</code>)
     * @param _addRuntimeClassPath must the classpath from the runtime be added
     *                  to the classpath also
     * @throws InstallationException if reload cache of compile failed
     */
    public void compileAll(final String _userName,
                           final boolean _addRuntimeClassPath)
        throws InstallationException
    {
        if (Application.LOG.isInfoEnabled()) {
            Application.LOG.info("..Compiling");
        }

        reloadCache();

        try {
            Context.begin(_userName);
            try {
                new ESJPCompiler(getClassPathElements()).compile(null, _addRuntimeClassPath);
            } catch (final InstallationException e) {
                Application.LOG.error(" error during compilation of ESJP.");
            }
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
     * @param _profiles set of profile to be applied
     * @throws InstallationException for all cases the installation failed
     * @see #install(String, String, boolean)
     */
    public void install(final String _userName,
                        final String _password,
                        final Set<Profile> _profiles)
        throws InstallationException
    {
        this.install(_userName, _password, _profiles, null);
    }

    /**
     * Installs current application including existing {@link #dependencies}.
     *
     * @param _userName name of logged in user
     * @param _password password of logged in user
     * @param _profiles set of profile to be applied
     * @param _compile  compile during the install/ if null the setting from the version.xml applies
     * @throws InstallationException for all cases the installation failed
     * @see #install(String, String, boolean)
     */
    public void install(final String _userName,
                        final String _password,
                        final Set<Profile> _profiles,
                        final Boolean _compile)
        throws InstallationException
    {
        this.install(_userName, _password, _profiles, _compile, true);
    }

    /**
     * For each version in {@link #versions} is tested, if it is already
     * installed. If not already installed, the version is installed. Only if
     * <code>_withDependency</code> is defined, also the {@link #dependencies}
     * are installed.
     *
     * @param _userName name of the installation user
     * @param _password password of the installation user
     * @param _profiles set of profile to be applied
     * @param _compile  compile during the install/ if null the setting from the version.xml applies
     * @param _withDependency must the dependency also installed?
     * @throws InstallationException if installation failed
     */
    protected void install(final String _userName,
                           final String _password,
                           final Set<Profile> _profiles,
                           final Boolean _compile,
                           final boolean _withDependency)
        throws InstallationException
    {

        // install dependency if required
        if (_withDependency) {
            for (final Dependency dependency : this.dependencies) {
                dependency.resolve();
                final Application appl = Application.getApplicationFromJarFile(
                                dependency.getJarFile(), this.classpathElements);
                appl.install(_userName, _password, dependency.getProfiles(), _compile, false);
            }
        }

        // reload cache (if possible)
        reloadCache();

        // load latest installed versions
        final Map<String, Integer> latestVersions;
        try {
            Context.begin();
            EFapsClassLoader.getOfflineInstance(getClass().getClassLoader());
            latestVersions = this.install.getLatestVersions();
            Context.rollback();
        } catch (final EFapsException e) {
            throw new InstallationException("Could not get information about installed versions", e);
        }
        final Integer latestVersion = latestVersions.get(this.application);

        Application.LOG.info("Install application '" + this.application + "'");

        for (final ApplicationVersion version : this.versions) {
            Application.LOG.info("Check version '{}'", version.getNumber());
            if (_compile != null) {
                version.setCompile(_compile);
            }
            if (latestVersion != null && version.getNumber() < latestVersion) {
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
                    version.install(this.install, getLastVersion().getNumber(), _profiles, _userName, _password);
                  //CHECKSTYLE:OFF
                } catch (final Exception e) {
                  //CHECKSTYLE:ON
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
     * @param _password password of logged in user
     * @param _profiles Profiles to be applied
     * @throws Exception on error
     */
    public void updateLastVersion(final String _userName,
                                  final String _password,
                                  final Set<Profile> _profiles)
        throws Exception
    {
        // reload cache (if possible)
        reloadCache();

        // load installed versions
        Context.begin();
        EFapsClassLoader.getOfflineInstance(getClass().getClassLoader());
        final Map<String, Integer> latestVersions = this.install.getLatestVersions();
        Context.rollback();
        final long latestVersion = latestVersions.get(this.application);

        final ApplicationVersion version = getLastVersion();
        if (version.getNumber() == latestVersion) {
            Application.LOG.info("Update version '{}' of application '{}' ", version.getNumber(), this.application);

            version.install(this.install, version.getNumber(), _profiles, _userName, _password);

            Application.LOG.info("Finished update of version '{}'", version.getNumber());
        } else {
            Application.LOG.error("Version {}' of application '{}'  not installed and could not updated!",
                            version.getNumber(), this.application);
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
        try {
            Context.begin(_userName);
            if (CIAdminCommon.ApplicationVersion.getType() != null) {
                // store cached versions
                for (final Long version : this.notStoredVersions) {
                    final QueryBuilder appQueryBldr = new QueryBuilder(CIAdminCommon.Application);
                    appQueryBldr.addWhereAttrEqValue(CIAdminCommon.Application.Name, this.application);
                    final InstanceQuery appQuery = appQueryBldr.getQuery();
                    appQuery.execute();
                    Instance appInst;
                    if (appQuery.next()) {
                        appInst = appQuery.getCurrentValue();
                    } else {
                        final Insert insert = new Insert(CIAdminCommon.Application);
                        insert.add(CIAdminCommon.Application.Name, this.application);
                        insert.execute();
                        appInst = insert.getInstance();
                    }
                    final Insert insert = new Insert(CIAdminCommon.ApplicationVersion);
                    insert.add(CIAdminCommon.ApplicationVersion.ApplicationLink, appInst);
                    insert.add(CIAdminCommon.ApplicationVersion.Revision, version);
                    insert.execute();
                }
                this.notStoredVersions.clear();

                final QueryBuilder appQueryBldr = new QueryBuilder(CIAdminCommon.Application);
                appQueryBldr.addWhereAttrEqValue(CIAdminCommon.Application.Name, this.application);
                final InstanceQuery appQuery = appQueryBldr.getQuery();
                appQuery.execute();
                Instance appInst;
                if (appQuery.next()) {
                    appInst = appQuery.getCurrentValue();
                } else {
                    final Insert insert = new Insert(CIAdminCommon.Application);
                    insert.add(CIAdminCommon.Application.Name, this.application);
                    insert.execute();
                    appInst = insert.getInstance();
                }

                final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.ApplicationVersion);
                queryBldr.addWhereAttrEqValue(CIAdminCommon.ApplicationVersion.ApplicationLink, appInst);
                queryBldr.addWhereAttrEqValue(CIAdminCommon.ApplicationVersion.Revision, _version);
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                if (!query.next()) {
                    // store current version
                    final Insert insert = new Insert(CIAdminCommon.ApplicationVersion);
                    insert.add(CIAdminCommon.ApplicationVersion.ApplicationLink, appInst);
                    insert.add(CIAdminCommon.ApplicationVersion.Revision, _version);
                    insert.execute();
                }
            } else {
                // if version could not be stored, cache the version information
                this.notStoredVersions.add(_version);
            }
            Context.commit();
        } catch (final EFapsException e) {
            throw new InstallationException("Update of the version information failed", e);
        }
    }

    /**
     *
     * @param _dependency  dependency
     */
    @SetNext
    public void addDependency(final Dependency _dependency)
    {
        this.dependencies.add(_dependency);
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
    @SetNext
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
     * Setter method for instance variable {@link #application}.
     *
     * @param _application value for instance variable {@link #application}
     */

    public void setApplication(final String _application)
    {
        this.application = _application;
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
     * @param _type             type of the file to be added
     * @throws MalformedURLException on error with the URL
     * @see #addURL(URL, String)
     */
    @CallMethod(pattern = "install/files/file")
    public void addClassPathFile(
                        @CallParam(pattern = "install/files/file", attributeName = "name") final String _classPathFile,
                        @CallParam(pattern = "install/files/file", attributeName = "type") final String _type)
        throws MalformedURLException
    {
        this.tmpElements.put(_classPathFile, _type);
    }

    /**
     * Getter method for the instance variable {@link #install}.
     *
     * @return value of instance variable {@link #install}
     */
    public Install getInstall()
    {
        return this.install;
    }

    /**
     * Getter method for the instance variable {@link #dependencies}.
     *
     * @return value of instance variable {@link #dependencies}
     */
    public List<Dependency> getDependencies()
    {
        return this.dependencies;
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
    @Override
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

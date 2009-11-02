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

package org.efaps.maven.plugin.goal.efaps.install;

import static org.efaps.admin.EFapsClassNames.ADMIN_COMMON_VERSION;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.update.Install;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class Application
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

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
     * Stores the Directory to the files to be installed.
     */
    private File eFapsDir;

    /**
     * <code>null</code> is returned, of the version file could not be opened
     * and read.
     *
     * @return application instance with all version information
     * @todo description
     * @todo better definition of include dir / file
     */
    public static Application getApplication(final URL _url,
                                             final List<String> _classpathElements,
                                             final File _efapsdir)
    {
        Application appl = null;
        try {
            final Digester digester = new Digester();
            digester.setValidating(false);
            digester.addObjectCreate("install", Application.class);

            digester.addCallMethod("install/application", "setApplication", 1);
            digester.addCallParam("install/application", 0);

            digester.addCallMethod("install/files/file", "addClassPathFile", 2);
            digester.addCallParam("install/files/file", 0, "name");
            digester.addCallParam("install/files/file", 1, "type");

            digester.addObjectCreate("install/version", ApplicationVersion.class);
            digester.addSetNext("install/version", "addVersion");

            digester.addCallMethod("install/version", "setNumber", 1, new Class[] {Long.class});
            digester.addCallParam("install/version", 0, "number");

            digester.addCallMethod("install/version", "setCompile", 1, new Class[] {Boolean.class});
            digester.addCallParam("install/version", 0, "compile");

            digester.addCallMethod("install/version", "setReloadCacheNeeded", 1, new Class[] {Boolean.class});
            digester.addCallParam("install/version", 0, "reloadCache");

            digester.addCallMethod("install/version", "setLoginNeeded", 1, new Class[] {Boolean.class});
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

            appl = (Application) digester.parse(_url);
            appl.setEFapsDir(_efapsdir);

            for (final ApplicationVersion applVers : appl.getVersions()) {
                applVers.setClasspathElements(_classpathElements);
                applVers.setApplication(appl);
                appl.setMaxVersion(applVers.getNumber());
            }
        } catch (final IOException e) {
            Application.LOG.error("Could not open / read version file '" + _url + "'");
        } catch (final Exception e) {
            Application.LOG.error("Error while parsing file '" + _url + "'", e);
        }
        return appl;
    }

    /**
     * For each version in {@link @versions} is tested, if it is already
     * installed (installed versions are previously loaded with
     * {@link #loadInstalledVersions}). If not already installed, the version
     * is installed.
     *
     * @param _userName   name of logged in user
     * @param _password   password of logged in user
     * @see #loadInstalledVersions
     * @see #versions
     * @see ApplicationVersion#install
     */
    public void install(final String _userName,
                        final String _password)
        throws EFapsException, Exception
    {
        // reload cache (if possible)
        Context.begin();
        if (RunLevel.isInitialisable()) {
            RunLevel.init("shell");
            RunLevel.execute();
        }
        Context.rollback();

        // load latest installed versions
        Context.begin();
        final Map<String, Long> latestVersions = this.install.getLatestVersions();
        Context.rollback();
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
                    if (!"".equals(desc))  {
                        Application.LOG.info(desc);
                    }
                }
                version.install(this.install, getLastVersion().getNumber(), _userName, _password);
                storeVersion(_userName, version.getNumber());

                if (Application.LOG.isInfoEnabled()) {
                    Application.LOG.info("Finished installation of version " + version.getNumber());
                }
            }
        }

        Context.begin(_userName);
        if (RunLevel.isInitialisable()) {
            RunLevel.init("shell");
            RunLevel.execute();
            Context.rollback();
            Context.begin(_userName);
        }
        Context.commit();
    }

    /**
     * Updates the last installed version.
     *
     * @param _userName   name of logged in user
     * @param _password   password of logged in user
     * @todo throw Exceptions instead of logging errors
     */
    public void updateLastVersion(final String _userName,
                                  final String _password)
        throws EFapsException,Exception
    {
        // reload cache (if possible)
        Context.begin();
        if (RunLevel.isInitialisable()) {
            RunLevel.init("shell");
            RunLevel.execute();
        }
        Context.rollback();

        // load installed versions
        Context.begin();
        final Map<String, Long> latestVersions = this.install.getLatestVersions();
        Context.rollback();
        final long latestVersion = latestVersions.get(this.application);

        final ApplicationVersion version = getLastVersion();
        if (version.getNumber() == latestVersion)  {
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
     * Store for this application that the version is already installed. If
     * data model in the local type cache is not loaded (because, e.g., it is a
     * new kernel install), the version numbers are cached.<br/> The first
     * time, the version type could be get from the type cache, all cached
     * versions are stored in eFaps.
     *
     * @param _userName logged in user name
     * @param _version  version id to store
     */
    private void storeVersion(final String _userName,
                              final Long _version)
        throws EFapsException
    {
        final Type versionType = Type.get(ADMIN_COMMON_VERSION);

        if (versionType != null) {
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
        } else {
            // if version could not be stored, cache the version information
            this.notStoredVersions.add(_version);
        }
    }

    /**
     * Adds a n ew application version to this application which should be
     * installed.
     *
     * @param _version  new application version to add
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
    public final ApplicationVersion getLastVersion()
    {
        return (ApplicationVersion) this.versions.toArray()[this.versions.size() - 1];
    }

    /**
     * Adds a new URL with the XML definition file.
     *
     * @param _url    url of XML definition files used to install
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
     * @param _classPathFile    file name from the class path to add
     * @see #addURL(URL, String)
     */
    public void addClassPathFile(final String _classPathFile,
                                 final String _type)
    {
        addURL(getClass().getClassLoader().getResource(_classPathFile), _type);
    }

    /**
     * This is the setter method for instance variable {@link #application}.
     *
     * @param _application  new value for instance variable {@link #application}
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
     * This is the getter method for the instance variable {@link #eFapsDir}.
     *
     * @return value of instance variable {@link #eFapsDir}
     */
    public File getEFapsDir()
    {
        return this.eFapsDir;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #eFapsDir}.
     *
     * @param _efapsdir   the eFapsDir to set
     */
    public void setEFapsDir(final File _efapsdir)
    {
        this.eFapsDir = _efapsdir;
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
     * @param _maxVersion  the maxVersion to set
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
     * Returns a string representation with values of all instance variables.
     *
     * @return string representation of this Application
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                  .append("application", this.application)
                  .append("versions", this.versions)
                  .append("install", this.install)
                  .toString();
    }
}

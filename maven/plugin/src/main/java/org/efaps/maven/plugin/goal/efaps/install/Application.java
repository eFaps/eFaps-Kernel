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

package org.efaps.maven.plugin.goal.efaps.install;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.importer.DataImport;
import org.efaps.update.Install;
import org.efaps.update.dbproperty.DBPropertiesUpdate;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 */
public class Application {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(Application.class);

  /**
   * UUID of eFaps type 'Admin_Common_Version'.
   */
  private final static UUID VERSION_UUID
          = UUID.fromString("1bb051f3-b664-43db-b409-c0c4009f5972");

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Stores the name of the application.
   * 
   * @see #setApplication
   */
  private String application = null;

  /**
   * Stores all versions of this application which must be installed.
   * 
   * @see #getVersions
   */
  private Set<ApplicationVersion> versions = new TreeSet<ApplicationVersion>();

  /**
   * Stores all already installed version numbers.
   * 
   * @see #loadInstalledVersions
   */
  private final Set<Long> installed = new HashSet<Long>();

  /**
   * Install instance holding all xml update files.
   */
  private final Install install = new Install();

  /**
   * Caches not stores versions (because if the kernel install is made, the
   * version could not be updated till the SQL tables and the data model is
   * already installed and the cache is reloaded).
   */
  private final List<Long> notStoredVersions = new ArrayList<Long>();

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * <code>null</code> is returned, of the version file could not be opened
   * and read.
   * 
   * @return application instance with all version information
   * @todo description
   * @todo better definition of include dir / file
   */
  public static Application getApplication(final URL _url,
                                           final List<String> _classpathElements) {
    Application appl = null;
    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("install", Application.class);

      digester.addCallMethod("install/application", "setApplication", 1);
      digester.addCallParam("install/application", 0);

      digester.addObjectCreate("install/version", ApplicationVersion.class);
      digester.addSetNext("install/version", "addVersion");

      digester.addCallMethod("install/version", "setNumber", 1, new Class[]{Long.class});
      digester.addCallParam("install/version", 0, "number");

      digester.addCallMethod("install/version", "setCompile", 1, new Class[]{Boolean.class});
      digester.addCallParam("install/version", 0, "compile");

      digester.addCallMethod("install/version", "setReloadCacheNeeded", 1, new Class[]{Boolean.class});
      digester.addCallParam("install/version", 0, "reloadCache");

      digester.addCallMethod("install/version", "setLoginNeeded", 1, new Class[]{Boolean.class});
      digester.addCallParam("install/version", 0, "login");

      digester.addCallMethod("install/version/script", "addScript", 2);
      digester.addCallParam("install/version/script", 0, "name");
      digester.addCallParam("install/version/script", 1, "function");

      appl = (Application) digester.parse(_url);

      for (final ApplicationVersion applVers : appl.getVersions()) {
        applVers.setClasspathElements(_classpathElements);
      }
    } catch (IOException e) {
      LOG.error(
          "Could not open / read version file '" + _url + "'");
    } catch (Exception e) {
      LOG.error("Error while parsing file '" + _url + "'", e);
    }
    return appl;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * For each version in {@link @versions} is tested, if it is alread installed
   * (installed versions are previously loaded with
   * {@link #loadInstalledVersions}). If not already installed, the version is
   * installed.
   * 
   * @see #loadInstalledVersions
   * @see #versions
   * @see ApplicationVersion#install
   */
  public void install(final String _userName) throws EFapsException, Exception {
    loadInstalledVersions(_userName);

    LOG.info("Install application '" + this.application + "'");
    for (final ApplicationVersion version : versions) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Check version " + version.getNumber());
      }
      if (this.installed.contains(version.getNumber())) {
        if (LOG.isInfoEnabled()) {
          LOG.info("Version " + version.getNumber() + " already installed");
        }
      } else {
        if (LOG.isInfoEnabled()) {
          LOG.info("Starting installation of version " + version.getNumber());
        }
        version.install(this.install, _userName);
        storeVersion(_userName, version.getNumber());

        if (LOG.isInfoEnabled()) {
          LOG.info("Finished installation of version " + version.getNumber());
        }
      }
    }

  }

  /**
   * Updates the last installed version.
   * 
   * @todo throw Exceptions instead of logging errors
   */
  public void updateLastVersion(final String _userName) throws EFapsException, Exception {
    loadInstalledVersions(_userName);
    ApplicationVersion version = getLastVersion();
    if (this.installed.contains(version.getNumber())) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Update version " + version.getNumber() + " of application "
            + "'" + this.application + "'");
      }
      version.install(this.install, _userName);
      if (LOG.isInfoEnabled()) {
        LOG.info("Finished update of version " + version.getNumber());
      }
    } else {
      LOG.error("Version " + version.getNumber() + " of application " + "'"
          + this.application + "' not installed and could not updated!");
    }
  }

  /**
   * Load the already installed versions for this application from eFaps.
   * 
   * @param _userName   logged in user name
   * @see #installed
   */
  private void loadInstalledVersions(final String _userName) throws Exception {
    final Type versionType = Type.get(VERSION_UUID);
    if (versionType != null)  {
      Context.begin(_userName);
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(versionType.getName());
      query.addWhereExprEqValue("Name", this.application);
      query.addSelect("Revision");
      query.executeWithoutAccessCheck();
      while (query.next()) {
        this.installed.add((Long) query.get("Revision"));
      }
      query.close();
      Context.commit();
    }
  }

  /**
   * Store for this application that the version is already installed. If data
   * model in the local type cache is not loaded (because, e.g., it is a new
   * kernel install), the version numbers are cached.<br/>
   * The first time, the version type could be get from the type cache, all
   * cached versions are stores in eFaps.
   *
   * @param _userName   logged in user name
   * @param _version    version id to store
   */
  private void storeVersion(final String _userName,
                            final Long _version) throws Exception  {
    final Type versionType = Type.get(VERSION_UUID);
    if (versionType != null)  {
      Context.begin(_userName);

      // store cached versions
      for (final Long version : this.notStoredVersions)  {
        final Insert insert = new Insert(versionType.getName());
        insert.add("Name", this.application);
        insert.add("Revision", "" + version);
        insert.execute();
      }
      this.notStoredVersions.clear();

      // store current version
      final Insert insert = new Insert(versionType.getName());
      insert.add("Name", this.application);
      insert.add("Revision", "" + _version);
      insert.execute();

      Context.commit();
    } else  {
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
  public void addVersion(final ApplicationVersion _version) {
    this.versions.add(_version);
  }

  /**
   * Returns the last application version which must be installed.
   * 
   * @return last application version to install
   */
  public final ApplicationVersion getLastVersion() {
    return (ApplicationVersion) this.versions.toArray()[this.versions.size() - 1];
  }

  public void addURL(final URL _url) {
    this.install.addURL(_url);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the setter method for instance variable {@link #application}.
   * 
   * @param _application  new value for instance variable {@link #application}
   * @see #application
   */
  public void setApplication(final String _application) {
    this.application = _application;
  }

  /**
   * This is the getter method for instance variable {@link #versions}.
   * 
   * @return value of instance variable {@link #versions}
   * @see #versions
   */
  public Set<ApplicationVersion> getVersions() {
    return this.versions;
  }

  /**
   * imports Data from XML-Files and Properties
   */
  public void importData() {

    Map<String, URL> map = this.install.getURLs();

    for (Entry<?, ?> entry : map.entrySet()) {
      DataImport dimport = new DataImport();
      dimport.readXMLFile((URL) entry.getValue());
      if (dimport.hasData()) {
        dimport.updateInDB();
      }

      DBPropertiesUpdate prop =
          DBPropertiesUpdate.readXMLFile((URL) entry.getValue());
      if (prop != null) {
        try {
          prop.updateInDB();
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Returns a string representation with values of all instance variables.
   * 
   * @return string representation of this Application
   */
  public String toString() {
    return new ToStringBuilder(this)
        .append("application", this.application)
        .append("versions", this.versions)
        .append("install", this.install)
        .toString();
  }

}

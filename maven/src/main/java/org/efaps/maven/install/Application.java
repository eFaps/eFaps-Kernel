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

package org.efaps.maven.install;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * 
 * @author tmo
 * @version $Id: Application.java 609 2007-01-07 18:34:38 +0000 (Sun, 07 Jan
 *          2007) tmo $
 */
public class Application {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(Application.class);

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
  private Set<ApplicationVersion> versions    = new TreeSet<ApplicationVersion>();

  /**
   * Stores all alread installed version numbers.
   * 
   * @see #loadInstalledVersions
   */
  private final Set<Long> installed = new HashSet<Long>();

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * For each version in {@link @versions} is tested, if it is alread installed
   * (installed versions are previously loaded with
   * {@link #loadInstalledVersions}). If not already installed, the version is
   * installed.
   * 
   * @see #loadInstalledVersions #
   * @see #versions
   * @see ApplicationVersion#install
   */
  public void install() throws EFapsException, Exception {
    loadInstalledVersions();
    
    LOG.info("Install application '" + this.application + "'");
    for (ApplicationVersion version : versions) {
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
        version.install();
        storeVersion(version.getNumber());

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
  public void updateLastVersion() throws EFapsException, Exception {
    loadInstalledVersions();
    ApplicationVersion version = getLastVersion();
    if (this.installed.contains(version.getNumber())) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Update version " + version.getNumber() + " of application "
            + "'" + this.application + "'");
      }
      version.install();
      if (LOG.isInfoEnabled()) {
        LOG.info("Finished update of version " + version.getNumber());
      }
    } else {
      LOG.error("Version " + version.getNumber() + " of application " + "'"
          + this.application + "' not installed and could " + "not updated!");
    }
  }

  /**
   * Load the already installed versions for this application from eFaps.
   * 
   * @see #installed
   */
  private void loadInstalledVersions() throws EFapsException {
    SearchQuery query = new SearchQuery();
    query.setQueryTypes("Admin_Common_Version");
    query.addWhereExprEqValue("Name", this.application);
    query.addSelect("Revision");
    query.executeWithoutAccessCheck();
    while (query.next()) {
      this.installed.add((Long) query.get("Revision"));
    }
    query.close();
  }

  /**
   * Store for this application that the version is already installed.
   */
  private void storeVersion(final Long _version) throws EFapsException {
    Insert insert = new Insert("Admin_Common_Version");
    insert.add("Name", this.application);
    insert.add("Revision", "" + _version);
    insert.execute();
  }

  /**
   * Adds a n ew application version to this application which should be
   * installed.
   * 
   * @param _version
   *          new application version to add
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

  // ///////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the setter method for instance variable {@link #application}.
   * 
   * @param _application
   *          new value for instance variable {@link #application}
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
   * Returns a string representation with values of all instance variables.
   * 
   * @return string representation of this Application
   */
  public String toString() {
    return new ToStringBuilder(this).append("application", this.application)
        .append("versions", this.versions).toString();
  }
}

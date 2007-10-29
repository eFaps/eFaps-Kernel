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
package org.efaps.maven.plugin.install;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.jfrog.maven.annomojo.annotations.MojoParameter;

import org.efaps.maven.plugin.EFapsAbstractMojo;
import org.efaps.maven.plugin.goal.efaps.install.Application;

/**
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractEFapsInstallMojo extends EFapsAbstractMojo {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Location of the version file (defining all versions to install).
   */
  @MojoParameter(expression = "${basedir}/src/main/efaps/versions.xml")
  private File versionFile;

  /**
   * Root Directory with the XML installation files.
   */
  @MojoParameter(expression = "${basedir}/src/main/efaps")
  private File eFapsDir;

  /**
   * List of includes.
   */
  @MojoParameter()
  private List<String> includes = null;

  /**
   * List of excludes.
   */
  @MojoParameter()
  private List<String> excludes = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * <code>null</code> is returned, of the version file could not be opened
   * and read.
   * 
   * @return application instance with all version information
   * @todo description
    */
  protected Application getApplication() {
    Application appl = null;
    try {
      appl = Application.getApplication(this.versionFile.toURL(),
                                        getClasspathElements());

      for (final String file : getFiles())  {
        appl.addURL(new File(this.eFapsDir, file).toURL());
      }
    } catch (IOException e) {
      getLog().error(
          "Could not open / read version file " + "'" + this.versionFile + "'");
    } catch (Exception e) {
      getLog().error(e);
    }
    return appl;
  }

  /**
   * Uses the {@link #includes} and {@link #excludes} together with the root
   * directory {@link #eFapsDir} to get all related and matched files.
   *
   * @see #includes   defines includes; if not specified by maven, the default
   *                  value is <code>**&#x002f;*.xml</code>
   * @see #excludes   defines excludes; if not specified by maven , the default
   *                  value is <code>**&#x002f;version.xml</code>
   * @see FileSet
   */
  protected Collection<String> getFiles()  {
    FileSet fileSet = new FileSet();
    fileSet.setRootDirectory(eFapsDir.toString());
    if (this.includes == null)  {
      fileSet.addInclude("**/*.xml");
    } else  {
      fileSet.addIncludes(this.includes);
    }
    if (this.excludes == null)  {
      fileSet.addExclude("**/versions.xml");
    } else  {
      fileSet.addExcludes(this.excludes);
    }
    return fileSet.getFiles();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter / setter methods

  /**
   * Getter method for instance variable {@see #eFapsDir}.
   *
   * @return value of instance variable eFapsDir
   * @see #eFapsDir
   */
  protected File getEFapsDir() {
    return this.eFapsDir;
  }

  /**
   * Getter method for instance variable {@see #versionFile}.
   *
   * @return value of instance variable versionFile
   * @see #versionFile
   */
  protected File getVersionFile() {
    return this.versionFile;
  }
}

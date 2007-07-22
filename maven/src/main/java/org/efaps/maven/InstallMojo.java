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

package org.efaps.maven;

import org.apache.maven.plugin.MojoExecutionException;

import org.efaps.maven.install.Application;
import org.efaps.util.EFapsException;

/**
 * Installs an eFaps application.
 *
 * @author tmo
 * @version $Id$
 * @goal install
 * @requiresDependencyResolution compile
 */
public final class InstallMojo extends EFapsAbstractMojo {

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Executes the install goal.
   */
  public void execute() throws MojoExecutionException {
    init();

    try {
      reloadCache();
      startTransaction();

      Application appl = getApplication();
      if (appl != null) {
        appl.install();
      }

      commitTransaction();
      if (appl != null) {
        startTransaction();
        appl.importData();
        commitTransaction();
      }

    } catch (EFapsException e) {
      getLog().error(e);
    } catch (Exception e) {
      getLog().error(e);
    }
  }
}

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

package org.efaps.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;

import org.efaps.maven.plugin.goal.efaps.install.ApplicationVersion;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.util.EFapsException;

/**
 * Compiles all ESPJ's and Cascade Style Sheets within eFaps.
 *
 * @author tmo
 * @version $Id$
 */
@Goal(name = "compile",
    requiresDependencyResolutionScope = "compile")
public final class CompileMojo extends EFapsAbstractMojo {

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Executes the esjp goal.
   */
  public void execute() throws MojoExecutionException {
    init();

    try {
      reloadCache();

      final ApplicationVersion applVers = new ApplicationVersion();
      applVers.setClasspathElements(getClasspathElements());
      applVers.compileAll(getUserName());

    } catch (final EFapsException e) {
      getLog().error(e);
    } catch (final Exception e) {
      getLog().error(e);
    }
  }
}

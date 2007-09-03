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

import org.efaps.admin.program.esjp.Compiler;
import org.efaps.util.EFapsException;

import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

/**
 * Compiles all esjp's within eFaps.
 *
 * @author tmo
 * @version $Id$
 */
@MojoGoal("compile")
@MojoRequiresDependencyResolution("compile")
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
      startTransaction();
      (new Compiler(getClasspathElements())).compile();
      commitTransaction();

    } catch (EFapsException e) {
      getLog().error(e);
    } catch (Exception e) {
      getLog().error(e);
    }
  }
}

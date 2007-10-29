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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import org.efaps.maven.plugin.goal.efaps.install.Application;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
@MojoGoal("install")
@MojoRequiresDependencyResolution("compile")
public final class InstallMojo extends AbstractEFapsInstallMojo  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * 
   */
  @MojoParameter()
  private List<String> applications = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Executes the kernel install goal.
   *
   * @todo descriptionâ
   */
  public void execute() throws MojoExecutionException  {
    init();
    try  {
      final ClassLoader cl = getClass().getClassLoader();

      // get kernel install application (read from version xml file)
      Application appl = Application.getApplication(cl.getResource("META-INF/efaps/install.xml"),
                                                    getClasspathElements());
      appl.install(getUserName(), getPassWord());

      startTransaction();
      appl.importData();
      commitTransaction();
    } catch (Exception e)  {
      throw new MojoExecutionException(
            "Could not execute Kernal Installation script", e);
    }
  }
}

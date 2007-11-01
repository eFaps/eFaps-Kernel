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

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
   * Comma separated list of applications to install. The default value is the
   * kernel application.
   */
  @MojoParameter(defaultValue = "efaps")
  private String applications;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Executes the kernel install goal.
   *
   * @throws MojoExecutionException if a defined application could not be found
   *                                or the installation scripts could not be
   *                                executed
   * @todo descriptionâ
   */
  public void execute() throws MojoExecutionException  {
    init();
    try  {
      final ClassLoader cl = getClass().getClassLoader();

      // get install application (read from all install xml files)
      final Map<String, Application> appls = new HashMap<String, Application>();
      Enumeration<URL> urlEnum = cl.getResources("META-INF/efaps/install.xml");
      while (urlEnum.hasMoreElements())  {
        final Application appl = Application.getApplication(urlEnum.nextElement(),
                                                            getClasspathElements());
        appls.put(appl.getApplication(), appl);
      }

      // test if all defined applications could be found
      final String[] applicationNames = this.applications.split(",");
      for (final String applName : applicationNames)  {
        if (appls.get(applName) == null)  {
          throw new MojoExecutionException("Could not found defined "
              + "application '" + applName + "'. Installation not possible!");
        }
      }

      // install applications
      for (final String applName : applicationNames)  {
        final Application appl = appls.get(applName);
        appl.install(getUserName(), getPassWord());
        startTransaction();
        appl.importData();
        commitTransaction();
      }
    } catch (MojoExecutionException e)  {
      throw e;
    } catch (Exception e)  {
      throw new MojoExecutionException(
            "Could not execute Installation script", e);
    }
  }
}

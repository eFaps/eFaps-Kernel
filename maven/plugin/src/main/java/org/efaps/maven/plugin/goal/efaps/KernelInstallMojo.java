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

package org.efaps.maven.plugin.goal.efaps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import org.efaps.maven.plugin.goal.efaps.install.Application;
import org.efaps.maven.plugin.install.AbstractEFapsInstallMojo;
import org.efaps.update.Install;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
@MojoGoal("kernel-install")
@MojoRequiresDependencyResolution("compile")
public final class KernelInstallMojo extends AbstractEFapsInstallMojo  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance variables
  
  /**
   * 
   */
  final private Install install = new Install();

  final List<URL> files = new ArrayList<URL>();

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
      Application appl = Application.getApplication(cl.getResource("META-INF/efaps/kernel-install/versions.xml"),
                                                    getClasspathElements());

      // append xml files to application
      getLog().info("Append XML Files");
      final InputStream stream = cl.getResourceAsStream("META-INF/efaps/kernel-install/files.txt");
      if (stream != null)  {
        final LineNumberReader reader = new LineNumberReader(new InputStreamReader(stream, "UTF-8"));

        String line = reader.readLine();
        while (line != null)  {
          final URL url = cl.getResource(line);
          appl.addURL(url);
          this.files.add(url);
          line = reader.readLine();
        }
        reader.close();

        getLog().info("Cache XML Files");
        this.install.initialise();
      } else  {
        getLog().error("Could not Found the index file 'files.txt'.");
      }

      appl.install(getUserName(), getPassWord());

      startTransaction();
      appl.importData();
      commitTransaction();
    } catch (Exception e)  {
e.printStackTrace();
      throw new MojoExecutionException(
            "Could not execute Kernal Installation script", e);
    }
  }
}

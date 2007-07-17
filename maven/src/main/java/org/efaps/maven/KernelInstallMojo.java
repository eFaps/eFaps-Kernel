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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.maven.plugin.MojoExecutionException;

import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 * @goal kernel-install
 * @requiresDependencyResolution compile
 */
public final class KernelInstallMojo extends AbstractJavaScriptMojo  {
  
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
      Reader in = new InputStreamReader(
          getClass().getClassLoader()
                    .getResourceAsStream("org/efaps/kernel-install/CreateAll.js"));
      evaluate(in, "CreateAll.js");
      in.close();
      putPropertyInJS("classPathElements", getClasspathElements());

      evaluate(new StringReader("eFapsCreateAll();"), "eFapsCreateAll()");
    } catch (IOException e)  {
      throw new MojoExecutionException(
            "Could not execute Kernal Installation script", e);
    }
  }
}

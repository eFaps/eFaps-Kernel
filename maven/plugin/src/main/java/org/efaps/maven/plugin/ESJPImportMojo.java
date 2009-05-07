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

import java.io.File;
import java.net.MalformedURLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.efaps.admin.program.esjp.ESJPImporter;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Parameter;
import org.efaps.util.EFapsException;

/**
 * Mojo used to import ESJP's.
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
@Goal(name = "esjp-import")
public class ESJPImportMojo extends EFapsAbstractMojo  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * URL of the ESJP to import.
   */
  @Parameter(required = true)
  private File file;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    init();

    try {
      reloadCache();
      startTransaction();
      final ESJPImporter esjpImport = new ESJPImporter(this.file.toURL());
      esjpImport.execute();
      getLog().info("ESJP '" + this.file.toString() + "' inserted.");
      commitTransaction();
    } catch (final EFapsException e) {
      throw new MojoFailureException("ESJP import failed " + e.toString());
    } catch (final MalformedURLException e) {
      throw new MojoFailureException("File not found " + e.toString());
    }
  }
}

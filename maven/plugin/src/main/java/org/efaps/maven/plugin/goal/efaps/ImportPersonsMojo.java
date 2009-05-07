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

package org.efaps.maven.plugin.goal.efaps;

import org.apache.maven.plugin.MojoExecutionException;

import org.efaps.jaas.ImportHandler;
import org.efaps.maven.plugin.EFapsAbstractMojo;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Parameter;

/**
 * The class is used to start the import of persons directly as parameter from
 * the eFaps shell.<br/>
 * To start the import, call the shell with
 * <code>shell.sh -importPersons</code><br/>
 * Following Java system properties must / could be set to configure the
 * import:
 * <dl>
 *   <dt>java.security.auth.login.config</dt>
 *   <dd>Defines the JAAS configuration file.</dd>
 * </dl>
 *
 * @author tmo
 * @version $Id$
 */
@Goal(name = "import-persons")
public final class ImportPersonsMojo extends EFapsAbstractMojo  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Defines the name of the JAAS application defined in the JAAS configuration
   * used to import.
   */
  @Parameter(defaultValue = "eFaps")
  private String application;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The import of persons is started. First the cache is reloaded and then
   * the import itself is done.
   *
   * @todo remove Exception
   */
  public void execute() throws MojoExecutionException {

    try {
      reloadCache();
      startTransaction();
      (new ImportHandler(this.application)).importPersons();
      commitTransaction();
    } catch (final Exception e)  {
      throw new MojoExecutionException("import of persons failed", e);
    }
  }
}

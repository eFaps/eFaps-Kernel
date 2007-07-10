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

import org.apache.commons.logging.Log;
import org.apache.maven.plugin.MojoExecutionException;

import org.efaps.maven.install.Application;
import org.efaps.util.EFapsException;

/**
 * Makes an update of an eFaps application for the last version of the
 * application.
 *
 * @author tmo
 * @version $Id$
 * @goal update
 */
public final class UpdateMojo extends EFapsAbstractMojo  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Executes the update goal.
   */
  public void execute() throws MojoExecutionException  {
    System.getProperties().setProperty(Log.class.getName(),
                                       Maven2CommonsLog.class.getName());    
    Maven2CommonsLog.logger = getLog();
    getLog().info("Initialise Database Connection");
    if (!initDatabase())  {
      getLog().error("Database Connection could not be initialised!");
    } else  {
      try  {
        login("Administrator", "");
        reloadCache();
        startTransaction();
       
        Application appl = getApplication();
        if (appl != null)  {
          appl.updateLastVersion();
        }

        commitTransaction();
      } catch (EFapsException e)  {
        getLog().error(e);
      } catch (Exception e)  {
        getLog().error(e);
      }
    }
  }
}

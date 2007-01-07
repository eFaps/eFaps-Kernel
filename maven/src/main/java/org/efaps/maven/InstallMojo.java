/*
 * Copyright 2006 The eFaps Team
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
 *
 * @author tmo
 * @version $Id$
 * @goal install
 */
public final class InstallMojo extends AbstractMojo  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  public void execute() throws MojoExecutionException  {
Maven2CommonsLog.logger=getLog();
System.getProperties().setProperty(org.apache.commons.logging.Log.class.getName(),
                                   Maven2CommonsLog.class.getName());    

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
          appl.install();
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

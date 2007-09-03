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

import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

/**
 * 
 * @author tmo
 * @version $Id$
 */
@MojoGoal("derby-start")
public class DerbyStartMojo extends AbstractMojo {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Derby specific Properties.
   */
  @MojoParameter
  private Properties properties;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
   */
  public void execute() throws MojoExecutionException  {
    getLog().info("Start Derby Database");
    if (this.properties != null)  {
      for (Object propName : this.properties.keySet())  {
        String propValue = this.properties.getProperty((String) propName);
        getLog().info("- using " + propName + " = " + propValue);
        System.setProperty((String) propName,
                           propValue);
      }
    }
    NetworkServerControl.main(new String[]{"start"});
  }
}

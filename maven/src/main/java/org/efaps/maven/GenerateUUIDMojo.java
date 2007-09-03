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

import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

/**
 * A new universally unique identifier (UUID) is created and printed out.
 *
 * @author tmo
 * @version $Id$
 */
@MojoGoal("generateUUID")
public final class GenerateUUIDMojo extends AbstractMojo  {

  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * Number of UUID's to generate.
   */
  @MojoParameter
  private int count = 1;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The new universally unique identifier is created and printed out with a 
   * normal call to the mojo log info.
   */
  public void execute() throws MojoExecutionException  {
    for (int i = 0; i < this.count; i++)  {
      UUID uuid = UUID.randomUUID();
      getLog().info("UUID[" + (i + 1) + "] = " + uuid.toString());
    }
  }
}

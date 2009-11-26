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

import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.update.util.InstallationException;
import org.efaps.update.version.Application;

/**
 * Compiles all ESPJ's and Cascade Style Sheets within eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Goal(name = "compile",
      requiresDependencyResolutionScope = "compile")
public final class CompileMojo
    extends EFapsAbstractMojo
{
    /**
     * Executes the compile goal.
     */
    public void execute()
    {
        this.init();

        try {
            Application.compileAll(this.getUserName(), this.getClasspathElements());
        } catch (final InstallationException e) {
            this.getLog().error(e);
        }
    }
}

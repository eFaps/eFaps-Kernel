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

package org.efaps.maven.plugin.install;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.update.version.Application;

/**
 * Updates an eFaps application.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Goal(name = "source-update",
      requiresDependencyResolutionScope = "compile")
public final class SourceUpdateMojo
    extends AbstractEFapsInstallMojo
{
    /**
     * List of includes.
     */
    private final List<String> includes = null;

    /**
     * List of excludes.
     */
    private final List<String> excludes = null;

    /**
     * Executes the install goal.
     *
     * @throws MojoExecutionException if installation failed
     */
    public void execute()
        throws MojoExecutionException
    {
        this.init();

        try {
            final Application appl = Application.getApplicationFromSource(
                    this.getVersionFile(),
                    this.getClasspathElements(),
                    this.getEFapsDir(),
                    this.includes,
                    this.excludes,
                    this.getTypeMapping());

            // install applications
            if (appl != null) {
                appl.updateLastVersion(this.getUserName(), this.getPassWord());
            }
        } catch (final Exception e) {
            throw new MojoExecutionException("Could not execute SourceInstall script", e);
        }
    }
}

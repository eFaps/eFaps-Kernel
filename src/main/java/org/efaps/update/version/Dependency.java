/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.update.version;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.efaps.update.util.InstallationException;

/**
 * Defines a dependency for an eFaps application. Existing dependency could be
 * resolved.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Dependency
{
    /**
     * Group identifier.
     */
    private final String groupId;

    /**
     * Artifact identifier.
     */
    private final String artifactId;

    /**
     * Version.
     */
    private final String version;

    /**
     * Link to the class file which is defined by this dependency.
     *
     * @see #resolve()
     */
    private File jarFile;

    /**
     *
     * @param _groupId      group ID of the dependency
     * @param _artifactId   artifact ID of the dependency
     * @param _version      version of the dependency
     */
    Dependency(final String _groupId,
               final String _artifactId,
               final String _version)
    {
        this.groupId = _groupId;
        this.artifactId = _artifactId;
        this.version = _version;
    }

    /**
     * Resolves this dependency.
     *
     * @throws InstallationException if dependency could not be resolved
     *                               because the ivy settings could not be
     *                               loaded
     */
    public void resolve()
        throws InstallationException
    {
        if (true)  {
            final IvySettings ivySettings = new IvySettings();
            try  {
                ivySettings.load(this.getClass().getResource("/org/efaps/update/version/ivy.xml"));
            } catch (final IOException e)  {
                throw new InstallationException("IVY setting file could not be read", e);
            } catch (final ParseException e)  {
                throw new InstallationException("IVY setting file could not be parsed", e);
            }

            final Ivy ivy = Ivy.newInstance(ivySettings);
            ivy.getLoggerEngine().pushLogger(new IvyOverSLF4JLogger());

            final Map<String, String> attr = new HashMap<String, String>();
            attr.put("changing", "true");

            final ModuleRevisionId modRevId = ModuleRevisionId.newInstance(this.groupId,
                                                                           this.artifactId,
                                                                           this.version,
                                                                           attr);

            final ResolveOptions options = new ResolveOptions();
            options.setConfs(new String[] {"runtime"});


            final ResolvedModuleRevision resModRev = ivy.findModule(modRevId);

            Artifact dw = null;
            for (final Artifact artifact : resModRev.getDescriptor().getAllArtifacts())  {
                if ("jar".equals(artifact.getType()))  {
                    dw = artifact;
                    break;
                }
            }

            final DownloadOptions dwOptions = new DownloadOptions();

//            dwOptions.setLog(DownloadOptions.LOG_QUIET);

            final ArtifactOrigin ao = resModRev.getArtifactResolver().locate(dw);
            resModRev.getArtifactResolver().getRepositoryCacheManager().clean();

            final ArtifactDownloadReport adw = resModRev.getArtifactResolver().download(ao, dwOptions);

            this.jarFile = adw.getLocalFile();
        }
    }

    /**
     * Returns the related Jar file of this dependency.
     *
     * @return Jar file of this dependency
     * @see #jarFile
     */
    public File getJarFile()
    {
        return this.jarFile;
    }

    /**
     * Returns the information about this dependency as string representation.
     *
     * @return string representation
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("groupId", this.groupId)
                .append("artifactId", this.artifactId)
                .append("version", this.version)
                .append("jarFile", this.jarFile)
                .toString();
    }
}

/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.CallMethod;
import org.apache.commons.digester3.annotations.rules.CallParam;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.efaps.update.Profile;
import org.efaps.update.util.InstallationException;

/**
 * Defines a dependency for an eFaps application. Existing dependency could be
 * resolved.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@ObjectCreate(pattern = "install/dependencies/dependency")
public class Dependency
{
    /**
     * Group identifier.
     */
    @BeanPropertySetter(pattern = "install/dependencies/dependency/groupId")
    private String groupId;


    /**
     * Artifact identifier.
     */
    @BeanPropertySetter(pattern = "install/dependencies/dependency/artifactId")
    private String artifactId;

    /**
     * Version.
     */
    @BeanPropertySetter(pattern = "install/dependencies/dependency/version")
    private String version;

    /**
     * Link to the class file which is defined by this dependency.
     *
     * @see #resolve()
     */
    private File jarFile;

    /**
     * Order position of this dependency.
     */
    @SetProperty(pattern = "install/dependencies/dependency/", attributeName = "order")
    private Integer order;

    /**
     * List of related profiles.
     */
    private final Set<String> profileNames = new HashSet<String>();

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

        final ArtifactOrigin ao = resModRev.getArtifactResolver().locate(dw);
        resModRev.getArtifactResolver().getRepositoryCacheManager().clean();

        final ArtifactDownloadReport adw = resModRev.getArtifactResolver().download(ao, dwOptions);

        this.jarFile = adw.getLocalFile();
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
     * Getter method for the instance variable {@link #order}.
     *
     * @return value of instance variable {@link #order}
     */
    public Integer getOrder()
    {
        return this.order;
    }

    /**
     * Setter method for instance variable {@link #order}.
     *
     * @param _order value for instance variable {@link #order}
     */

    public void setOrder(final Integer _order)
    {
        this.order = _order;
    }

    /**
     * Getter method for the instance variable {@link #groupId}.
     *
     * @return value of instance variable {@link #groupId}
     */
    public String getGroupId()
    {
        return this.groupId;
    }


    /**
     * Setter method for instance variable {@link #groupId}.
     *
     * @param _groupId value for instance variable {@link #groupId}
     */

    public void setGroupId(final String _groupId)
    {
        this.groupId = _groupId;
    }


    /**
     * Getter method for the instance variable {@link #artifactId}.
     *
     * @return value of instance variable {@link #artifactId}
     */
    public String getArtifactId()
    {
        return this.artifactId;
    }


    /**
     * Setter method for instance variable {@link #artifactId}.
     *
     * @param _artifactId value for instance variable {@link #artifactId}
     */

    public void setArtifactId(final String _artifactId)
    {
        this.artifactId = _artifactId;
    }


    /**
     * Getter method for the instance variable {@link #version}.
     *
     * @return value of instance variable {@link #version}
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Setter method for instance variable {@link #version}.
     *
     * @param _version value for instance variable {@link #version}
     */

    public void setVersion(final String _version)
    {
        this.version = _version;
    }

    /**
     * @param _name name to add
     */
    @CallMethod(pattern = "install/dependencies/dependency/profiles/profile")
    public void addProfileName(@CallParam(pattern = "install/dependencies/dependency/profiles/profile",
                                                attributeName = "name") final String _name)
    {
        this.profileNames.add(_name);
    }

    /**
     * Get the profiles for this dependency. In case that there are no profiles
     * defined in {@link #profileNames} it will return a set containing the
     * default profile.
     *
     * @return profiles applied for this dependency
     */
    public Set<Profile> getProfiles()
    {
        final Set<Profile> ret = new HashSet<Profile>();
        if (this.profileNames.isEmpty()) {
            ret.add(Profile.getDefaultProfile());
        } else {
            for (final String name : this.profileNames) {
                ret.add(Profile.getProfile(name));
            }
        }
        return ret;
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
                .append("order", this.order)
                .append("jarFile", this.jarFile)
                .toString();
    }
}

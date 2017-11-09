/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.update.schema.program;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * ABstract class for all kind of sources. eg. java, css, js.
 *
 * @author The eFaps Team
 */
public abstract class AbstractSourceUpdate
    extends AbstractUpdate
{

    /**
     * Constructor setting the Name of the Type to be imported/updated.
     *
     * @param _installFile the install file
     * @param _modelTypeName name of the type
     */
    protected AbstractSourceUpdate(final InstallFile _installFile,
                                   final String _modelTypeName)
    {
        this(_installFile, _modelTypeName, null);
    }

    /**
     * Constructor setting the Name of the Type to be imported/updated.
     *
     * @param _installFile the install file
     * @param _modelTypeName name of the type
     * @param _linkTypes set of links
     */
    protected AbstractSourceUpdate(final InstallFile _installFile,
                                   final String _modelTypeName,
                                   final Set<Link> _linkTypes)
    {
        super(_installFile, _modelTypeName, _linkTypes);
    }

    /**
     * Get the Version of this Update. Override id to use other than 1.
     *
     * @return always 1
     */
    protected Long getVersion()
    {
        return new Long(1);
    }

    /**
     * Throws always a new error because not allowed to call.
     *
     * @return nothing
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        throw new Error("not allowed");
    }

    /**
     * Class used as the definition for one source.
     */
    public abstract class AbstractSourceDefinition
        extends AbstractDefinition
    {
        /**
         * Instance variable holding the URL to the file to be imported.
         */
        private InstallFile installFile;

        /**
         * Constructor to defined the URL in {@link #fileUrl} to the file and
         * calculating the name of the source object (file url minus root url).
         * The path separators are replaces by points.
         *
         * @param _installFile the install file
         */
        protected AbstractSourceDefinition(final InstallFile _installFile)
        {
            // searched by attribute Name
            super("Name");
            this.installFile = _installFile;
        }

        /**
         * Updates / creates the instance in the database. If a file
         * name is given, this file is checked in
         *
         * @param _step             current update step
         * @param _allLinkTypes     set of links
         * @return the multi valued map
         * @throws InstallationException on error
         */
        @Override
        public MultiValuedMap<String, String>  updateInDB(final UpdateLifecycle _step,
                                                          final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            final MultiValuedMap<String, String> ret = super.updateInDB(_step, _allLinkTypes);
            if (_step == UpdateLifecycle.EFAPS_UPDATE && getValue("Name") != null)  {
                final Checkin checkin = new Checkin(getInstance());
                try {
                    touch();
                    final InputStream in = getInstallFile().getUrl().openStream();
                    checkin.executeWithoutAccessCheck(getValue("Name"),
                                                      in,
                                                      in.available());
                    in.close();
                } catch (final IOException e) {
                    throw new InstallationException("updateInDB.IOException", e);
                } catch (final EFapsException e) {
                    throw new InstallationException("EFapsException", e);
                }
            }
            return ret;
        }

        /**
         * Touch the main instance to register the update.
         *
         * @throws InstallationException the installation exception
         */
        protected void touch()
            throws InstallationException
        {
            try {
                final String tmpUUID = getValue(CIAdminProgram.Abstract.UUID.name);
                if (StringUtils.isNotEmpty(tmpUUID)) {
                    final Update update = new Update(getInstance());
                    update.add(CIAdminProgram.Abstract.UUID, "TMP");
                    update.executeWithoutAccessCheck();

                    final Update update2 = new Update(getInstance());
                    update2.add(CIAdminProgram.Abstract.UUID, tmpUUID);
                    update2.executeWithoutAccessCheck();
                }
            } catch (final EFapsException e) {
                throw new InstallationException("Catched", e);
            }
            registerRevision(getFileApplication(), getInstallFile(), getInstance());
        }

        @Override
        public boolean isValidVersion(final JexlContext _jexlContext)
            throws InstallationException
        {
            boolean ret = false;
            try {
                ret = getDataModelTypeName() != null && Type.isInitialized()
                                && Type.get(getDataModelTypeName()) != null
                                && !Type.get(getDataModelTypeName()).getAttributes().isEmpty()
                                && Type.get(getDataModelTypeName()).getStoreId() > 0
                                && Context.getThreadContext().getPerson() != null;
            } catch (final EFapsException e) {
                throw new InstallationException("Could not validate the version.", e);
            }
            return ret;
        }

        /**
         * Method returns a String representation of this class.
         * @return String representation
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("installFile", this.installFile)
                .toString();
        }

        /**
         * Getter method for the instance variable {@link #installFile}.
         *
         * @return value of instance variable {@link #installFile}
         */
        public InstallFile getInstallFile()
        {
            return this.installFile;
        }

        /**
         * Setter method for instance variable {@link #installFile}.
         *
         * @param _installFile value for instance variable {@link #installFile}
         */
        public void setInstallFile(final InstallFile _installFile)
        {
            this.installFile = _installFile;
        }
    }
}

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

package org.efaps.update.schema.program;

import java.util.Set;

import org.efaps.db.Instance;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.schema.program.esjp.ESJPImporter;
import org.efaps.update.util.InstallationException;

/**
 * Handles the import / update of ESJP's (eFaps stored Java Programs) for eFaps
 * read from a plain Java source code file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JavaUpdate
    extends AbstractSourceUpdate
{
    /**
     * Default constructor to initialize this ESJP update instance for given
     * <code>_url</code>.
     *
     * @param _url  url to the file
     */
    public JavaUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Program_Java");
    }

    /**
     * If the extension of the file is <code>.java</code>, the method returns
     * an instance of this class. The instance of this class owns one definition
     * instance where the code and the name is defined.
     *
     * @param _url      URL of the file depending of the root URL
     * @return Java update definition read by digester
     */
    public static JavaUpdate readFile(final InstallFile _installFile)
    {
        final JavaUpdate ret = new JavaUpdate(_installFile);
        final JavaDefinition definition = ret.new JavaDefinition(_installFile);
        ret.addDefinition(definition);
        return ret;
    }

    /**
     * The Java definition holds the code and the name of the Java class.
     */
    public class JavaDefinition
        extends AbstractSourceDefinition
    {
        /**
         * Importer for the ESJP.
         */
        private ESJPImporter importer = null;

        /**
         * Is update of the given esjp allowed.
         */
        private boolean updateAllowed = true;

        /**
         * @param _url URL to the java file
         */
        protected JavaDefinition(final InstallFile _installFile)
        {
            super(_installFile);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the Java source code could not be
         *                               read or the file could not be accessed
         *                               because of the wrong URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.importer == null) {
                this.importer = new ESJPImporter(getInstallFile());
            }
            setName(this.importer.getProgramName());

            if (this.importer.getEFapsUUID() != null) {
                addValue("UUID", this.importer.getEFapsUUID().toString());
            }

            if (this.importer.getRevision() != null) {
                addValue("Revision", this.importer.getRevision());
            }
            if (getInstance() == null) {
                final Instance instTmp = this.importer.searchInstance();
                setInstance(instTmp);
                if (instTmp == null) {
                    this.updateAllowed = true;
                } else {
                    this.updateAllowed = this.importer.isUpdate();
                }
            }
        }

        /**
         * The method overwrites the method from the super class, because a
         * check in of the Java source code is needed after the update in the
         * database.
         *
         * @param _step             current update step
         * @param _allLinkTypes     all link types to update
         * @throws InstallationException on error
         */
        @Override
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                if (this.updateAllowed) {
                    AbstractUpdate.LOG.info("    Update {} '{}'", getInstance().getType().getName(),
                                    this.importer.getProgramName());
                    this.importer.updateDB(getInstance());
                } else {
                    AbstractUpdate.LOG.info("    No Update set for esjp: {}", this.importer.getProgramName());
                }
            } else  {
                super.updateInDB(_step, _allLinkTypes);
            }
        }
    }
}

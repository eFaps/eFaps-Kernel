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

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.schema.program.staticsource.BPMImporter;
import org.efaps.update.util.InstallationException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class BPMUpdate
    extends AbstractSourceUpdate
{

    /**
     * Instantiates a new BPM update.
     *
     * @param _installFile the install file
     */
    protected BPMUpdate(final InstallFile _installFile)
    {
        super(_installFile, CIAdminProgram.BPM.getType().getName());
    }

    /**
     * Read the file.
     *
     * @param _installFile the install file
     * @return CSSUpdate
     */
    public static BPMUpdate readFile(final InstallFile _installFile)
    {
        final BPMUpdate ret = new BPMUpdate(_installFile);
        final BPMDefinition definition = ret.new BPMDefinition(_installFile);
        ret.addDefinition(definition);
        return ret;
    }

    /**
    *
    */
    public class BPMDefinition
        extends AbstractSourceDefinition
    {

        /**
         * Importer for the BPM.
         */
        private BPMImporter importer = null;

        /**
         * Constructor.
         *
         * @param _installFile the install file
         */
        public BPMDefinition(final InstallFile _installFile)
        {
            super(_installFile);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the XSLT source code could not be
         *             read or the file could not be accessed because of the
         *             wrong URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.importer == null) {
                this.importer = new BPMImporter(getInstallFile());
            }
            setName(this.importer.getProgramName());

            if (this.importer.getEFapsUUID() != null) {
                addValue("UUID", this.importer.getEFapsUUID().toString());
            }

            if (getInstance() == null) {
                setInstance(this.importer.searchInstance());
            }

            if (getFileApplication() == null && this.importer.getApplication() != null) {
                setFileApplication(this.importer.getApplication());
            }
        }
    }
}

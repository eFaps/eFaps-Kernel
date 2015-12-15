/*
 * Copyright 2003 - 2015 The eFaps Team
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
import org.efaps.update.schema.program.staticsource.XSLImporter;
import org.efaps.update.util.InstallationException;

/**
 * TODO description!.
 *
 * @author The eFaps Team
 */
public class XSLUpdate
    extends AbstractSourceUpdate
{

    /**
     * Constructor.
     *
     * @param _installFile the install file
     */
    protected XSLUpdate(final InstallFile _installFile)
    {
        super(_installFile, CIAdminProgram.XSL.getType().getName());
    }

    /**
     * Read the file.
     *
     * @param _installFile the install file
     * @return XSLUpdate
     */
    public static XSLUpdate readFile(final InstallFile _installFile)
    {
        final XSLUpdate ret = new XSLUpdate(_installFile);
        final XSLDefinition definition = ret.new XSLDefinition(_installFile);
        ret.addDefinition(definition);
        return ret;
    }

    /**
     * The Class XSLDefinition.
     *
     * @author The eFaps Team
     */
    public class XSLDefinition
        extends AbstractSourceDefinition
    {

        /**
         * Importer for the css.
         */
        private XSLImporter sourceCode = null;

        /**
         * Constructor.
         *
         * @param _installFile the install file
         */
        public XSLDefinition(final InstallFile _installFile)
        {
            super(_installFile);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the XSLT source code could not be
         *                               read or the file could not be accessed
         *                               because of the wrong URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.sourceCode == null) {
                this.sourceCode = new XSLImporter(getInstallFile());
            }
            setName(this.sourceCode.getProgramName());

            if (this.sourceCode.getEFapsUUID() != null) {
                addValue("UUID", this.sourceCode.getEFapsUUID().toString());
            }

            if (getInstance() == null) {
                setInstance(this.sourceCode.searchInstance());
            }
        }
    }
}

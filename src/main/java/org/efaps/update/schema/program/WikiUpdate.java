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

import java.util.HashSet;
import java.util.Set;

import org.efaps.update.Install.InstallFile;
import org.efaps.update.schema.program.staticsource.WikiImporter;
import org.efaps.update.util.InstallationException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class WikiUpdate
    extends AbstractSourceUpdate
{

    /**
     * Link from Wiki to other Wiki.
     */
    private static final Link LINK2WIKI = new Link("Admin_Program_Wiki2Wiki", "From", "Admin_Program_Wiki", "To");

    /**
     * Set off all links for this JasperReportUpdate.
     */
    private static final Set<Link> ALLLINKS = new HashSet<>();

    static {
        WikiUpdate.ALLLINKS.add(WikiUpdate.LINK2WIKI);
    }

    /**
     * Instantiates a new wiki update.
     *
     * @param _installFile the install file
     */
    public WikiUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Program_Wiki", WikiUpdate.ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _installFile the install file
     * @return JavaScriptUpdate
     */
    public static WikiUpdate readFile(final InstallFile _installFile)
    {
        final WikiUpdate ret = new WikiUpdate(_installFile);
        final WikiDefinition definition = ret.new WikiDefinition(_installFile);
        ret.addDefinition(definition);
        return ret;
    }

    /**
     * Definition for the JasperReport.
     *
     */
    public class WikiDefinition
        extends AbstractSourceDefinition
    {

        /**
         * Importer for the JasperReport.
         */
        private WikiImporter importer = null;

        /**
         * Construtor.
         *
         * @param _installFile the install file
         */
        public WikiDefinition(final InstallFile _installFile)
        {
            super(_installFile);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the source code for the Jasper
         *             Report could not be read or file could not be accessed
         *             because of the wrong URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.importer == null) {
                this.importer = new WikiImporter(getInstallFile());
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

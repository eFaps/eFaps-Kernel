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
import java.util.HashSet;
import java.util.Set;

import org.efaps.update.Install.InstallFile;
import org.efaps.update.schema.program.jasperreport.JasperReportImporter;
import org.efaps.update.util.InstallationException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class JasperReportUpdate extends AbstractSourceUpdate
{

    /**
     * Link from JavaScript extending JavaScript.
     */
    private static final Link LINK2SUPER = new Link("Admin_Program_JasperReport2JasperReport", "From",
                                                    "Admin_Program_JasperReport", "To");

    /**
     * Set off all links for this JasperReportUpdate.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static {
        JasperReportUpdate.ALLLINKS.add(JasperReportUpdate.LINK2SUPER);
    }

    /**
     * Instantiates a new jasper report update.
     *
     * @param _installFile the install file
     */
    public JasperReportUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Program_JasperReport", JasperReportUpdate.ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _installFile the install file
     * @return JavaScriptUpdate
     */
    public static JasperReportUpdate readFile(final InstallFile _installFile)
    {
        final JasperReportUpdate ret = new JasperReportUpdate(_installFile);
        final JasperReportDefinition definition = ret.new JasperReportDefinition(_installFile);
        ret.addDefinition(definition);
        return ret;
    }

    /**
     * Definition for the JasperReport.
     *
     */
    public class JasperReportDefinition extends AbstractSourceDefinition
    {

        /**
         * Importer for the JasperReport.
         */
        private JasperReportImporter jrxml = null;

        /**
         * Construtor.
         *
         * @param _installFile the install file
         */
        public JasperReportDefinition(final InstallFile _installFile)
        {
            super(_installFile);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the source code for the Jasper
         *                               Report could not be read or file could
         *                               not be accessed because of the wrong
         *                               URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.jrxml == null) {
                this.jrxml = new JasperReportImporter(getInstallFile());
            }
            setName(this.jrxml.getProgramName());

            if (this.jrxml.getEFapsUUID() != null) {
                addValue("UUID", this.jrxml.getEFapsUUID().toString());
            }

            if (getInstance() == null) {
                setInstance(this.jrxml.searchInstance());
            }
        }
    }

}

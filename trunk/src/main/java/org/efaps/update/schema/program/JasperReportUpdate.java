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

package org.efaps.update.schema.program;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.efaps.update.schema.program.jasperreport.JasperReportImporter;
import org.efaps.update.util.InstallationException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
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
     * @param _url URL of the file
     *
     */
    public JasperReportUpdate(final URL _url)
    {
        super(_url, "Admin_Program_JasperReport", JasperReportUpdate.ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _url URL to the file
     * @return JavaScriptUpdate
     */
    public static JasperReportUpdate readFile(final URL _url)
    {
        final JasperReportUpdate ret = new JasperReportUpdate(_url);
        final JasperReportDefinition definition = ret.new JasperReportDefinition(_url);
        ret.addDefinition(definition);
        return ret;
    }

    /**
     * Definition for the JasperReport.
     *
     */
    public class JasperReportDefinition extends SourceDefinition
    {

        /**
         * Importer for the JasperReport.
         */
        private JasperReportImporter jrxml = null;

        /**
         * Construtor.
         *
         * @param _url URL to the JasperReport file
         *
         */
        public JasperReportDefinition(final URL _url)
        {
            super(_url);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the source code for the Jasper
         *                               Report could not be read or file could
         *                               not be accessed because of the wrong
         *                               URL
         */
        @Override()
        protected void searchInstance()
            throws InstallationException
        {
            if (this.jrxml == null) {
                this.jrxml = new JasperReportImporter(getUrl());
            }
            setName(this.jrxml.getProgramName());

            if (this.jrxml.getEFapsUUID() != null) {
                addValue("UUID", this.jrxml.getEFapsUUID().toString());
            }

//            if (this.jrxml.getExtendSource() != null) {
//                addLink(JasperReportUpdate.LINK2SUPER, new LinkInstance(this.jrxml.getExtendSource()));
//            }

            if (this.instance == null) {
                this.instance = this.jrxml.searchInstance();
            }
        }
    }

}

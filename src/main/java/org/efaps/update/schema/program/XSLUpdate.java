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

package org.efaps.update.schema.program;



import java.net.URL;

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.schema.program.staticsource.XSLImporter;
import org.efaps.update.util.InstallationException;

/**
 * TODO description
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class XSLUpdate
    extends AbstractSourceUpdate
{

    /**
     * Constructor.
     *
     * @param _url URL of the file
     */
    protected XSLUpdate(final URL _url)
    {
        super(_url, CIAdminProgram.XSL.getType().getName());
    }

    /**
     * Read the file.
     *
     * @param _url URL to the file
     * @return XSLUpdate
     */
    public static XSLUpdate readFile(final URL _url)
    {
        final XSLUpdate ret = new XSLUpdate(_url);
        final XSLDefinition definition = ret.new XSLDefinition(_url);
        ret.addDefinition(definition);
        return ret;
    }

    public class XSLDefinition
        extends SourceDefinition
    {

        /**
         * Importer for the css.
         */
        private XSLImporter sourceCode = null;

        /**
         * Constructor.
         *
         * @param _url URL of the file
         */
        public XSLDefinition(final URL _url)
        {
            super(_url);
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
                this.sourceCode = new XSLImporter(getUrl());
            }
            setName(this.sourceCode.getProgramName());

            if (this.sourceCode.getEFapsUUID() != null) {
                addValue("UUID", this.sourceCode.getEFapsUUID().toString());
            }

            if (this.instance == null) {
                this.instance = this.sourceCode.searchInstance();
            }
        }

        /**
         * {@inheritDoc}
         *
         */
        @Override
        protected String getRevision() throws InstallationException
        {
            if (this.sourceCode == null) {
                this.sourceCode = new XSLImporter(getUrl());
            }
            return this.sourceCode.getRevision();
        }
    }
}

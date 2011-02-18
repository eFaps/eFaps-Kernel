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
import java.util.HashSet;
import java.util.Set;

import org.efaps.update.LinkInstance;
import org.efaps.update.schema.program.staticsource.JavaScriptImporter;
import org.efaps.update.util.InstallationException;

/**
 * Class to update a javascript into eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JavaScriptUpdate extends AbstractSourceUpdate
{

    /**
     * Link from JavaScript extending JavaScript.
     */
    private static final Link LINK2SUPER = new Link("Admin_Program_JavaScript2JavaScript", "From",
                                                    "Admin_Program_JavaScript", "To");

    /**
     * Set off all links for this JavaScriptUpdate.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static {
        JavaScriptUpdate.ALLLINKS.add(JavaScriptUpdate.LINK2SUPER);
    }

    /**
     * Constructor.
     *
     * @param _url URL of the file
     */
    protected JavaScriptUpdate(final URL _url)
    {
        super(_url, "Admin_Program_JavaScript", JavaScriptUpdate.ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _url URL to the file
     * @return JavaScriptUpdate
     */
    public static JavaScriptUpdate readFile(final URL _url)
    {
        final JavaScriptUpdate ret = new JavaScriptUpdate(_url);
        final JavaScriptDefinition definition = ret.new JavaScriptDefinition(_url);
        ret.addDefinition(definition);

        return ret;
    }

    /**
     * Definition for the JavaScript.
     *
     */
    public class JavaScriptDefinition extends SourceDefinition
    {

        /**
         * Importer for the css.
         */
        private JavaScriptImporter sourceCode = null;

        /**
         * Construtor.
         *
         * @param _url URL to the css file
         *
         */
        public JavaScriptDefinition(final URL _url)
        {
            super(_url);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the Javascript source code could
         *                               not be read or the file could not be
         *                               accessed because of the wrong URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.sourceCode == null) {
                this.sourceCode = new JavaScriptImporter(getUrl());
            }
            setName(this.sourceCode.getProgramName());

            if (this.sourceCode.getEFapsUUID() != null) {
                addValue("UUID", this.sourceCode.getEFapsUUID().toString());
            }

            if (this.sourceCode.getExtendSource() != null) {
                addLink(JavaScriptUpdate.LINK2SUPER, new LinkInstance(this.sourceCode.getExtendSource()));
            }

            if (this.instance == null) {
                this.instance = this.sourceCode.searchInstance();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getRevision()
            throws InstallationException
        {
            if (this.sourceCode == null) {
                this.sourceCode = new JavaScriptImporter(getUrl());
            }
            return this.sourceCode.getRevision();
        }
    }
}

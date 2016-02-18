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
import org.efaps.update.LinkInstance;
import org.efaps.update.schema.program.staticsource.JavaScriptImporter;
import org.efaps.update.util.InstallationException;

/**
 * Class to update a javascript into eFaps.
 *
 * @author The eFaps Team
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
     * @param _installFile the install file
     */
    protected JavaScriptUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Program_JavaScript", JavaScriptUpdate.ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _installFile the install file
     * @return JavaScriptUpdate
     */
    public static JavaScriptUpdate readFile(final InstallFile _installFile)
    {
        final JavaScriptUpdate ret = new JavaScriptUpdate(_installFile);
        final JavaScriptDefinition definition = ret.new JavaScriptDefinition(_installFile);
        ret.addDefinition(definition);

        return ret;
    }

    /**
     * Definition for the JavaScript.
     *
     */
    public class JavaScriptDefinition extends AbstractSourceDefinition
    {

        /**
         * Importer for the css.
         */
        private JavaScriptImporter sourceCode = null;

        /**
         * Construtor.
         *
         * @param _installFile the install file
         */
        public JavaScriptDefinition(final InstallFile _installFile)
        {
            super(_installFile);
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
                this.sourceCode = new JavaScriptImporter(getInstallFile());
            }
            setName(this.sourceCode.getProgramName());

            if (this.sourceCode.getEFapsUUID() != null) {
                addValue("UUID", this.sourceCode.getEFapsUUID().toString());
            }

            if (this.sourceCode.getExtendSource() != null) {
                addLink(JavaScriptUpdate.LINK2SUPER, new LinkInstance(this.sourceCode.getExtendSource()));
            }

            if (getInstance() == null) {
                setInstance(this.sourceCode.searchInstance());
            }
        }
    }
}

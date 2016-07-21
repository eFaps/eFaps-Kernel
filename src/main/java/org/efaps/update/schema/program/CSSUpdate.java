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

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.LinkInstance;
import org.efaps.update.schema.program.staticsource.CSSImporter;
import org.efaps.update.util.InstallationException;

/**
 * The class updates programs from type <code>Admin_Program_CSS</code> inside
 * the eFaps database.
 *
 * @author The eFasp Team
 */
public class CSSUpdate
    extends AbstractSourceUpdate
{

    /**
     * Link from CSS extending CSS.
     */
    private static final Link LINK2SUPER = new Link(CIAdminProgram.CSS2CSS.getType().getName(),
                                                    CIAdminProgram.CSS2CSS.From.name,
                                                    CIAdminProgram.CSS.getType().getName(),
                                                    CIAdminProgram.CSS2CSS.To.name);

    /**
     * Set off all links for this cssupdate.
     */
    private static final Set<Link> ALLLINKS = new HashSet<>();
    static {
        CSSUpdate.ALLLINKS.add(CSSUpdate.LINK2SUPER);
    }

    /**
     * Constructor.
     *
     * @param _installFile the install file
     */
    protected CSSUpdate(final InstallFile _installFile)
    {
        super(_installFile, CIAdminProgram.CSS.getType().getName(), CSSUpdate.ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _installFile the install file
     * @return CSSUpdate
     */
    public static CSSUpdate readFile(final InstallFile _installFile)
    {

        final CSSUpdate ret = new CSSUpdate(_installFile);
        final CSSDefinition definition = ret.new CSSDefinition(_installFile);
        ret.addDefinition(definition);
        return ret;
    }

    /**
     *
     */
    public class CSSDefinition
        extends AbstractSourceDefinition
    {

        /**
         * Importer for the css.
         */
        private CSSImporter importer = null;

        /**
         * Construtor.
         *
         * @param _installFile the install file
         */
        public CSSDefinition(final InstallFile _installFile)
        {
            super(_installFile);
        }

        /**
         * Search the instance.
         *
         * @throws InstallationException if the Java source code could not be read or
         *             the file could not be accessed because of the wrong URL
         */
        @Override
        protected void searchInstance()
            throws InstallationException
        {
            if (this.importer == null) {
                this.importer = new CSSImporter(getInstallFile());
            }
            setName(this.importer.getProgramName());

            if (this.importer.getEFapsUUID() != null) {
                addValue("UUID", this.importer.getEFapsUUID().toString());
            }

            if (this.importer.getExtendSource() != null) {
                addLink(CSSUpdate.LINK2SUPER,
                                new LinkInstance(this.importer.getExtendSource()));
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

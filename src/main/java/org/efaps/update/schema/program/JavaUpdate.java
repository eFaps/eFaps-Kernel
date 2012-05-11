/*
 * Copyright 2003 - 2012 The eFaps Team
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
import java.util.Set;

import org.efaps.update.AbstractUpdate;
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
    public JavaUpdate(final URL _url)
    {
        super(_url, "Admin_Program_Java");
    }

    /**
     * If the extension of the file is <code>.java</code>, the method returns
     * an instance of this class. The instance of this class owns one definition
     * instance where the code and the name is defined.
     *
     * @param _url      URL of the file depending of the root URL
     * @return Java update definition read by digester
     */
    public static JavaUpdate readFile(final URL _url)
    {
        final JavaUpdate ret = new JavaUpdate(_url);
        final JavaDefinition definition = ret.new JavaDefinition(_url);
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
        private ESJPImporter javaCode = null;

        /**
         * @param _url URL to the java file
         */
        protected JavaDefinition(final URL _url)
        {
            super(_url);
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
            if (this.javaCode == null) {
                this.javaCode = new ESJPImporter(getUrl());
            }
            setName(this.javaCode.getProgramName());

            if (this.javaCode.getEFapsUUID() != null) {
                addValue("UUID", this.javaCode.getEFapsUUID().toString());
            }

            if (this.javaCode.getRevision() != null) {
                addValue("Revision", this.javaCode.getRevision());
            }
            if (getInstance() == null) {
                setInstance(this.javaCode.searchInstance());
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
                AbstractUpdate.LOG.info("    Update {} '{}'", getInstance().getType().getName(),
                                this.javaCode.getProgramName());
                this.javaCode.updateDB(getInstance());
            } else  {
                super.updateInDB(_step, _allLinkTypes);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getRevision()
            throws InstallationException
        {
            if (this.javaCode == null) {
                this.javaCode = new ESJPImporter(getUrl());
            }
            return this.javaCode.getRevision();
        }
    }
}

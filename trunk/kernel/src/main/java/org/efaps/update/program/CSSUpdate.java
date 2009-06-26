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

package org.efaps.update.program;

import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_CSS;
import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_CSS2CSS;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.staticsource.CSSImporter;
import org.efaps.update.LinkInstance;
import org.efaps.util.EFapsException;

/**
 * The class updates programs from type <code>Admin_Program_CSS</code> inside
 * the eFaps database.
 *
 * @author jmox
 * @version $Id$
 */
public class CSSUpdate extends AbstractSourceUpdate
{

    /**
     * Link from CSS extending CSS.
     */
    private static final Link LINK2SUPER = new Link(Type.get(ADMIN_PROGRAM_CSS2CSS).getName(), "From", Type.get(
                    ADMIN_PROGRAM_CSS).getName(), "To");

    /**
     * Set off all links for this cssupdate.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static {
        ALLLINKS.add(LINK2SUPER);
    }

    /**
     * Constructor.
     *
     * @param _url URL of the file
     */
    protected CSSUpdate(final URL _url)
    {
        super(_url, Type.get(ADMIN_PROGRAM_CSS).getName(), ALLLINKS);
    }

    /**
     * Read the file.
     *
     * @param _url URL to the file
     * @return CSSUpdate
     */
    public static CSSUpdate readFile(final URL _url)
    {

        final CSSUpdate ret = new CSSUpdate(_url);
        final CSSDefinition definition = ret.new CSSDefinition(_url);
        ret.addDefinition(definition);
        return ret;
    }

    /**
   *
   */
    public class CSSDefinition extends SourceDefinition
    {

        /**
         * Importer for the css.
         */
        private CSSImporter sourceCode = null;

        /**
         * Construtor.
         *
         * @param _url URL to the css file
         *
         */
        public CSSDefinition(final URL _url)
        {
            super(_url);
        }

        /**
         * Search the instance.
         *
         * @throws EFapsException if the Java source code could not be read or
         *             the file could not be accessed because of the wrong URL
         */
        @Override
        protected void searchInstance() throws EFapsException
        {
            if (this.sourceCode == null) {
                this.sourceCode = new CSSImporter(getUrl());
            }
            setName(this.sourceCode.getProgramName());

            if (this.sourceCode.getEFapsUUID() != null) {
                addValue("UUID", this.sourceCode.getEFapsUUID().toString());
            }

            if (this.sourceCode.getExtendSource() != null) {
                addLink(LINK2SUPER, new LinkInstance(this.sourceCode.getExtendSource()));
            }

            if (this.instance == null) {
                this.instance = this.sourceCode.searchInstance();
            }
        }
    }
}

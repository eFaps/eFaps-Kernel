/*
 * Copyright 2003 - 2010 The eFaps Team
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

import org.efaps.update.schema.AbstractFileUpdate;

/**
 * Handles the import / update of Jasper Images for eFaps read from a XML
 * configuration item file (for the meta data) and the image itself as binary
 * file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JasperImageUpdate
    extends AbstractFileUpdate
{

    /**
     * Default constructor to initialize this Jasper report image update
     * instance for given <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public JasperImageUpdate(final URL _url)
    {
        super(_url, "Admin_Program_JasperImage");
    }

    /**
     * Creates new instance of class
     * {@link JasperImageUpdate.JasperImageDefinition}.
     *
     * @return new definition instance
     * @see JasperImageUpdate.JasperImageDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new JasperImageDefinition();
    }

    /**
     * Definition for a Jasper Image.
     */
    protected class JasperImageDefinition
        extends AbstractFileDefinition
    {
    }
}

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
 * Revision:        $Rev: 3511 $
 * Last Changed:    $Date: 2009-12-20 11:11:53 -0500 (Sun, 20 Dec 2009) $
 * Last Changed By: $Author: tim.moxter $
 */

package org.efaps.update.schema.program;

import java.net.URL;

import org.efaps.update.schema.AbstractFileUpdate;

/**
 * Handles the import / update of wiki Images for eFaps read from a XML
 * configuration item file (for the meta data) and the image itself as binary
 * file.
 *
 * @author The eFaps Team
 * @version $Id: JasperImageUpdate.java 3511 2009-12-20 16:11:53Z tim.moxter $
 */
public class WikiImageUpdate
    extends AbstractFileUpdate
{
    /**
     * Default constructor to initialize this Jasper report image update
     * instance for given <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public WikiImageUpdate(final URL _url)
    {
        super(_url, "Admin_Program_WikiImage");
    }

    /**
     * Creates new instance of class
     * {@link WikiImageUpdate.WikiImageDefinition}.
     *
     * @return new definition instance
     * @see WikiImageUpdate.JasperImageDefinition
     */
    @Override()
    protected AbstractDefinition newDefinition()
    {
        return new WikiImageDefinition();
    }

    /**
     * Definition for a Wiki Image.
     *
     */
    private class WikiImageDefinition
        extends AbstractFileDefinition
    {
    }
}

/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.LinkInstance;
import org.efaps.update.schema.AbstractFileUpdate;
import org.efaps.update.schema.ui.ImageUpdate;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of BPM Images for eFaps read from a XML
 * configuration item file (for the meta data) and the image itself as binary
 * file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class BPMImageUpdate
    extends AbstractFileUpdate
{

    /**
     * Link from bpm to related image.
     */
    private static final Link BPM2IMAGE = new Link("Admin_Program_BPM2Image",
                   "ToLink",
                   "Admin_Program_BPM", "FromLink");

    /**
     * All specific used links for images.
     */
    private static final Set <Link> ALLLINKS = new HashSet<Link>();
    static  {
        BPMImageUpdate.ALLLINKS.add(BPMImageUpdate.BPM2IMAGE);
    }

    /**
     * Default constructor to initialize this BPM report image update
     * instance for given <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public BPMImageUpdate(final URL _url)
    {
        super(_url, "Admin_Program_BPMImage", BPMImageUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class
     * {@link BPMImageUpdate.BPMImageDefinition}.
     *
     * @return new definition instance
     * @see JasperImageUpdate.JasperImageDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new BPMImageDefinition();
    }

    /**
     * Definition for a Jasper Image.
     */
    protected class BPMImageDefinition
        extends AbstractFileDefinition
    {
        /**
         * Interprets the image specific part of the XML configuration item
         * file. Following information is read:
         * <ul>
         * <li>name of the type for which this image is defined (as type
         *     image); interpreted as {@link ImageUpdate#LINK2TYPE link}</li>
         * </ul>
         *
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         * @throws EFapsException
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("processId".equals(value)) {
                // assigns a type the image for which this image instance is
                // the type icon
                addLink(BPMImageUpdate.BPM2IMAGE, new LinkInstance(_text));
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

    }
}

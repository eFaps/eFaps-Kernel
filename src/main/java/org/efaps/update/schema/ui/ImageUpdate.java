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

package org.efaps.update.schema.ui;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.update.LinkInstance;
import org.efaps.update.schema.AbstractFileUpdate;

/**
 * Handles the import / update of images for eFaps read from a XML
 * configuration item file (for the meta data) and the image itself as binary
 * file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ImageUpdate
    extends AbstractFileUpdate
{
    /**
     * Link from menu to type as type tree menu.
     */
    private static final Link LINK2TYPE
        = new Link("Admin_UI_LinkIsTypeIconFor",
                   "From",
                   "Admin_DataModel_Type", "To");

    /**
     * All specific used links for images.
     */
    private static final Set <Link> ALLLINKS = new HashSet<Link>();
    static  {
        ImageUpdate.ALLLINKS.add(ImageUpdate.LINK2TYPE);
    }


    /**
     * Default constructor to initialize this image update instance for given
     * <code>_url</code>.
     *
     * @param _url        URL of the file
     */
    public ImageUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Image", ImageUpdate.ALLLINKS);
    }

    /**
     * Creates new instance of class {@link ImageUpdate.ImageDefinition}.
     *
     * @return new definition instance
     * @see ImageUpdate.ImageDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new ImageDefinition();
    }

    /**
     * Handles the definition of one version for an image defined within XML
     * configuration item file.
     */
    protected class ImageDefinition
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
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("type".equals(value))  {
                // assigns a type the image for which this image instance is
                // the type icon
                addLink(ImageUpdate.LINK2TYPE, new LinkInstance(_text));
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }
    }
}

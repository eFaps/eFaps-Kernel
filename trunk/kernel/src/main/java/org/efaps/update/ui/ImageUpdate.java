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

package org.efaps.update.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Checkin;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.UpdateLifecycle;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of images for eFaps read from a XML
 * configuration item file (for the meta data) and the image itself as binary
 * file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ImageUpdate
    extends AbstractUpdate
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
     * Name of the root path used to initialize the path for the image.
     */
    private final String root;

    /**
     * Default constructor to initialize this image update instance for given
     * <code>_url</code>.
     *
     * @param _url        URL of the file
     */
    public ImageUpdate(final URL _url)
    {
        super(_url, "Admin_UI_Image", ImageUpdate.ALLLINKS);
        final String urlStr = _url.toString();
        final int i = urlStr.lastIndexOf("/");
        this.root = urlStr.substring(0, i + 1);
    }

    /**
     * Creates new instance of class {@link ImageDefinition}.
     *
     * @return new definition instance
     * @see ImageDefinition
     */
    @Override()
    protected AbstractDefinition newDefinition()
    {
        return new ImageDefinition();
    }

    /**
     * Handles the definition of one version for an image defined within XML
     * configuration item file.
     */
    private class ImageDefinition
        extends AbstractDefinition
    {
        /**
         * Name of the Image file (including the path) to import.
         */
        private String file;

        /**
         * Interprets the image specific part of the XML configuration item
         * file. Following information is read:
         * <ul>
         * <li>name of the {@link #file}</li>
         * <li>name of the type for which this image is defined (as type
         *     image); interpreted as {@link ImageUpdate#LINK2TYPE link}</li>
         * </ul>
         *
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         */
        @Override()
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("file".equals(value))  {
                this.file = _text;
            } else if ("type".equals(value))  {
                // assigns a type the image for which this image instance is
                // the type icon
                this.addLink(ImageUpdate.LINK2TYPE, new LinkInstance(_text));
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Updates / creates the instance in the database. If a
         * {@link #file file name} is given, this file is checked in the
         * created image instance.
         *
         * @param _step             current update step
         * @param _allLinkTypes     set of all type of links
         * @throws EFapsException if update failed
         */
        @Override()
        protected void updateInDB(final UpdateLifecycle _step,
                                  final Set<Link> _allLinkTypes)
            throws EFapsException
        {
            super.updateInDB(_step, _allLinkTypes);

            if ((_step == UpdateLifecycle.EFAPS_UPDATE) && (this.file != null))  {
                try  {
                    final InputStream in = new URL(ImageUpdate.this.root + this.file).openStream();
                    try  {
                        final Checkin checkin = new Checkin(this.instance);
                        checkin.executeWithoutAccessCheck(this.file,
                                                          in,
                                                          in.available());
                    } finally  {
                        in.close();
                    }
                } catch (final IOException e)  {
                    throw new EFapsException(this.getClass(),
                                             "updateInDB.IOException",
                                             e,
                                             ImageUpdate.this.root + this.file);
                }
            }
        }

        /**
         * Returns a string representation with values of all instance
         * variables of an image definition.
         *
         * @return string representation of this definition of a column
         */
        @Override()
        public String toString()
        {
            return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("file", this.file).toString();
        }
    }
}

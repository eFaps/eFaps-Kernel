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

package org.efaps.update.schema.program;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Checkin;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of Jasper Images for eFaps read from a XML
 * configuration item file  (for the meta data) and the image itself as binary
 * file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JasperImageUpdate
    extends AbstractUpdate
{
    /**
     * Name of the root path used to initialize the path for the image.
     * */
    private final String root;

    /**
     * Default constructor to initialize this Jasper report image update
     * instance for given <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public JasperImageUpdate(final URL _url)
    {
        super(_url, "Admin_Program_JasperImage");
        final String urlStr = _url.toString();
        final int i = urlStr.lastIndexOf("/");
        this.root = urlStr.substring(0, i + 1);
    }

    /**
     * Creates new instance of class
     * {@link JasperImageUpdate.JasperImageDefinition}.
     *
     * @return new definition instance
     * @see JasperImageUpdate.JasperImageDefinition
     */
    @Override()
    protected AbstractDefinition newDefinition()
    {
        return new JasperImageDefinition();
    }

    /**
     * Definition for a Jasper Image.
     *
     */
    private class JasperImageDefinition
        extends AbstractDefinition
    {
        /** Name of the Image file (including the path) to import. */
        private String file = null;

        /**
         * {@inheritDoc}
         */
        @Override()
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("file".equals(value)) {
                this.file = _text;
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override()
        protected void updateInDB(final UpdateLifecycle _step,
                                  final Set<Link> _allLinkTypes)
            throws InstallationException, EFapsException
        {
            super.updateInDB(_step, _allLinkTypes);

            if ((_step == UpdateLifecycle.EFAPS_UPDATE) && (this.file != null))  {
                try {
                    final InputStream in = new URL(JasperImageUpdate.this.root + this.file).openStream();
                    final Checkin checkin = new Checkin(this.instance);
                    checkin.executeWithoutAccessCheck(this.file, in, in.available());
                    in.close();
                } catch (final IOException e) {
                    throw new EFapsException(getClass(), "updateInDB.IOException", e, JasperImageUpdate.this.root
                                    + this.file);
                }
            }
        }

        /**
         * Returns a string representation with values of all instance variables
         * of a field.
         *
         * @return string representation of this definition of a column
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).appendSuper(super.toString()).append("file", this.file).toString();
        }
    }
}

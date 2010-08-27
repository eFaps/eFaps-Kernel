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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Checkin;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * ABstract class for all kind of sources. eg. java, css, js.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractSourceUpdate
    extends AbstractUpdate
{
    /**
     * Constructor setting the Name of the Type to be imported/updated.
     *
     * @param _url URL to the file
     * @param _modelTypeName name of the type
     */
    protected AbstractSourceUpdate(final URL _url,
                                   final String _modelTypeName)
    {
        this(_url, _modelTypeName, null);
    }

    /**
     * Constructor setting the Name of the Type to be imported/updated.
     *
     * @param _url URL to the file
     * @param _modelTypeName name of the type
     * @param _linkTypes set of links
     */
    protected AbstractSourceUpdate(final URL _url,
                                   final String _modelTypeName,
                                   final Set<Link> _linkTypes)
    {
        super(_url, _modelTypeName, _linkTypes);
    }

    /**
     * Get the Version of this Update. Override id to use other than 1.
     *
     * @return always 1
     */
    protected Long getVersion()
    {
        return new Long(1);
    }

    /**
     * Throws always a new error because not allowed to call.
     *
     * @return nothing
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        throw new Error("not allowed");
    }

    /**
     * Class used as the definition for one source.
     */
    public abstract class SourceDefinition
        extends AbstractDefinition
    {
        /**
         * Instance variable holding the URL to the file to be imported.
         */
        private URL fileUrl;

        /**
         * Constructor to defined the URL in {@link #fileUrl} to the file and
         * calculating the name of the source object (file url minus root url).
         * The path separators are replaces by points.
         *
         * @param _fileUrl  URL to the file (incl. root).
         */
        protected SourceDefinition(final URL _fileUrl)
        {
            // searched by attribute Name
            super("Name");
            this.fileUrl = _fileUrl;
        }

        /**
         * Method to get the Revision from the importer.
         * @return revision
         * @throws InstallationException on error
         */
        protected abstract String getRevision() throws InstallationException;

        /**
         * Updates / creates the instance in the database. If a file
         * name is given, this file is checked in
         *
         * @param _step             current update step
         * @param _allLinkTypes     set of links
         * @throws InstallationException on error
         *
         */
        @Override
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            // on update the revision must be set before the super method is called
            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                setFileRevision(getRevision());
            }
            super.updateInDB(_step, _allLinkTypes);

            if ((_step == UpdateLifecycle.EFAPS_UPDATE) && (getValue("Name") != null))  {
                final Checkin checkin = new Checkin(this.instance);
                try {
                    final InputStream in = this.fileUrl.openStream();
                    checkin.executeWithoutAccessCheck(getValue("Name"),
                                                      in,
                                                      in.available());
                    in.close();
                } catch (final IOException e) {
                    throw new InstallationException("updateInDB.IOException", e);
                } catch (final EFapsException e) {
                    throw new InstallationException("EFapsException", e);
                }
            }
        }

        /**
         * This is the getter method for the instance variable
         * {@link #fileUrl}.
         *
         * @return value of instance variable {@link #fileUrl}
         */
        public URL getUrl()
        {
            return this.fileUrl;
        }

        /**
         * This is the setter method for the instance variable
         * {@link #fileUrl}.
         *
         * @param _url  the url to set
         */
        public void setUrl(final URL _url)
        {
            this.fileUrl = _url;
        }

        /**
         * Method returns a String representation of this class.
         * @return String representation
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("url", this.fileUrl)
                .toString();
        }
    }
}

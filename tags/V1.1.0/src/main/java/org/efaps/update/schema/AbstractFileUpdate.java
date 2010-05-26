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


package org.efaps.update.schema;

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
 * Abstract class for the different image updates/inserts.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractFileUpdate
    extends AbstractUpdate
{
    /**
     * Name of the root path used to initialize the path for the image.
     */
    private final String root;

    /**
     * @param _url                  URL to the import definition file
     * @param _dataModelTypeName    name of the datamodel type
     */
    protected AbstractFileUpdate(final URL _url,
                                  final String _dataModelTypeName)
    {
        this(_url, _dataModelTypeName, null);
    }

    /**
     * @param _url                  URL to the import definition file
     * @param _dataModelTypeName    name of the datamodel type
     * @param _links                set of links
     */
    protected AbstractFileUpdate(final URL _url,
                                  final String _dataModelTypeName,
                                  final Set <Link> _links)
    {
        super(_url, _dataModelTypeName, _links);
        final String urlStr = _url.toString();
        final int i = urlStr.lastIndexOf("/");
        this.root = urlStr.substring(0, i + 1);
    }

    /**
     * Abstract image definition.
     */
    protected abstract class AbstractFileDefinition
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
         * @throws InstallationException if update failed
         */
        @Override()
        protected void updateInDB(final UpdateLifecycle _step,
                                  final Set<Link> _allLinkTypes)
            throws InstallationException, EFapsException
        {
            super.updateInDB(_step, _allLinkTypes);

            if ((_step == UpdateLifecycle.EFAPS_UPDATE) && (this.file != null))  {
                try  {
                    final InputStream in = new URL(AbstractFileUpdate.this.root + this.file).openStream();
                    try  {
                        final Checkin checkin = new Checkin(this.instance);
                        checkin.executeWithoutAccessCheck(this.file, in, in.available());
                    } finally  {
                        in.close();
                    }
                } catch (final EFapsException e)  {
                    throw new InstallationException("Check of file '" + AbstractFileUpdate.this.root + this.file
                            + "' failed", e);
                } catch (final IOException e)  {
                    throw new InstallationException("It seems that file '" + AbstractFileUpdate.this.root + this.file
                            + "' does not exists or is not accessable.", e);
                }
            }
        }

        /**
         * Returns a string representation with values of all instance variables
         * of an image definition.
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

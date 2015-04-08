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

package org.efaps.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl2.JexlContext;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DefaultEmptyUpdate
    implements IUpdate
{

    /**
     * Logging instance used to give logging information of this class.
     */
    public static final Logger LOG = LoggerFactory.getLogger(DefaultEmptyUpdate.class);

    /**
     * Url of the update file.
     */
    private final InstallFile installFile;

    /**
     * @param _url url of the update file
     */
    public DefaultEmptyUpdate(final InstallFile _installFile)
    {
        this.installFile = _installFile;
        DefaultEmptyUpdate.LOG.debug("Ignoring file: '{}'", this.installFile.getUrl());
    }

    @Override
    public void updateInDB(final JexlContext _jexlContext,
                           final UpdateLifecycle _step,
                           final Set<Profile> _profiles)
        throws InstallationException
    {
        // nothing will be done at all
    }

    @Override
    public String getFileApplication()
    {
        return "Empty";
    }

    @Override
    public void readXML(final List<String> _tags,
                        final Map<String, String> _attributes,
                        final String _text)
        throws SAXException
    {
        // nothing will be done at all
    }

    /**
     * Getter method for the instance variable {@link #installFile}.
     *
     * @return value of instance variable {@link #installFile}
     */
    @Override
    public InstallFile getInstallFile()
    {
        return this.installFile;
    }
}

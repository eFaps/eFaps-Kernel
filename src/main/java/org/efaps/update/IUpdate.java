/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.jexl2.JexlContext;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.xml.sax.SAXException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public interface IUpdate
{

    /**
     * Runs the update for given eFaps configuration item.
     *
     * @param _jexlContext  context used to evaluate JEXL expressions
     * @param _step         current step of the update life cycle
     * @param _profiles     the Profiles that will be executed
     * @return the multi valued map
     * @throws InstallationException from called update methods
     */
    MultiValuedMap<String, String> updateInDB(JexlContext _jexlContext,
                    UpdateLifecycle _step,
                    Set<Profile> _profiles)
        throws InstallationException;

    /**
     * Name of the file application for which this XML file is defined.
     * @return String containing the name of the application
     */
    String getFileApplication();

    /**
     * Read event for given tags path with attributes and text.
     *
     * @param _tags         tags path as list
     * @param _attributes   map of attributes for current tag
     * @param _text         content text of this tags path
     * @throws SAXException on error
     * @throws EFapsException on error
     */
    void readXML(List<String> _tags,
                 Map<String, String> _attributes,
                 String _text)
        throws SAXException, EFapsException;

    /**
     * @return the URL of the CI.
     */
    InstallFile getInstallFile();

    /**
     * Gets the identifier. This key serves also as sort criteria.
     *
     * @return the identifier
     */
    String getIdentifier();
}

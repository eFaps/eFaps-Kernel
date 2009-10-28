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

package org.efaps.update;

import java.util.List;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;

import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IUpdate
{

    /**
     * The instance method returns the eFaps instance representing the read XML
     * configuration. If not already get from the eFaps database, the
     * information is read. If no instance exists in the database, a new one is
     * automatically created. The method searches for the given universal unique
     * identifier in {@link #uuid} the instance in the eFaps database and stores
     * the result in {@link #instance}. If no object is found in eFaps,
     * {@link #instance} is set to <code>null</code>. A new instance is created
     * in the eFaps DB for given universal unique identifier in {@link #uuid}.
     * The name of the access set is also the universal unique identifier,
     * because the name of access set is first updates in the version
     * definition.<br/>
     * The new created object is stored as instance information in
     * {@link #instance}.
     *
     * @param _jexlContext  context used to evaluate JEXL expressions
     * @param _step         current step of the update life cycle
     * @throws EFapsException from called update methods
     */
    void updateInDB(final JexlContext _jexlContext,
                    final UpdateLifecycle _step)
        throws EFapsException;

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
     */
    void readXML(final List<String> _tags,
                 final Map<String, String> _attributes,
                 final String _text);

}

/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.eql;

import java.util.List;

import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IEsjpSelect
{
    /**
     * Initialize this IEsjpSelect.
     *
     * @param _instances list of instances
     * @param _parameters array of parameters
     * @throws EFapsException on error
     */
    void initialize(final List<Instance> _instances,
                    final String... _parameters)
        throws EFapsException;

    /**
     * @param _instance Instance of the current object
     * @return the value for the given instance
     * @throws EFapsException on error
     */
    Object getValue(final Instance _instance)
        throws EFapsException;

}

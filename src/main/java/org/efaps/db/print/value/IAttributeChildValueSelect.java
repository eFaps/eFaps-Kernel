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

package org.efaps.db.print.value;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.util.EFapsException;

/**
 * Interface for ValueSelects used as Child Value for an attribute select.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IAttributeChildValueSelect
{

    /**
     * @param _attribute    Attribute this value belongs to
     * @param _object       Object retrieved from the eFaps DataBase
     * @return Object
     * @throws EFapsException on error
     */
    Object get(final Attribute _attribute,
               final Object _object)
        throws EFapsException;
}

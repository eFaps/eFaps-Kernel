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

package org.efaps.admin.datamodel.attributetype;

import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractFileType extends AbstractType
{
    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return nothing, because error will be thrown
     * @throws EFapsException allways
     *
     */
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        throw new EFapsException(AbstractFileType.class, "readValue.notAllowed");
    }
}

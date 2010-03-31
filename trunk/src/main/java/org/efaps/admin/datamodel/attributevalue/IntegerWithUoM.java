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

package org.efaps.admin.datamodel.attributevalue;

import org.efaps.admin.datamodel.Dimension.UoM;

/**
 * Attribute value with {@link Long} and unit of measure.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class IntegerWithUoM
    extends AbstractWithUoM<Long>
{
    /**
     * Initializes this attribute value for given <code>_value</code> and
     * <code>_uom</code>.
     *
     * @param _value    new value
     * @param _uom      link to the unit of measure instance
     */
    public IntegerWithUoM(final Long _value,
                          final UoM _uom)
    {
        super(_value, _uom);
    }
}

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

import org.efaps.admin.datamodel.Dimension.UoM;


/**
 * Abstract class for an attribute type with an UoM.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractWithUoMType extends AbstractType
{
    /**
     * Uom of this attribute type.
     */
    private UoM uoM = null;

    /**
     * Getter method for instance variable {@link #uoM}.
     *
     * @return value of instance variable {@link #uoM}
     */
    public UoM getUoM()
    {
        return this.uoM;
    }

    /**
     * Setter method for instance variable {@link #uoM}.
     *
     * @param _uoM value for instance variable {@link #uoM}
     */
    public void setUoM(final UoM _uoM)
    {
        this.uoM = _uoM;
    }

    @Override
    public boolean prepareUpdate(final StringBuilder _stmt)
    {
        _stmt.append("?,?");
        return false;
    }

}

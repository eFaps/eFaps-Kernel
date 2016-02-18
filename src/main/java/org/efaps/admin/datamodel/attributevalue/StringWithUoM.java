/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.admin.datamodel.attributevalue;

import org.efaps.admin.datamodel.Dimension.UoM;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class StringWithUoM
    extends AbstractWithUoM<String>
{
    /**
     * @param _value    Value
     * @param _uom      Dimension
     */
    public StringWithUoM(final String _value,
                         final UoM _uom)
    {
        super(_value, _uom);
    }

    @Override
    public Double getBaseDouble()
    {
        return new Double(0);
    }
}

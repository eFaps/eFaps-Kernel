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


package org.efaps.db.print.value;

import org.efaps.db.Instance;
import org.efaps.db.print.OneSelect;
import org.efaps.util.EFapsException;


/**
 * Value Select that returns an instance. Uses the {@link OIDValueSelect}
 * as basis.
 *
 * @author The eFaps Team
 *
 */
public class InstanceValueSelect
    extends OIDValueSelect
{

    /**
     * @param _oneSelect    OneSelect
     */
    public InstanceValueSelect(final OneSelect _oneSelect)
    {
        super(_oneSelect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "instance";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        return Instance.get((String) super.getValue(_object));
    }
}

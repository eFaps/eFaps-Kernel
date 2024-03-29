/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.search.value;

import org.efaps.db.wrapper.SQLSelect;

/**
 * A boolean value.
 *
 * @author The eFaps Team
 *
 */
public class QBooleanValue
    extends AbstractQValue
{

    /**
     * Number of this Value.
     */
    private final Boolean value;

    /**
     * @param _value value
     */
    public QBooleanValue(final Boolean _value)
    {
        this.value = _value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QBooleanValue appendSQL(final SQLSelect _sql)
    {
        //_sql.addPart(this.value ?  SQLPart.TRUE : SQLPart.FALSE);
        _sql.addBooleanValue(this.value);
        return this;
    }
}

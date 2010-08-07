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


package org.efaps.db.search.value;



/**
 * A boolean value.
 *
 * @author The eFaps Team
 * @version $Id$
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
    public QBooleanValue appendSQL(final StringBuilder _sql)
    {
        _sql.append(this.value ? "TRUE" : "FALSE");
        return this;
    }
}

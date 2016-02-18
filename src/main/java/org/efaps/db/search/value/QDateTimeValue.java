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


package org.efaps.db.search.value;

import org.efaps.admin.datamodel.attributetype.DateTimeType;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * A DateTime value.
 *
 * @author The eFaps Team
 *
 */
public class QDateTimeValue
    extends AbstractQValue
{

    /**
     * Number of this Value.
     */
    private final DateTime value;

    /**
     * @param _value value
     */
    public QDateTimeValue(final DateTime _value)
    {
        this.value = _value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QDateTimeValue appendSQL(final SQLSelect _sql)
        throws EFapsException
    {
        _sql.addTimestampValue(new DateTimeType().toString4Where(this.value));
        return this;
    }
}

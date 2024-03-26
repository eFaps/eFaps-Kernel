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
package org.efaps.db.search.section;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.Context;
import org.efaps.db.databases.OracleDatabase;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

public class QOffsetSection extends AbstractQSection
{

    /**
     * Limit of the SLQ statement.
     */
    private final int offset;

    /**
     * @param _limit    Limit of the SLQ statement
     */
    public QOffsetSection(final Integer offset)
    {
        this.offset = offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QOffsetSection appendSQL(final SQLSelect _select)
        throws EFapsException
    {
        // TODO it should be checked if can to be used in Oracle.
        if (!(Context.getDbType() instanceof OracleDatabase)) {
            _select.addPart(SQLPart.OFFSET).addValuePart(offset);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QOffsetSection prepare(final AbstractObjectQuery<?> _query)
        throws EFapsException
    {
        // nothing must be done
        return this;
    }
}

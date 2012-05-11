/*
 * Copyright 2003 - 2012 The eFaps Team
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


package org.efaps.db.search.section;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.Context;
import org.efaps.db.databases.OracleDatabase;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QLimitSection
    extends AbstractQSection
{

    /**
     * Limit of the SLQ statement.
     */
    private final int limit;

    /**
     * @param _limit    Limit of the SLQ statement
     */
    public QLimitSection(final Integer _limit)
    {
        this.limit = _limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QLimitSection appendSQL(final SQLSelect _select)
        throws EFapsException
    {
        // TODO it should be checked if can to be used in Oracle.
        if (!(Context.getDbType() instanceof OracleDatabase)) {
            _select.addPart(SQLPart.LIMIT).addValuePart(this.limit);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QLimitSection prepare(final AbstractObjectQuery<?> _query)
        throws EFapsException
    {
        // nothing must be done
        return this;
    }
}

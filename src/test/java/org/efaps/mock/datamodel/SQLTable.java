/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.mock.datamodel;

import java.util.List;

import org.efaps.test.EFapsQueryHandler;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class SQLTable.
 */
public class SQLTable
    extends AbstractType
{

    /** The Constant SQL. */
    private static final String SQL = "select ID,UUID,NAME,SQLTABLE,SQLCOLUMNID,SQLCOLUMNTYPE,DMTABLEMAIN "
                    + "from V_ADMINSQLTABLE T0 where T0.ID = ?";

    /**
     * Instantiates a new SQL table.
     *
     * @param _builder the builder
     */
    private SQLTable(final SQLTableBuilder _builder)
    {
        super(_builder);
    }

    @Override
    public QueryResult getResult()
    {
        return RowLists.rowList7(Long.class, String.class, String.class, String.class, String.class, String.class,
                        Long.class).append(getId(), getUuid().toString(), getName(), "T_DEMO", "ID", null, null)
                        .asResult();
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { SQL };
    }

    @Override
    public boolean applies(final String _sql, final List<Parameter> _parameters)
    {
        boolean ret = false;
        if (_parameters.size() == 1) {
            final Parameter parameter = _parameters.get(0);
            ret = getId().equals(parameter.right);
        }
        return ret;
    }

    /**
     * Builder.
     *
     * @return the SQL table builder
     */
    public static SQLTableBuilder builder()
    {
        return new SQLTableBuilder();
    }

    /**
     * The Class SQLTableBuilder.
     */
    public static class SQLTableBuilder
        extends AbstractBuilder<SQLTableBuilder>
    {

        /**
         * Builds the.
         *
         * @return the SQL table
         */
        public SQLTable build()
        {
            final SQLTable ret = new SQLTable(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}

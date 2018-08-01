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

package org.efaps.test;

import static org.testng.Assert.assertEquals;

import java.util.List;

import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class SQLVerify.
 */
public class SQLVerify
    implements IVerify
{

    /** The sql. */
    private final String sql;

    /** The counter. */
    private int counter = 0;

    /** Parameters the SQL was executed with. */
    private List<Parameter> sqlParameters;

    /**
     * Instantiates a new SQL verify.
     *
     * @param _sqlVerifyBuilder the sql verify builder
     */
    public SQLVerify(final SQLVerifyBuilder _sqlVerifyBuilder)
    {
        this.sql = _sqlVerifyBuilder.sql;
    }

    @Override
    public String getSql()
    {
        return this.sql;
    }

    @Override
    public void execute(final List<Parameter> _sqlParameters)
    {
        this.sqlParameters = _sqlParameters;
        this.counter++;
    }

    @Override
    public void verify()
    {
        EFapsQueryHandler.get().unregister(getSql());
        assertEquals(this.counter, 1);
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
                        .append(SQLVerify.class.getSimpleName())
                        .append(" ")
                        .append("sql: ")
                        .append(this.sql)
                        .toString();
    }

    /**
     * Gets the parameters the SQL was executed with.
     *
     * @return the parameters the SQL was executed with
     */
    public List<Parameter> getSqlParameters()
    {
        return this.sqlParameters;
    }

    /**
     * Builder.
     *
     * @return the type builder
     */
    public static SQLVerifyBuilder builder()
    {
        return new SQLVerifyBuilder();
    }

    /**
     * The Class TypeBuilder.
     */
    public static class SQLVerifyBuilder
    {

        /** The sql. */
        private String sql;

        /**
         * With sql.
         *
         * @param _sql the sql
         * @return the SQL verify builder
         */
        public SQLVerifyBuilder withSql(final String _sql)
        {
            this.sql = _sql;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the type
         */
        public SQLVerify build()
        {
            final SQLVerify ret = new SQLVerify(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}

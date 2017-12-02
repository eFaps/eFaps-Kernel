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

package org.efaps.mock;

import java.util.List;

import org.efaps.test.EFapsQueryHandler;
import org.efaps.test.IMockResult;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class MockResult.
 */
public class MockResult
    implements IMockResult
{

    /** The sql. */
    private final String sql;

    /** The result. */
    private final QueryResult result;

    /**
     * Instantiates a new mock result.
     *
     * @param _mockResultBuilder the mock result builder
     */
    public MockResult(final MockResultBuilder _mockResultBuilder)
    {
        this.sql = _mockResultBuilder.sql;
        this.result = _mockResultBuilder.result;
    }

    @Override
    public String[] getSqls()
    {
        return new String[] { this.sql };
    }

    @Override
    public boolean applies(final String _sql,
                           final List<Parameter> _parameters)
    {
        return true;
    }

    @Override
    public QueryResult getResult()
    {
        return this.result;
    }

    /**
     * Builder.
     *
     * @return the mock result builder
     */
    public static MockResultBuilder builder()
    {
        return new MockResultBuilder();
    }

    /**
     * The Class MockResultBuilder.
     */
    public static class MockResultBuilder
    {

        /** The sql. */
        private String sql;

        /** The result. */
        private QueryResult result;

        /**
         * With sql.
         *
         * @param _sql the sql
         * @return the mock result builder
         */
        public MockResultBuilder withSql(final String _sql)
        {
            this.sql = _sql;
            return this;
        }

        /**
         * With result.
         *
         * @param _result the result
         * @return the mock result builder
         */
        public MockResultBuilder withResult(final QueryResult _result)
        {
            this.result = _result;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the mock result
         */
        public MockResult build()
        {
            final MockResult ret = new MockResult(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}

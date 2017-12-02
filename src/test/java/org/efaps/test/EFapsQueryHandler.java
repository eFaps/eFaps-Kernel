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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.SetValuedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acolyte.jdbc.AbstractCompositeHandler.QueryHandler;
import acolyte.jdbc.QueryResult;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class EFapsQueryHandler.
 */
public class EFapsQueryHandler
    implements QueryHandler
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsQueryHandler.class);

    /** The instance. */
    private static EFapsQueryHandler INSTANCE;

    /** The sql 2 results. */
    private final SetValuedMap<String, IMockResult> sql2results = MultiMapUtils.newSetValuedHashMap();

    private final Map<String, IVerify> sql2verify = new HashMap<>();

    /**
     * Instantiates a new e faps query handler.
     */
    private EFapsQueryHandler() {
    }

    @Override
    public QueryResult apply(final String _sql,
                             final List<Parameter> _parameters)
        throws SQLException
    {
        QueryResult ret = QueryResult.Nil;
        final String sql = _sql.trim();
        if (this.sql2verify.containsKey(sql)) {
            this.sql2verify.get(sql).execute();
        } else if (this.sql2results.containsKey(sql)) {
           for (final IMockResult result : this.sql2results.get(sql)) {
               if (result.applies(sql, _parameters)) {
                   ret = result.getResult();
                   break;
               }
           }
        }
        return ret;
    }

    /**
     * Gets the sql two results.
     *
     * @return the sql two results
     */
    public SetValuedMap<String, IMockResult> getSql2Results()
    {
        return this.sql2results;
    }

    /**
     * Register.
     *
     * @param _result the result
     */
    public void register(final IMockResult _result) {
        for (final String sql : _result.getSqls()) {
            this.sql2results.put(sql, _result);
        }
        LOG.info("Added Result '{}'", _result);
    }

    /**
     * Register.
     *
     * @param _result the result
     */
    public void register(final IVerify _verify) {
        this.sql2verify.put(_verify.getSql(), _verify);
        LOG.info("Added Verify '{}'", _verify);
    }

    /**
     * Unregister.
     *
     * @param _sql the sql
     */
    public void unregister(final String _sql)
    {
        this.sql2verify.remove(_sql);
    }

    /**
     * Gets the.
     *
     * @return the e faps query handler
     */
    public static EFapsQueryHandler get() {
        if (INSTANCE == null) {
            INSTANCE = new EFapsQueryHandler();
        }
        return INSTANCE;
    }
}

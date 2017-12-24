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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.SetValuedMap;
import org.efaps.mock.MockResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acolyte.jdbc.AbstractCompositeHandler.QueryHandler;
import acolyte.jdbc.QueryResult;
import acolyte.jdbc.Row;
import acolyte.jdbc.RowList;
import acolyte.jdbc.RowList11;
import acolyte.jdbc.RowList2;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

/**
 * The Class EFapsQueryHandler.
 */
public final class EFapsQueryHandler
    implements QueryHandler
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsQueryHandler.class);

    /** The instance. */
    private static EFapsQueryHandler INSTANCE;

    /** The sql 2 results. */
    private final SetValuedMap<String, IMockResult> sql2results = MultiMapUtils.newSetValuedHashMap();

    /** The sql 2 verify. */
    private final Map<String, IVerify> sql2verify = new HashMap<>();

    /** The to be cleaned. */
    private final Set<String> toBeUnregistered = new HashSet<>();

    /**
     * Instantiates a new e faps query handler.
     */
    private EFapsQueryHandler()
    {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public QueryResult apply(final String _sql, final List<Parameter> _parameters)
        throws SQLException
    {
        QueryResult ret = QueryResult.Nil;
        final String sql = _sql.trim();
        LOG.info("Handle Query: '{}'", sql);

        if (this.sql2verify.containsKey(sql)) {
            this.sql2verify.get(sql).execute();
        } else if (this.sql2results.containsKey(sql)) {
            final List<QueryResult> results = new ArrayList<>();
            for (final IMockResult result : this.sql2results.get(sql)) {
                if (result.applies(sql, _parameters)) {
                    results.add(result.getResult());
                    if (result instanceof MockResult) {
                        this.toBeUnregistered.add(sql);
                    }
                }
            }
            final Iterator<QueryResult> iter = results.iterator();
            if (iter.hasNext()) {
                ret = iter.next();
            }
            while (iter.hasNext()) {
                final QueryResult temp = iter.next();
                final RowList<?> current = ret.getRowList();
                final RowList<?> rowList = temp.getRowList();
                if (rowList instanceof RowList11) {
                    acolyte.jdbc.RowList11.Impl rl = RowLists.rowList11(rowList.getColumnClasses().get(0),
                                    rowList.getColumnClasses().get(1),
                                    rowList.getColumnClasses().get(2),
                                    rowList.getColumnClasses().get(3),
                                    rowList.getColumnClasses().get(4),
                                    rowList.getColumnClasses().get(5),
                                    rowList.getColumnClasses().get(6),
                                    rowList.getColumnClasses().get(7),
                                    rowList.getColumnClasses().get(8),
                                    rowList.getColumnClasses().get(9),
                                    rowList.getColumnClasses().get(10));
                    for (final Row row : rowList.getRows()) {
                        final List<Object> cells = row.cells();
                        rl = (acolyte.jdbc.RowList11.Impl) rl.append(cells.get(0), cells.get(1), cells.get(2),
                                        cells.get(3), cells.get(4), cells.get(5), cells.get(6), cells.get(7),
                                        cells.get(8), cells.get(9), cells.get(10));
                    }
                    for (final Row row : current.getRows()) {
                        final List<Object> cells = row.cells();
                        rl = (acolyte.jdbc.RowList11.Impl) rl.append(cells.get(0), cells.get(1), cells.get(2),
                                        cells.get(3), cells.get(4), cells.get(5), cells.get(6), cells.get(7),
                                        cells.get(8), cells.get(9), cells.get(10));
                    }
                    ret = rl.asResult();
                } else if (rowList instanceof RowList2) {
                    acolyte.jdbc.RowList2.Impl rl = RowLists.rowList2(rowList.getColumnClasses().get(0),
                                    rowList.getColumnClasses().get(1));
                    for (final Row row : rowList.getRows()) {
                        final List<Object> cells = row.cells();
                        rl = (acolyte.jdbc.RowList2.Impl) rl.append(cells.get(0), cells.get(1));
                    }
                    for (final Row row : current.getRows()) {
                        final List<Object> cells = row.cells();
                        rl = (acolyte.jdbc.RowList2.Impl) rl.append(cells.get(0), cells.get(1));
                    }
                    ret = rl.asResult();
                }
            }
        }
        LOG.info("found result: {}", ret.getRowList().getRows().size());
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
    public void register(final IMockResult _result)
    {
        for (final String sql : _result.getSqls()) {
            this.sql2results.put(sql, _result);
        }
        LOG.info("Added Result '{}'", _result);
    }

    /**
     * Register.
     *
     * @param _verify the verify
     */
    public void register(final IVerify _verify)
    {
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
        this.sql2results.remove(_sql);
    }

    /**
     * Clean up.
     */
    public void cleanUp()
    {
        this.toBeUnregistered.forEach(this::unregister);
        this.toBeUnregistered.clear();
    }

    /**
     * Gets the.
     *
     * @return the e faps query handler
     */
    public static EFapsQueryHandler get()
    {
        if (INSTANCE == null) {
            INSTANCE = new EFapsQueryHandler();
        }
        return INSTANCE;
    }
}

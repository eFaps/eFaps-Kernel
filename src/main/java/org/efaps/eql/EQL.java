/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.eql;

import java.util.Arrays;

import org.efaps.db.Instance;
import org.efaps.db.stmt.AbstractStmt;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.eql.builder.Print;
import org.efaps.eql.builder.Query;
import org.efaps.eql.builder.Selectables;
import org.efaps.eql.builder.Where;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.IStatement;
import org.efaps.eql2.bldr.AbstractInsertEQLBuilder;
import org.efaps.eql2.bldr.AbstractPrintEQLBuilder;
import org.efaps.eql2.bldr.AbstractQueryEQLBuilder;
import org.efaps.eql2.bldr.AbstractSelectables;
import org.efaps.eql2.bldr.AbstractUpdateEQLBuilder;
import org.efaps.eql2.bldr.AbstractWhereBuilder;

/**
 * EQL main util classes.
 *
 * @author The eFaps Team
 */
public final class EQL
    extends org.efaps.eql2.EQL
{
    @Override
    protected AbstractPrintEQLBuilder<?> getPrint()
    {
        return new Print();
    }

    @Override
    protected AbstractUpdateEQLBuilder<?> getUpdate()
    {
        return null;
    }

    @Override
    protected AbstractInsertEQLBuilder<?> getInsert()
    {
        return null;
    }

    @Override
    protected AbstractQueryEQLBuilder<?> getQuery()
    {
        return new Query();
    }

    @Override
    protected AbstractWhereBuilder<?> getWhere()
    {
        return new Where();
    }

    @Override
    protected AbstractSelectables getSelectables()
    {
        return new Selectables();
    }

    /**
     * Prints the.
     *
     * @param _instances the instances
     * @return the prints the
     */
    public static Print print(final Instance... _instances)
    {
        return print(Arrays.stream(_instances)
                        .map(instance -> instance.getOid())
                        .toArray(String[]::new));
    }

    /**
     * Prints the.
     *
     * @param _oid the oid
     * @return the prints the
     */
    public static Print print(final String... _oid)
    {
        return (Print) org.efaps.eql2.EQL.print(_oid);
    }

    /**
     * Prints the.
     *
     * @param _queryBuilder the query builder
     * @return the prints the
     */
    public static Print print(final Query  _queryBuilder)
    {
        return (Print) org.efaps.eql2.EQL.print(_queryBuilder);
    }

    /**
     * Prints the.
     *
     * @param _types the types
     * @return the abstract print EQL builder<?>
     */
    public static Query query(final String... _types)
    {
        return (Query) org.efaps.eql2.EQL.query(_types);
    }

    /**
     * Parses the stmt.
     *
     * @param _stmt the stmt
     * @return the abstract stmt
     */
    public static AbstractStmt getStatement(final CharSequence _stmt) {
        AbstractStmt ret = null;
        final IStatement<?> stmt = parse(_stmt);
        if (stmt instanceof IPrintStatement) {
            ret = PrintStmt.get((IPrintStatement<?>) stmt);
        }
        return ret;
    }
}

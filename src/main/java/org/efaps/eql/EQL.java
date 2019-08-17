/*
 * Copyright 2003 - 2019 The eFaps Team
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

import org.efaps.ci.CIType;
import org.efaps.db.Instance;
import org.efaps.db.stmt.AbstractStmt;
import org.efaps.db.stmt.DeleteStmt;
import org.efaps.db.stmt.InsertStmt;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.UpdateStmt;
import org.efaps.eql.builder.Delete;
import org.efaps.eql.builder.Insert;
import org.efaps.eql.builder.Print;
import org.efaps.eql.builder.Query;
import org.efaps.eql.builder.Selectables;
import org.efaps.eql.builder.Update;
import org.efaps.eql.builder.Where;
import org.efaps.eql2.EQL2;
import org.efaps.eql2.IDeleteStatement;
import org.efaps.eql2.IInsertStatement;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.IStatement;
import org.efaps.eql2.IUpdateStatement;
import org.efaps.eql2.bldr.AbstractDeleteEQLBuilder;
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
    extends EQL2
{
    @Override
    protected AbstractPrintEQLBuilder<?> getPrint()
    {
        return new Print();
    }

    @Override
    protected AbstractUpdateEQLBuilder<?> getUpdate()
    {
        return new Update();
    }

    @Override
    protected AbstractInsertEQLBuilder<?> getInsert()
    {
        return new Insert();
    }

    @Override
    protected AbstractDeleteEQLBuilder<?> getDelete()
    {
        return new Delete();
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
     * Parses the stmt.
     *
     * @param _stmt the stmt
     * @return the abstract stmt
     */
    public static AbstractStmt getStatement(final CharSequence _stmt)
    {
        AbstractStmt ret = null;
        final IStatement<?> stmt = parse(_stmt);
        if (stmt instanceof IPrintStatement) {
            ret = PrintStmt.get((IPrintStatement<?>) stmt);
        } else if (stmt instanceof IDeleteStatement) {
            ret = DeleteStmt.get((IDeleteStatement<?>) stmt);
        } else if (stmt instanceof IInsertStatement) {
            ret = InsertStmt.get((IInsertStatement) stmt);
        } else if (stmt instanceof IUpdateStatement) {
            ret = UpdateStmt.get((IUpdateStatement<?>) stmt);
        }
        return ret;
    }

    public static EQLBuilder builder() {
        return new EQLBuilder(EQL2.eql());
    }

    public static class EQLBuilder extends EQL2Builder<EQLBuilder> {
        public EQLBuilder(final EQL2 _eql2) {
            super(_eql2);
        }

        @Override
        public Delete delete(final String... _oids) {
            return (Delete) super.delete(_oids);
        }

        public Delete delete(final Instance... _instances)
        {
            return delete(Arrays.stream(_instances)
                            .map(instance -> instance.getOid())
                            .toArray(String[]::new));
        }

        @Override
        public Insert insert(final String _typeName)
        {
            return (Insert) super.insert(_typeName);
        }

        public Insert insert(final CIType _ciType)
        {
            return insert(_ciType.getType().getName());
        }

        @Override
        public Update update(final String... _oid)
        {
            return (Update) super.update(_oid);
        }

        public Update update(final Instance... _instances)
        {
            return update(Arrays.stream(_instances)
                            .map(instance -> instance.getOid())
                            .toArray(String[]::new));
        }

        public Print print()
        {
            return print(new String[0]);
        }

        @Override
        public Print print(final String... _oids)
        {
            return (Print) super.print(_oids);
        }

        public Print print(final Instance... _instances)
        {
            return print(Arrays.stream(_instances)
                            .map(instance -> instance.getOid())
                            .toArray(String[]::new));
        }

        @Override
        public Query query(final String... _types)
        {
            return (Query) super.query(_types);
        }

        @Override
        public Where where()
        {
            return (Where) super.where();
        }
    }
}

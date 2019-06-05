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

package org.efaps.eql.builder;

import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.StmtFlag;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql.EQL;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.bldr.AbstractPrintEQLBuilder;
import org.efaps.eql2.bldr.ISelectable;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Print.
 */
public class Print
    extends AbstractPrintEQLBuilder<Print>
    implements IEQLBuilder
{

    private static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    /** The flags. */
    private StmtFlag[] flags;

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public PrintStmt stmt()
    {
        LOG.debug("Stmt: {}", getStmt().eqlStmt());
        return PrintStmt.get((IPrintStatement<?>) getStmt(), flags);
    }

    public PrintStmt execute()
        throws EFapsException
    {
        return stmt().execute();
    }

    public Evaluator evaluate()
        throws EFapsException
    {
        return stmt().evaluate();
    }

    @Override
    protected Print getThis()
    {
        return this;
    }

    @Override
    public Print select(final ISelectable... _selects)
    {
        for (final ISelectable select : _selects) {
            switch (select.getKey()) {
                case "CIAttribute":
                    attribute(((CIAttribute) select).name);
                    break;
                default:
                    super.select(select);
                    break;
            }
        }
        return getThis();
    }

    public Print attribute(final CIAttribute... _ciAttrs)
    {
        for (final CIAttribute ciAttr : _ciAttrs) {
            attribute(ciAttr.name);
            as(getDefaultAlias(ciAttr));
        }
        return getThis();
    }

    public Print linkto(final CIAttribute _ciAttr)
    {
        linkto(_ciAttr.name);
        return getThis();
    }

    public Print orderBy(final CIAttribute _ciAttr)
    {
        super.orderBy(_ciAttr.name);
        return getThis();
    }

    public Print with(final StmtFlag... _flags)
    {
        flags = _flags;
        return getThis();
    }

    public Query query(final CIType... _ciTypes)
    {
        final Query query = EQL.query(_ciTypes);
        query.setParent(this);
        print(query);
        return query;
    }

    public static String getDefaultAlias(final CIAttribute _ciAttr)
    {
        return "CIALIAS" + _ciAttr.name;
    }
}

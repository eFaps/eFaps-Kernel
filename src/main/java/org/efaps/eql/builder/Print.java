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
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.StmtFlag;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.bldr.AbstractPrintEQLBuilder;
import org.efaps.eql2.bldr.ISelectable;

/**
 * The Class Print.
 */
public class Print
    extends AbstractPrintEQLBuilder<Print>
{

    /** The flags. */
    private StmtFlag[] flags;

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public PrintStmt stmt()
    {
        return PrintStmt.get((IPrintStatement<?>) getStmt(), flags);
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

    public Print linkto(final CIAttribute _ciAttr) {
        linkto(_ciAttr.name);
        return getThis();
    }

    public Print with(final StmtFlag... _flags)
    {
        flags = _flags;
        return getThis();
    }

    public static String getDefaultAlias(final CIAttribute _ciAttr) {
        return "CIALIAS" + _ciAttr.name;
    }
}

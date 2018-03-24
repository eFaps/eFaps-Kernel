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

package org.efaps.eql.builder;

import org.efaps.ci.CIAttribute;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.bldr.AbstractPrintEQLBuilder;

/**
 * The Class Print.
 */
public class Print
    extends AbstractPrintEQLBuilder<Print>
{

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public PrintStmt stmt()
    {
        return PrintStmt.get((IPrintStatement<?>) getStmt());
    }

    @Override
    protected Print getThis()
    {
        return this;
    }

    public Print select(final Selectable... _selects) {
        for (final Selectable select : _selects) {
            if (select instanceof CIAttribute) {
                attribute(((CIAttribute) select).name);
            }
        }
        return getThis();
    }
}

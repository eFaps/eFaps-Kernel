/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.stmt;

import org.efaps.db.stmt.print.AbstractPrint;
import org.efaps.db.stmt.runner.StmtRunner;
import org.efaps.db.stmt.selection.EvalHelper;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.ICountQueryStatement;
import org.efaps.eql2.StmtFlag;
import org.efaps.util.EFapsException;

public class CountStmt
    extends AbstractStmt
{

    private AbstractPrint print;

    private EvalHelper helper;

    public CountStmt(final StmtFlag... _flags)
    {
        super(_flags);
    }

    private void setHelper(final EvalHelper _helper)
    {
        helper = _helper;
    }

    public CountStmt execute()
        throws EFapsException
    {
        StmtRunner.get().execute(print);
        return this;
    }

    public Evaluator evaluate()
        throws EFapsException
    {
        if (print == null) {
            execute();
        }
        return Evaluator.get(print, helper);
    }

    public static CountStmt get(final ICountQueryStatement countQueryStmt)
    {
        return get(countQueryStmt, null);
    }

    public static CountStmt get(final ICountQueryStatement countQueryStmt, final EvalHelper _helper)
    {
        final var ret = new CountStmt(countQueryStmt.getFlags());
        ret.setEQLStmt(countQueryStmt);
        ret.setHelper(_helper);
        return ret;
    }
}

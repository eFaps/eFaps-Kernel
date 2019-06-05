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

package org.efaps.db.stmt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.db.stmt.print.AbstractPrint;
import org.efaps.db.stmt.print.ListPrint;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.print.QueryPrint;
import org.efaps.db.stmt.runner.StmtRunner;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.IPrintListStatement;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.eql2.IPrintStatement;
import org.efaps.util.EFapsException;

/**
 * PrintStatement wrapper and execute class.
 *
 * @author The eFaps Team
 */
public final class PrintStmt
    extends AbstractStmt
{

    /** The print. */
    private AbstractPrint print;

    /**
     * Instantiates a new prints the stmt.
     */
    private PrintStmt(final StmtFlag... _flags)
    {
        super(_flags);
    }

    /**
     * Execute.
     *
     * @return the prints the stmt
     * @throws EFapsException on error
     */
    public PrintStmt execute()
        throws EFapsException
    {
        if (getEQLStmt() instanceof IPrintObjectStatement) {
            print = new ObjectPrint((IPrintObjectStatement) getEQLStmt(), getFlags());
        } else if (getEQLStmt() instanceof IPrintListStatement) {
            print = new ListPrint((IPrintListStatement) getEQLStmt(), getFlags());
        } else if (getEQLStmt() instanceof IPrintQueryStatement) {
            print = new QueryPrint((IPrintQueryStatement) getEQLStmt(), getFlags());
        }
        StmtRunner.get().execute(print);
        return this;
    }

    /**
     * Evaluator.
     *
     * @return the selection evaluator
     * @throws EFapsException the eFaps exception
     */
    public Evaluator evaluate()
        throws EFapsException
    {
        if (print == null) {
            execute();
        }
        return Evaluator.get(print.getSelection());
    }

    public String asString()
    {
        final StringBuilder ret = new StringBuilder();
        final Pattern regex = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"");
        final Matcher regexMatcher = regex.matcher(getEQLStmt().eqlStmt());
        while (regexMatcher.find()) {
            final String value = regexMatcher.group();
            if (!value.endsWith(",")) {
                ret.append(" ");
            }
            ret.append(value);
        }
        return ret.toString().trim();
    }

    /**
     * Gets the.
     *
     * @param _printStmt the print stmt
     * @param _flags the flags
     * @return the prints the stmt
     */
    public static PrintStmt get(final IPrintStatement<?> _printStmt,
                                final StmtFlag... _flags)
    {
        final PrintStmt ret = new PrintStmt(_flags);
        ret.setEQLStmt(_printStmt);
        return ret;
    }
}

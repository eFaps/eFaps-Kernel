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

package org.efaps.db.stmt;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.runner.StmtRunner;
import org.efaps.eql2.IPrintListStatement;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.impl.PrintQueryStatement;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;

/**
 * PrintStatement wrapper and execute class.
 *
 * @author The eFaps Team
 */
public final class PrintStmt
    extends AbstractStmt
{

    /** The print. */
    private ObjectPrint print;

    /**
     * Instantiates a new prints the stmt.
     */
    private PrintStmt()
    {
    }

    /**
     * Execute.
     * @throws EFapsException on error
     */
    public void execute()
        throws EFapsException
    {
        if (getEQLStmt() instanceof IPrintObjectStatement) {
            this.print = new ObjectPrint((IPrintObjectStatement) getEQLStmt());
            StmtRunner.get().execute(this.print);
        } else if (getEQLStmt() instanceof IPrintListStatement) {

        } else if (getEQLStmt() instanceof IPrintQueryStatement) {
            for (final String typeStr : ((PrintQueryStatement) getEQLStmt()).getQuery().getTypes()) {
                final Type type;
                if (UUIDUtil.isUUID(typeStr)) {
                    type = Type.get(UUID.fromString(typeStr));
                } else {
                    type = Type.get(typeStr);
                }
            }
        }
    }

    /**
     * Gets the.
     *
     * @param _printStmt the print stmt
     * @return the prints the stmt
     */
    public static PrintStmt get(final IPrintStatement<?> _printStmt)
    {
        final PrintStmt ret = new PrintStmt();
        ret.setEQLStmt(_printStmt);
        return ret;
    }
}

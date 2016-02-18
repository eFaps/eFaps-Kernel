/*
 * Copyright 2003 - 2016 The eFaps Team
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

import org.efaps.eql.stmt.AbstractCIPrintStmt;
import org.efaps.eql.stmt.AbstractDeleteStmt;
import org.efaps.eql.stmt.AbstractExecStmt;
import org.efaps.eql.stmt.AbstractInsertStmt;
import org.efaps.eql.stmt.AbstractPrintStmt;
import org.efaps.eql.stmt.AbstractUpdateStmt;
import org.efaps.eql.stmt.parts.INestedQueryStmtPart;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class InvokerUtil
{

    /**
     * Singelton.
     */
    private InvokerUtil()
    {
    }

    /**
     * Gets the invoker.
     *
     * @return the invoker
     */
    public static EQLInvoker getInvoker()
    {
        final EQLInvoker ret = new EQLInvoker()
        {
            @Override
            protected AbstractPrintStmt getPrint()
            {
                return new PrintStmt();
            }

            @Override
            protected AbstractInsertStmt getInsert()
            {
                return new InsertStmt();
            }

            @Override
            protected AbstractExecStmt getExec()
            {
                return new ExecStmt();
            }

            @Override
            protected AbstractUpdateStmt getUpdate()
            {
                return new UpdateStmt();
            }

            @Override
            protected AbstractDeleteStmt getDelete()
            {
                return new DeleteStmt();
            }

            @Override
            protected INestedQueryStmtPart getNestedQuery()
            {
                return super.getNestedQuery();
            }

            @Override
            protected AbstractCIPrintStmt getCIPrint()
            {
                return new CIPrintStmt();
            }
        };
        ret.getValidator().setDiagnosticClazz(EFapsDiagnostic.class);
        ret.getValidator().addValidation("EQLJavaValidator.type", new TypeValidation());
        return ret;
    }
}

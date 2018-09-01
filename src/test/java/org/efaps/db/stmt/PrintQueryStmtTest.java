/*
 * Copyright 2003 - 2018 The eFaps Team
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQL;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

public class PrintQueryStmtTest
    extends AbstractTest
{
    @Test
    public void testSimpleType()
        throws EFapsException
    {
        final String stmtStr = String.format("print query type %s select attribute[%s]",
                        Mocks.SimpleType.getName(), Mocks.TestAttribute.getName());

        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL.parse(stmtStr);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder()
            .withSql("select T0.TestAttribute_COL,T0.ID from T_DEMO T0")
            .build();
        printStmt.execute();
        verify.verify();
    }

    @Test(description = "Exec select without any other selects")
    public void testSelectExec()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0",
                        Mocks.SimpleTypeSQLTable.getSqlTableName());
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Long.class)
                .append(4L)
                .append(8L)
                .asResult())
            .build();

        final String stmtStr = String.format("print query type %s select exec %s as barcode",
                        Mocks.SimpleType.getName(), org.efaps.mock.esjp.SimpleSelect.class.getName());
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
        assertEquals(evaluator.get("barcode"),
                        String.format(org.efaps.mock.esjp.SimpleSelect.FORMAT,
                                        Instance.get(Mocks.SimpleType.getName(), "4").getOid()));
        assertTrue(evaluator.next());
        assertEquals(evaluator.get("barcode"),
                        String.format(org.efaps.mock.esjp.SimpleSelect.FORMAT,
                                        Instance.get(Mocks.SimpleType.getName(), "8").getOid()));
        assertFalse(evaluator.next());
    }
}

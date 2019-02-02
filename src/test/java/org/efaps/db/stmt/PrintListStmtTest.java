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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQL2;
import org.efaps.eql2.IPrintListStatement;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

public class PrintListStmtTest
    extends AbstractTest
{
    @Test
    public void testSimplePrintObject()
        throws EFapsException
    {
        final String sql = "select T0.TestAttribute_COL from T_DEMO T0 where T0.ID in ( 456,457,458 )";

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                .append("Val1")
                .append("Val2")
                .append("Val3")
                .asResult())
            .build();

        final String stmtStr = String.format("print list (%1$s.456, %1$s.457, %1$s.458) select attribute[%2$s]",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName());

        final IPrintListStatement stmt = (IPrintListStatement) EQL2.parse(stmtStr);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final Evaluator evalutor = printStmt.evaluate();
        assertTrue(evalutor.next());
        assertEquals(evalutor.get(1), "Val1");
        assertTrue(evalutor.next());
        assertEquals(evalutor.get(1), "Val2");
        assertTrue(evalutor.next());
        assertEquals(evalutor.get(1), "Val3");
        assertFalse(evalutor.next());
    }
}

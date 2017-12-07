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

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;

import org.eclipse.xtext.parser.IParseResult;
import org.efaps.db.stmt.selection.SelectionEvaluator;
import org.efaps.eql2.EQLStandaloneSetup;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.eql2.parser.antlr.EQLParser;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

/**
 * The Class PrintStmtTest.
 */
public class PrintStmtTest
    extends AbstractTest
{

    /** The parser. */
    @Inject
    private EQLParser parser;

    /**
     * Sets the up.
     */
    @BeforeClass
    public void setUp()
    {
        EQLStandaloneSetup.doSetup(this);
    }

    @Test
    public void testSimplePrintObject()
        throws EFapsException
    {
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder()
            .withSql("select T0.TestAttribute_COL from T_DEMO T0 where T0.ID = 4")
            .build();
        printStmt.execute();
        verify.verify();
    }

    @Test
    public void testSimplePrintObjectValue()
        throws EFapsException
    {
        final String sql = String.format("select T0.TestAttr_COL from T_DEMO T0 where T0.ID = 4 "
                        + "and T0.TYPE = %s", Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value")
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.TypedType.getId(), Mocks.TypedTypeTestAttr.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final SelectionEvaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test
    public void testStringAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value")
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrStringAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final SelectionEvaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test
    public void testLongAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(100)
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrLongAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final SelectionEvaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), Long.valueOf(100));
    }

    @Test
    public void testIntegerAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrIntegerAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(101)
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrIntegerAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final SelectionEvaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), Integer.valueOf(101));
    }

    @Test
    public void testLinkto()
        throws EFapsException
    {
        //"select T1.TestAttribute_COL from left join T_DEMO T1 on T0.AllAttrLinkAttribute_COL=T1.ID where T0.ID = 4"

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select linkto[%s].attribute[%s]",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttribute.getName(), Mocks.TestAttribute.getName()));

        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final SelectionEvaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluator();
        assertEquals(evaluator.get(1), Integer.valueOf(101));
    }
}


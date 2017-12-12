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

package org.efaps.eql;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.efaps.db.stmt.selection.SelectionEvaluator;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

/**
 * The Class QueryTest.
 */
public class QueryTest
    extends AbstractTest
{
    @Test
    public void testQuery()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(EQL.query(Mocks.SimpleType.getName()))
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testTypedQuery()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.TYPE = %s",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                        Mocks.TypedType.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(EQL.query(Mocks.TypedType.getName()))
            .attribute(Mocks.TypedTypeTestAttr.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void test2TypedQuery()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.TYPE in ( %s , %s )",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                        Mocks.TypedType.getId(),
                        Mocks.TypedType2.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(EQL.query(Mocks.TypedType.getName(), Mocks.TypedType2.getName()))
            .attribute(Mocks.TypedTypeTestAttr.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testQueryWhere()
        throws EFapsException
    {
        final String strCriteria = RandomUtil.randomAlphanumeric(8);
        final String sql = String.format("select T0.%s from %s T0 where T0.%s = '%s'",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.TestAttribute.getSQLColumnName(),
                        strCriteria);

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(EQL.query(Mocks.SimpleType.getName()).where(
                        EQL.where()
                        .attribute(Mocks.TestAttribute.getName())
                        .eq(strCriteria)))
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testQueryValue()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                .append("StringValue1")
                .append("StringValue2")
                .append("StringValue3")
                .asResult())
            .build();

        final List<String> values = new ArrayList<>();
        final SelectionEvaluator eval = EQL.print(EQL.query(Mocks.SimpleType.getName()))
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute()
            .evaluator();
        while (eval.next()) {
            values.add(eval.get(1));
        }
        assertEquals(values, Arrays.asList("StringValue1", "StringValue2", "StringValue3"));
    }

    @Test
    public void testQueryValues()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s from %s T0",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                .append("StringValue1", 1L)
                .append("StringValue2", 2L)
                .append("StringValue3", 3L)
                .asResult())
            .build();

        final List<String> values1 = new ArrayList<>();
        final List<Long> values2 = new ArrayList<>();
        final SelectionEvaluator eval = EQL.print(EQL.query(Mocks.AllAttrType.getName()))
            .attribute(Mocks.AllAttrStringAttribute.getName(), Mocks.AllAttrLongAttribute.getName())
            .stmt()
            .execute()
            .evaluator();
        while (eval.next()) {
            values1.add(eval.get(1));
            values2.add(eval.get(2));
        }
        assertEquals(values1, Arrays.asList("StringValue1", "StringValue2", "StringValue3"));
        assertEquals(values2, Arrays.asList(1L, 2L, 3L));
    }
}

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
package org.efaps.eql;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.Evaluator;
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(Mocks.SimpleType.getName())
            .select()
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testTypedQuery()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.TYPE = %s",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                        Mocks.TypedType.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(Mocks.TypedType.getName())
            .select()
            .attribute(Mocks.TypedTypeTestAttr.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void test2TypedQuery()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.TYPE in (%s,%s)",
                        Mocks.AbstractTypeStringAttribute.getSQLColumnName(),
                        Mocks.AbstractTypeSQLTable.getSqlTableName(),
                        Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                            ? Mocks.ChildType1.getId() : Mocks.ChildType2.getId() ,
                        Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                            ? Mocks.ChildType2.getId() : Mocks.ChildType1.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(Mocks.ChildType1.getName(), Mocks.ChildType2.getName())
            .select()
            .attribute(Mocks.AbstractTypeStringAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testQueryWhere()
        throws EFapsException
    {
        final String strCriteria = RandomUtil.randomAlphanumeric(8);
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s = '%s'",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.TestAttribute.getSQLColumnName(),
                        strCriteria);

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(Mocks.SimpleType.getName())
            .where()
                .attribute(Mocks.TestAttribute.getName())
                .eq(strCriteria)
            .select()
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testQueryValue()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                .append("StringValue1", 1L)
                .append("StringValue2", 2L)
                .append("StringValue3", 3L)
                .asResult())
            .build();

        final List<String> values = new ArrayList<>();
        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.SimpleType.getName())
            .select()
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute()
            .evaluate();
        while (eval.next()) {
            values.add(eval.get(1));
        }
        assertEquals(values, Arrays.asList("StringValue1", "StringValue2", "StringValue3"));
    }

    @Test
    public void testQueryValues()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s,T0.ID from %s T0",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Object.class, Object.class)
                .append("StringValue1", 1L, 234L)
                .append("StringValue2", 2L, 334L)
                .append("StringValue3", 3L, 434L)
                .asResult())
            .build();

        final List<String> values1 = new ArrayList<>();
        final List<Long> values2 = new ArrayList<>();
        final List<Instance> instances = new ArrayList<>();
        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(Mocks.AllAttrStringAttribute.getName(), Mocks.AllAttrLongAttribute.getName())
            .stmt()
            .execute()
            .evaluate();
        while (eval.next()) {
            values1.add(eval.get(1));
            values2.add(eval.get(2));
            instances.add(eval.inst());
        }
        assertEquals(values1, Arrays.asList("StringValue1", "StringValue2", "StringValue3"));
        assertEquals(values2, Arrays.asList(1L, 2L, 3L));
    }

    @Test
    public void testQueryLinktoValue()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Object.class, Object.class)
                            .append("StringValue1", 1L, 234L)
                            .append("StringValue2", 2L, 334L)
                            .append("StringValue3", 3L, 434L)
                            .asResult())
            .build();

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AllAttrType.getName())
                        .select()
                        .linkto(Mocks.AllAttrLinkAttribute.getName()).attribute(Mocks.TestAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();
        final List<String> values1 = new ArrayList<>();
        while (eval.next()) {
            values1.add(eval.get(1));
        }
        assertEquals(values1, Arrays.asList("StringValue1", "StringValue2", "StringValue3"));
    }

    @Test
    public void testLinkFromInstance()
        throws EFapsException
    {
        final String sql = String.format("select T1.ID,T0.ID from %s T0 left join %s T1 on T0.ID=T1.%s",
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(Mocks.SimpleType.getName())
            .select()
                    .linkfrom(Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName())
                            .instance()
                        .stmt()
                        .execute();
        verify.verify();
    }

    @Test
    public void testLinkFromAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 left join %s T1 on T0.ID=T1.%s",
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(Mocks.SimpleType.getName())
            .select()
                        .linkfrom(Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName())
                            .attribute(Mocks.RealtionStringAttribute.getName())
                        .stmt()
                        .execute();
        verify.verify();
    }

    @Test
    public void testLinkToLinkFromAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T2.%s,T0.ID,T1.ID,T2.ID from %s T0 left join %s T1 on T0.%s=T1.ID"
                        + " left join %s T2 on T1.ID=T2.%s",
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print().query(Mocks.AllAttrType.getName())
            .select()
                        .linkto(Mocks.AllAttrLinkAttribute.getName())
                        .linkfrom(Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName())
                            .attribute(Mocks.RealtionStringAttribute.getName())
                        .stmt()
                        .execute();
        verify.verify();
    }
}

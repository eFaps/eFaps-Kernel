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
package org.efaps.db.stmt.selection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.eql.EQL;
import org.efaps.json.data.DataList;
import org.efaps.json.data.LongValue;
import org.efaps.json.data.StringValue;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.CI;
import org.efaps.mock.esjp.AccessCheck;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

/**
 * The Class EvaluatorTest.
 */
public class EvaluatorTest
    extends AbstractTest
{
    @Test
    public void testCount()
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

        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(Mocks.AllAttrStringAttribute.getName(), Mocks.AllAttrLongAttribute.getName())
            .stmt()
            .execute()
            .evaluate();

        assertEquals(eval.count(), 3);
    }

    @Test
    public void testAccessAll() throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                .append("StringValue1", 234L)
                .append("StringValue2", 334L)
                .append("StringValue3", 434L)
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();

        assertEquals(eval.count(), 3);
    }

    @Test
    public void testAccess() throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                .append("StringValue1", 534L)
                .append("StringValue2", 634L)
                .append("StringValue3", 734L)
                .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 534L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();

        assertEquals(eval.count(), 2);
    }

    @Test
    public void testAccessAllToLinkToObject()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s,T0.ID,T1.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList4(Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1B", 114L, 115L)
                .append("StringValue2A", "StringValue2B", 224L, 225L)
                .append("StringValue3A", "StringValue3B", 334L, 335L)
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .attribute(Mocks.AccessType2StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();

        assertEquals(eval.count(), 3);
    }

    @Test(description = "Access to main object is restricted")
    public void testAccessMainObjectToLinkToObject()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s,T0.ID,T1.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList4(Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1B", 114L, 115L)
                .append("StringValue2A", "StringValue2B", 224L, 225L)
                .append("StringValue3A", "StringValue3B", 334L, 335L)
                .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 224L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .attribute(Mocks.AccessType2StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();

        assertEquals(eval.count(), 2);
    }

    @Test(description = "Access to linkto object is restricted")
    public void testAccessLinktoObjectToLinkToObject()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s,T0.ID,T1.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList4(Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1B", 114L, 115L)
                .append("StringValue2A", "StringValue2B", 224L, 225L)
                .append("StringValue3A", "StringValue3B", 334L, 335L)
                .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType2.getId()), 225L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .attribute(Mocks.AccessType2StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 3);
        eval.next();
        assertEquals(eval.get(1), "StringValue1A");
        assertEquals(eval.get(2), "StringValue1B");
        eval.next();
        assertEquals(eval.get(1), "StringValue2A");
        assertNull(eval.get(2));
        eval.next();
        assertEquals(eval.get(1), "StringValue3A");
        assertEquals(eval.get(2), "StringValue3B");
    }

    @Test(description = "Access to linkto.linkto object without restriction")
    public void testAccessLinktoLinktoObject()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T2.%s,T0.ID,T1.ID,T2.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "left join %s T2 on T1.%s=T2.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName(),
                        Mocks.AccessType3SQLTable.getSqlTableName(),
                        Mocks.AccessType2LinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList5(Object.class, Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1C", 114L, 115L, 116L)
                .append("StringValue2A", "StringValue2C", 224L, 225L, 226L)
                .append("StringValue3A", "StringValue3C", 334L, 335L, 336L)
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .linkto(Mocks.AccessType2LinkAttribute.getName())
                            .attribute(Mocks.AccessType3StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 3);
        eval.next();
        assertEquals(eval.get(1), "StringValue1A");
        assertEquals(eval.get(2), "StringValue1C");
        eval.next();
        assertEquals(eval.get(1), "StringValue2A");
        assertEquals(eval.get(2), "StringValue2C");
        eval.next();
        assertEquals(eval.get(1), "StringValue3A");
        assertEquals(eval.get(2), "StringValue3C");
    }

    @Test(description = "Access to linkto.linkto object restriction on base element")
    public void testAccessLinktoLinktoObjectRestrictBase()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T2.%s,T0.ID,T1.ID,T2.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "left join %s T2 on T1.%s=T2.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName(),
                        Mocks.AccessType3SQLTable.getSqlTableName(),
                        Mocks.AccessType2LinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList5(Object.class, Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1C", 114L, 115L, 116L)
                .append("StringValue2A", "StringValue2C", 224L, 225L, 226L)
                .append("StringValue3A", "StringValue3C", 334L, 335L, 336L)
                .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 224L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .linkto(Mocks.AccessType2LinkAttribute.getName())
                            .attribute(Mocks.AccessType3StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 2);
        eval.next();
        assertEquals(eval.get(1), "StringValue1A");
        assertEquals(eval.get(2), "StringValue1C");
        eval.next();
        assertEquals(eval.get(1), "StringValue3A");
        assertEquals(eval.get(2), "StringValue3C");
        assertFalse(eval.next());
    }

    @Test(description = "Access to linkto.linkto object restriction on middle element")
    public void testAccessLinktoLinktoObjectRestrictMiddle()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T2.%s,T0.ID,T1.ID,T2.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "left join %s T2 on T1.%s=T2.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName(),
                        Mocks.AccessType3SQLTable.getSqlTableName(),
                        Mocks.AccessType2LinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList5(Object.class, Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1C", 114L, 115L, 116L)
                .append("StringValue2A", "StringValue2C", 224L, 225L, 226L)
                .append("StringValue3A", "StringValue3C", 334L, 335L, 336L)
                .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType2.getId()), 225L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .linkto(Mocks.AccessType2LinkAttribute.getName())
                            .attribute(Mocks.AccessType3StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 3);
        eval.next();
        assertEquals(eval.get(1), "StringValue1A");
        assertEquals(eval.get(2), "StringValue1C");
        eval.next();
        assertEquals(eval.get(1), "StringValue2A");
        assertNull(eval.get(2));
        eval.next();
        assertEquals(eval.get(1), "StringValue3A");
        assertEquals(eval.get(2), "StringValue3C");
        assertFalse(eval.next());
    }


    @Test(description = "Access to linkto.linkto object restriction on final element")
    public void testAccessLinktoLinktoObjectRestrictFinal()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T2.%s,T0.ID,T1.ID,T2.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "left join %s T2 on T1.%s=T2.ID",
                        Mocks.AccessTypeStringAttribute.getSQLColumnName(),
                        Mocks.AccessType2StringAttribute.getSQLColumnName(),
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessType2SQLTable.getSqlTableName(),
                        Mocks.AccessTypeLinkAttribute.getSQLColumnName(),
                        Mocks.AccessType3SQLTable.getSqlTableName(),
                        Mocks.AccessType2LinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList5(Object.class, Object.class, Object.class, Object.class, Object.class)
                .append("StringValue1A", "StringValue1C", 114L, 115L, 116L)
                .append("StringValue2A", "StringValue2C", 224L, 225L, 226L)
                .append("StringValue3A", "StringValue3C", 334L, 335L, 336L)
                .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType3.getId()), 226L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .linkto(Mocks.AccessTypeLinkAttribute.getName())
                            .linkto(Mocks.AccessType2LinkAttribute.getName())
                            .attribute(Mocks.AccessType3StringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 3);
        eval.next();
        assertEquals(eval.get(1), "StringValue1A");
        assertEquals(eval.get(2), "StringValue1C");
        eval.next();
        assertEquals(eval.get(1), "StringValue2A");
        assertNull(eval.get(2));
        eval.next();
        assertEquals(eval.get(1), "StringValue3A");
        assertEquals(eval.get(2), "StringValue3C");
        assertFalse(eval.next());
    }

    @Test(description = "Basic linkFrom")
    public void testLinkFrom() throws EFapsException
    {
        final String sql = String.format("select T1.ID,T0.ID from %s T0 left join %s T1 on T0.ID=T1.%s",
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                            .append(215L, 116L)
                            .append(315L, 116L)
                            .append(415L, 116L)
                            .asResult())
            .build();

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.SimpleType.getName())
                        .select()
                        .linkfrom(Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName())
                            .instance()
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 1);
        eval.next();
        assertEquals(eval.inst(), Instance.get(Mocks.SimpleType.getName(), "116"));
        assertTrue(eval.get(1) instanceof List);
        assertEquals(((List<?>) eval.get(1)).size(), 3);
    }

    @Test(description = "linkFrom with mulitple results")
    public void testLinkFromMultiple() throws EFapsException
    {
        final String sql = String.format("select T1.ID,T0.ID from %s T0 left join %s T1 on T0.ID=T1.%s",
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                            .append(215L, 116L)
                            .append(315L, 116L)
                            .append(415L, 116L)
                            .append(515L, 117L)
                            .append(515L, 118L)
                            .append(715L, 118L)
                            .asResult())
            .build();

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.SimpleType.getName())
                        .select()
                        .linkfrom(Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName())
                            .instance()
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 3);
        eval.next();
        assertEquals(eval.inst(), Instance.get(Mocks.SimpleType.getName(), "116"));
        assertTrue(eval.get(1) instanceof List);
        assertEquals(((List<?>) eval.get(1)).size(), 3);
        eval.next();
        assertEquals(eval.inst(), Instance.get(Mocks.SimpleType.getName(), "117"));
        assertTrue(eval.get(1) instanceof List);
        assertEquals(((List<?>) eval.get(1)).size(), 1);
        eval.next();
        assertEquals(eval.inst(), Instance.get(Mocks.SimpleType.getName(), "118"));
        assertTrue(eval.get(1) instanceof List);
        assertEquals(((List<?>) eval.get(1)).size(), 2);
    }

    @Test(description = "Access check for Basic linkFrom")
    public void testAccessLinkFromBasic() throws EFapsException
    {
        final String sql = String.format("select T1.ID,T0.ID from %s T0 left join %s T1 on T0.ID=T1.%s",
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                            .append(215L, 116L)
                            .append(315L, 116L)
                            .append(415L, 116L)
                            .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 116L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .linkfrom(Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName())
                            .instance()
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 0);
    }

    @Test(description = "Access check for Basic linkFrom")
    public void testAccessLinkFromLeft() throws EFapsException
    {
        final String sql = String.format("select T1.ID,T0.ID from %s T0 left join %s T1 on T0.ID=T1.%s",
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.RelationAccessTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionAccessFromLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                            .append(215L, 116L)
                            .append(315L, 116L)
                            .append(415L, 116L)
                            .asResult())
            .build();

        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.RelationAccessType.getId()), 315L), false);

        final Evaluator eval = EQL.builder()
                        .print()
                        .query(Mocks.AccessType.getName())
                        .select()
                        .linkfrom(Mocks.RelationAccessType.getName(), Mocks.RealtionAccessFromLinkAttribute.getName())
                            .instance()
                        .stmt()
                        .execute()
                        .evaluate();
        assertEquals(eval.count(), 1);
        eval.next();
        assertEquals(eval.inst(), Instance.get(Mocks.AccessType.getName(), "116"));
        final Object result = eval.get(1);
        assertTrue(result instanceof List);
        assertEquals(((List<?>) result).size(), 3);
        assertEquals(((List<?>) result).get(0), Instance.get(Mocks.RelationAccessType.getName(), "215"));
        assertNull(((List<?>) result).get(1));
        assertEquals(((List<?>) result).get(2), Instance.get(Mocks.RelationAccessType.getName(), "415"));
    }

    @Test
    public void testGetDataList()
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

        final DataList dataList = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(Mocks.AllAttrStringAttribute.getName(), Mocks.AllAttrLongAttribute.getName())
            .stmt()
            .evaluate()
            .getDataList();

        assertEquals(dataList.size(), 3);
        assertTrue(dataList.get(0).getValues().get(0) instanceof StringValue);
        assertEquals(dataList.get(0).getValues().get(0).getKey(), "1");
        assertTrue(dataList.get(0).getValues().get(1) instanceof LongValue);
        assertEquals(dataList.get(0).getValues().get(1).getKey(), "2");
    }

    @Test
    public void testGetByCIAttribute()
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
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(CI.AllAttrType.StringAttribute, CI.AllAttrType.LongAttribute)
            .stmt()
            .evaluate();
        eval.next();
        assertEquals(eval.get(CI.AllAttrType.StringAttribute), "StringValue1");
        assertEquals(eval.get(CI.AllAttrType.LongAttribute), Long.valueOf(1));
    }

    @Test
    public void testGetByCIAttributeOverwriteAlias()
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
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(CI.AllAttrType.StringAttribute).as("Bethoven")
            .attribute(CI.AllAttrType.LongAttribute)
            .stmt()
            .evaluate();
        eval.next();
        assertEquals(eval.get("Bethoven"), "StringValue1");
        assertEquals(eval.get(CI.AllAttrType.LongAttribute), Long.valueOf(1));
    }

    @Test
    public void testGetAttributeByIndex()
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
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(CI.AllAttrType.StringAttribute, CI.AllAttrType.LongAttribute)
            .stmt()
            .evaluate();
        eval.next();
        assertEquals(eval.attribute(1).getName(), CI.AllAttrType.StringAttribute.name);
        assertEquals(eval.attribute(2).getName(), CI.AllAttrType.LongAttribute.name);
    }

    @Test
    public void testGetAttributeByAlias()
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
                .asResult())
            .build();

        final Evaluator eval = EQL.builder()
            .print()
            .query(Mocks.AllAttrType.getName())
            .select()
            .attribute(CI.AllAttrType.StringAttribute).as("alias1")
            .attribute(CI.AllAttrType.LongAttribute).as("alias2")
            .stmt()
            .evaluate();
        eval.next();
        assertEquals(eval.attribute("alias1").getName(), CI.AllAttrType.StringAttribute.name);
        assertEquals(eval.attribute("alias2").getName(), CI.AllAttrType.LongAttribute.name);
    }


}

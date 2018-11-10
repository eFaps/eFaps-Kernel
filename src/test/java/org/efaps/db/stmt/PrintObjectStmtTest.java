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
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.efaps.admin.datamodel.Status;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQL;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

/**
 * The Class PrintStmtTest.
 */
public class PrintObjectStmtTest
    extends AbstractTest
{

    @Test
    public void testSimplePrintObject()
        throws EFapsException
    {
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName()));
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
        final String sql = String.format("select T0.TestAttr_COL from T_DEMO T0 where T0.ID = 4",
                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value")
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.TypedType.getId(), Mocks.TypedTypeTestAttr.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test(description = "read a String Attribute")
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrStringAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrLongAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrIntegerAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), Integer.valueOf(101));
    }

    @Test
    public void testBooleanAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(true)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrBooleanAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.get(1));
    }

    @Test
    public void testDateAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDateAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testTimeAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrTimeAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrTimeAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), date.toLocalTime());
    }

    @Test
    public void testDateTimeAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateTimeAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDateTimeAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testCreatedAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrCreatedAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrCreatedAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testModifiedAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrModifiedAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrModifiedAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(strValue, 1L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select linkto[%s].attribute[%s]",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttribute.getName(), Mocks.TestAttribute.getName()));


        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), strValue);
    }

    @Test(description = "read a value using an Alias")
    public void testAttributeWithAliase()
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
        final String alias = "AliasName";

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s] as %s",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrStringAttribute.getName(), alias));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(alias), "A Value");
    }

    @Test
    public void testInstanceSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(4)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select instance",
                        Mocks.AllAttrType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testInstanceTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(4, Mocks.TypedType.getId())
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select instance",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testLinktoInstanceSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T1.ID from %s T0 left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(4)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select linkto[%s].instance",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttribute.getName()));


        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testLinktoInstanceTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T1.ID,T1.TYPE from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttributeTyped.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(4, Mocks.TypedType.getId())
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select linkto[%s].instance",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttributeTyped.getName()));


        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testOIDSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(4)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select oid",
                        Mocks.AllAttrType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), instance.getOid());
    }

    @Test
    public void testOIDTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(4, Mocks.TypedType.getId())
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(String.format("print obj %s.4 select oid",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), instance.getOid());
    }

    @Test(description = "Exec select without any other selects")
    public void testSelectExec()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0 where T0.ID = 4",
                        Mocks.SimpleTypeSQLTable.getSqlTableName());
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Long.class)
                .append(4L)
                .asResult())
            .build();
        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final String stmtStr = String.format("print obj %s select exec %s as barcode",
                        instance.getOid(), org.efaps.mock.esjp.SimpleSelect.class.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get("barcode"),
                        String.format(org.efaps.mock.esjp.SimpleSelect.FORMAT, instance.getOid()));
    }

    @Test(description = "Exec select with another select combined")
    public void testSelectExec2()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                    .append("A Value", 4L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final String stmtStr = String.format("print obj %s select attribute[%s], exec %s as barcode",
                        instance.getOid(),  Mocks.TestAttribute.getName(),
                        org.efaps.mock.esjp.SimpleSelect.class.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value");
        assertEquals(evaluator.get("barcode"),
                        String.format(org.efaps.mock.esjp.SimpleSelect.FORMAT, instance.getOid()));
    }

    @Test(description = "Single class select")
    public void testSelectClass()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T1.ID from %s T0 left join %s T1 on T0.%s=T1.%s where T0.ID = 4",
                        Mocks.ClassTypeStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.ClassTypeSQLTable.getSqlTableName(),
                        Mocks.IDAttribute.getSQLColumnName(),
                        Mocks.ClassTypeLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                    .append("A Value", 4L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final String stmtStr = String.format("print obj %s select class[%s].attribute[%s]",
                        instance.getOid(), Mocks.ClassType.getName(), Mocks.ClassTypeStringAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test(description = "Linkto with a  class select")
    public void testLinktoSelectClass()
        throws EFapsException
    {
        final String sql = String.format("select T2.%s,T1.ID,T2.ID "
                        + "from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID "
                        + "left join %s T2 on T1.%s=T2.%s where T0.ID = 4",
                        Mocks.ClassTypeStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName(),
                        Mocks.ClassTypeSQLTable.getSqlTableName(),
                        Mocks.IDAttribute.getSQLColumnName(),
                        Mocks.ClassTypeLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(String.class, Long.class, Long.class)
                    .append("A Value", 14L, 33L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final String stmtStr = String.format("print obj %s select linkto[%s].class[%s].attribute[%s]",
                        instance.getOid(), Mocks.AllAttrLinkAttribute.getName(), Mocks.ClassType.getName(),
                        Mocks.ClassTypeStringAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test(description = "read a Attribute from a child table without attribute in main table")
    public void testAttributeInChildTable()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s "
                        + "from %s T0 "
                        + "left join %s T1 on T0.ID=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrInChildSQLAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrTypeChildSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value in Child SQL Table")
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrInChildSQLAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value in Child SQL Table");
    }

    @Test(description = "read a Attribute from a child table")
    public void testAttributeInChildTable2()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s "
                        + "from %s T0 "
                        + "left join %s T1 on T0.ID=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrDecimalAttribute.getSQLColumnName(),
                        Mocks.AllAttrInChildSQLAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrTypeChildSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(BigDecimal.class, String.class)
                        .append(BigDecimal.ZERO, "A Value in Child SQL Table")
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s], attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDecimalAttribute.getName(),
                        Mocks.AllAttrInChildSQLAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(((BigDecimal) evaluator.get(1)).intValue(), BigDecimal.ZERO.intValue());
        assertEquals(evaluator.get(2), "A Value in Child SQL Table");
    }


    @Test(description = "read a linked to Attribute from a child table without attribute in main table")
    public void testLinktoAttributeInChildTable()
        throws EFapsException
    {
        final String sql = String.format("select T2.%s,T1.ID "
                        + "from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID "
                        + "left join %s T2 on T1.ID=T2.ID "
                        + "where T0.ID = 4",
                        Mocks.SimpleTypeInChildSQLAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeChildSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                        .append("A Value in Child SQL Table", 77L)
                        .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final String stmtStr = String.format("print obj %s select linkto[%s].attribute[%s]",
                        instance.getOid(), Mocks.AllAttrLinkAttribute.getName(),
                        Mocks.SimpleTypeInChildSQLAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value in Child SQL Table");
    }

    @Test(description = "read Attribute linked to Attribute from a child table")
    public void testAttributeLinktoAttributeInChildTable()
        throws EFapsException
    {
        final String sql = String.format("select T2.%s,T2.ID "
                        + "from %s T0 "
                        + "left join %s T1 on T0.ID=T1.ID "
                        + "left join %s T2 on T1.AllAttrLinkInChildSQLAttribute_COL=T2.ID "
                        + "where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrTypeChildSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkInChildSQLAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                        .append("A Value in Child SQL Table", 77L)
                        .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final String stmtStr = String.format("print obj %s select linkto[%s].attribute[%s]",
                        instance.getOid(), Mocks.AllAttrLinkInChildSQLAttribute.getName(),
                        Mocks.TestAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value in Child SQL Table");
    }

    @Test(description = "read Attribute with format")
    public void testAttributeWithFormat()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select attribute[%s].format[YYYY]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDateAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), date.toString("YYYY"));
    }

    @Test
    public void testStatus()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                                        Mocks.StatusAttribute.getSQLColumnName(),
                                        Mocks.StatusTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(Mocks.StatusGrp.getStatusId("Open"))
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select status",
                        Mocks.StatusType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        final Status status = evaluator.get(1);
        assertEquals(Long.valueOf(status.getId()), Mocks.StatusGrp.getStatusId("Open"));
    }

    @Test
    public void testStatusKey()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                                        Mocks.StatusAttribute.getSQLColumnName(),
                                        Mocks.StatusTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(Mocks.StatusGrp.getStatusId("Open"))
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select status.key",
                        Mocks.StatusType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "Open");
    }

    @Test
    public void testStatusLabel()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                                        Mocks.StatusAttribute.getSQLColumnName(),
                                        Mocks.StatusTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(Mocks.StatusGrp.getStatusId("Open"))
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL.parse(
                        String.format("print obj %s.4 select status.label",
                        Mocks.StatusType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "?? - TestStatusGroup/Key.Status.Open - ??");
    }
}


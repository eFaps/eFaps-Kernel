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
package org.efaps.db.stmt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQL2;
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName()));
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder()
            .withSql("select T0.TestAttribute_COL,T0.ID from T_DEMO T0 where T0.ID = 4")
            .build();
        printStmt.execute();
        verify.verify();
    }

    @Test
    public void testSimplePrintObjectValue()
        throws EFapsException
    {
        final String sql = String.format("select T0.TestAttr_COL,T0.ID,T0.TYPE from T_DEMO T0 where T0.ID = 4",
                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(String.class, Long.class, Long.class)
                        .append("A Value", 4L, Mocks.TypedType.getId())
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                        .append("A Value", 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(100, 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrIntegerAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(101, 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(true, 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final LocalDate date = LocalDate.of(2019, 8, 23);
        final Timestamp timestamp = Timestamp.valueOf(LocalDateTime.of(date, LocalTime.MIN));
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(timestamp, 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrTimeAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final LocalTime time = LocalTime.of(22, 31, 15);
        final Timestamp timestamp = Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), time));
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(timestamp, 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrTimeAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), time);
    }

    @Test
    public void testDateTimeAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateTimeAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final OffsetDateTime dateTime = OffsetDateTime.now();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(Timestamp.from(dateTime.toInstant()), 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDateTimeAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(0, ChronoUnit.SECONDS.between(evaluator.get(1), dateTime));
    }

    @Test
    public void testCreatedAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrCreatedAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final OffsetDateTime dateTime = OffsetDateTime.now();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(Timestamp.from(dateTime.toInstant()), 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrCreatedAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(0, ChronoUnit.SECONDS.between(evaluator.get(1), dateTime));
    }

    @Test
    public void testModifiedAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrModifiedAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final OffsetDateTime dateTime = OffsetDateTime.now();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(Timestamp.from(dateTime.toInstant()), 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrModifiedAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(0, ChronoUnit.SECONDS.between(evaluator.get(1), dateTime));
    }

    @Test
    public void testLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Long.class, Object.class)
                    .append(strValue, 4L, 1L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                        .append("A Value", 4L)
                        .asResult())
            .build();
        final String alias = "AliasName";

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T1.ID,T0.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                    .append(4, 4L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T1.ID,T1.TYPE,T0.ID from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttributeTyped.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Object.class, Long.class)
                    .append(4, Mocks.TypedType.getId(), 4L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(String.format("print obj %s.4 select oid",
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(stmtStr);
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
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(stmtStr);
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
        final String sql = String.format("select T1.%s,T0.ID,T1.ID "
                        + "from %s T0 left join %s T1 on T0.%s=T1.%s where T0.ID = 4",
                        Mocks.ClassTypeStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.ClassTypeSQLTable.getSqlTableName(),
                        Mocks.IDAttribute.getSQLColumnName(),
                        Mocks.ClassTypeLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(String.class, Long.class, Long.class)
                    .append("A Value", 4L, 4L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final String stmtStr = String.format("print obj %s select class[%s].attribute[%s]",
                        instance.getOid(), Mocks.ClassType.getName(), Mocks.ClassTypeStringAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test(description = "Linkto with a  class select")
    public void testLinktoSelectClass()
        throws EFapsException
    {
        final String sql = String.format("select T2.%s,T0.ID,T1.ID,T2.ID "
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
            .withResult(RowLists.rowList4(String.class, Long.class, Long.class, Long.class)
                    .append("A Value", 4L, 14L, 33L)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final String stmtStr = String.format("print obj %s select linkto[%s].class[%s].attribute[%s]",
                        instance.getOid(), Mocks.AllAttrLinkAttribute.getName(), Mocks.ClassType.getName(),
                        Mocks.ClassTypeStringAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test(description = "read a Attribute from a child table without attribute in main table")
    public void testAttributeInChildTable()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID "
                        + "from %s T0 "
                        + "left join %s T1 on T0.ID=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrInChildSQLAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrTypeChildSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                        .append("A Value in Child SQL Table", 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T1.%s,T0.ID "
                        + "from %s T0 "
                        + "left join %s T1 on T0.ID=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrDecimalAttribute.getSQLColumnName(),
                        Mocks.AllAttrInChildSQLAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrTypeChildSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(BigDecimal.class, String.class, Long.class)
                        .append(BigDecimal.ZERO, "A Value in Child SQL Table", 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T2.%s,T0.ID,T1.ID "
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
            .withResult(RowLists.rowList3(String.class, Long.class, Long.class)
                        .append("A Value in Child SQL Table", 4L, 77L)
                        .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final String stmtStr = String.format("print obj %s select linkto[%s].attribute[%s]",
                        instance.getOid(), Mocks.AllAttrLinkAttribute.getName(),
                        Mocks.SimpleTypeInChildSQLAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value in Child SQL Table");
    }

    @Test(description = "read Attribute linked to Attribute from a child table")
    public void testAttributeLinktoAttributeInChildTable()
        throws EFapsException
    {
        final String sql = String.format("select T2.%s,T0.ID,T2.ID "
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
            .withResult(RowLists.rowList3(String.class, Long.class, Long.class)
                        .append("A Value in Child SQL Table", 4L, 77L)
                        .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final String stmtStr = String.format("print obj %s select linkto[%s].attribute[%s]",
                        instance.getOid(), Mocks.AllAttrLinkInChildSQLAttribute.getName(),
                        Mocks.TestAttribute.getName());
        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "A Value in Child SQL Table");
    }

    @Test(description = "read Attribute with format")
    public void testAttributeWithFormat()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                        .append(date.toDate(), 4L)
                        .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select attribute[%s].format[yyyy]",
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.StatusAttribute.getSQLColumnName(),
                                        Mocks.StatusTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                    .append(Mocks.StatusGrp.getStatusId("Open"), 4L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.StatusAttribute.getSQLColumnName(),
                                        Mocks.StatusTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                    .append(Mocks.StatusGrp.getStatusId("Open"), 4L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.StatusAttribute.getSQLColumnName(),
                                        Mocks.StatusTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Long.class)
                    .append(Mocks.StatusGrp.getStatusId("Open"), 4L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select status.label",
                        Mocks.StatusType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "?? - TestStatusGroup/Key.Status.Open - ??");
    }

    @Test
    public void testTypeSimpleType()
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select type",
                        Mocks.AllAttrType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt).execute().evaluate();
        final Type type = evaluator.get(1);
        assertEquals(Long.valueOf(type.getId()), Mocks.AllAttrType.getId());
    }

    @Test
    public void testTypeTypedType()
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select type",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        final Type type = evaluator.get(1);
        assertEquals(Long.valueOf(type.getId()), Mocks.TypedType.getId());
    }

    @Test
    public void testTypeLabel()
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select type.label",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), "?? - " + Mocks.TypedType.getName() + ".Label - ??");
    }

    @Test
    public void testTypeName()
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select type.name",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), Mocks.TypedType.getName());
    }

    @Test
    public void testTypeID()
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select type.id",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), Mocks.TypedType.getId());
    }

    @Test
    public void testTypeUUID()
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

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select type.uuid",
                        Mocks.TypedType.getId()));

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertEquals(evaluator.get(1), Mocks.TypedType.getUuid());
    }

    @Test
    public void testLinkFromAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 left join %s T1 on T0.ID=T1.%s "
                        + "where T0.ID = 4",
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Long.class, Object.class)
                    .append(strValue, 4L, 1L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select linkfrom[%s#%s].attribute[%s]",
                    Mocks.SimpleType.getId(), Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName(),
                    Mocks.RealtionStringAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), Collections.singletonList(strValue));
    }

    @Test
    public void testLinkFromAttributeFirst()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 left join %s T1 on T0.ID=T1.%s "
                        + "where T0.ID = 4",
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName());

        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Long.class, Object.class)
                    .append(strValue, 4L, 1L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select linkfrom[%s#%s].attribute[%s].first",
                    Mocks.SimpleType.getId(), Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName(),
                    Mocks.RealtionStringAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), strValue);
    }

    @Test
    public void testLinkFromFilterAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 left join %s T1 on T0.ID=T1.%s "
                        + "and T1.%s = 'test' where T0.ID = 4",
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName(),
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.RealtionStringAttribute.getSQLColumnName());

        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Long.class, Object.class)
                    .append(strValue, 4L, 1L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                        String.format("print obj %s.4 select linkfrom[%s#%s, filter %s == 'test'].attribute[%s]",
                    Mocks.SimpleType.getId(), Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName(),
                    Mocks.RealtionStringAttribute.getName(), Mocks.RealtionStringAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), Collections.singletonList(strValue));
    }

    @Test
    public void testLinkFromFilterAttributeSelect()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 left join %s T1 on T0.ID=T1.%s "
                        + "and T1.%s = 'test' where T0.ID = 4",
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.RelationTypeSQLTable.getSqlTableName(),
                        Mocks.RealtionFromLinkAttribute.getSQLColumnName(),
                        Mocks.RealtionStringAttribute.getSQLColumnName(),
                        Mocks.RealtionStringAttribute.getSQLColumnName());

        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(Object.class, Long.class, Object.class)
                    .append(strValue, 4L, 1L)
                    .asResult())
            .build();

        final IPrintObjectStatement stmt = (IPrintObjectStatement) EQL2.parse(
                    String.format("print obj %s.4 select linkfrom[%s#%s, filter attribute[%s] == 'test'].attribute[%s]",
                    Mocks.SimpleType.getId(), Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName(),
                    Mocks.RealtionStringAttribute.getName(), Mocks.RealtionStringAttribute.getName()));

        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluate();
        assertEquals(evaluator.get(1), Collections.singletonList(strValue));
    }

}


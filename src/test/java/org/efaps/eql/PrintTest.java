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

import org.efaps.db.Instance;
import org.efaps.eql.builder.Selectables;
import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.CI;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

/**
 * The Class PrintTest.
 */
public class PrintTest
    extends AbstractTest
{

    @Test
    public void testObjPrintOneAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.SimpleType.getId() + ".4")
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintVariousAttributes()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .attribute(Mocks.AllAttrBooleanAttribute.getName(),
                       Mocks.AllAttrStringAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintVariousAttributes2()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .attribute(Mocks.AllAttrBooleanAttribute.getName())
            .attribute(Mocks.AllAttrStringAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintAttributeWithAlias()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .attribute(Mocks.AllAttrBooleanAttribute.getName())
            .as("BlaBla")
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintAttributesWithAlias()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .attribute(Mocks.AllAttrBooleanAttribute.getName()).as("BlaBla")
            .attribute(Mocks.AllAttrStringAttribute.getName()).as("BlaBla2")
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T1.ID from %s T0 left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .linkto(Mocks.AllAttrLinkAttribute.getName())
                .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testAttributeAndLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .attribute(Mocks.AllAttrStringAttribute.getName())
            .linkto(Mocks.AllAttrLinkAttribute.getName())
                .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testOIDSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .oid()
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testOIDTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                                        Mocks.TypedType.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Mocks.TypedType.getId() + ".4")
            .oid()
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectCIAttr()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(CI.TypedType.TestAttr)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectCIAttrs()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s from %s T0 where T0.ID = 4",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeIDAttribute.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(CI.TypedType.TestAttr, CI.TypedType.ID)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T1.ID from %s T0 left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.print(Instance.get(Mocks.AllAttrType.getId() + ".4"))
            .select(Selectables.linkto(CI.AllAttrType.LinkAttribute).attr(CI.SimpleType.TestAttr))
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectAttributeAndLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.print(Mocks.AllAttrType.getId() + ".4")
            .select(CI.AllAttrType.StringAttribute,
                Selectables.linkto(CI.AllAttrType.LinkAttribute).attr(CI.SimpleType.TestAttr))
            .stmt()
            .execute();

        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectInstance()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                        Mocks.TypedTypeSQLTable.getSqlTableName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(Selectables.instance())
            .stmt()
            .execute();
        verify.verify();

    }
}

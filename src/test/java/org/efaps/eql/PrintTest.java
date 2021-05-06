/*
 * Copyright 2003 - 2021 The eFaps Team
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.eql.builder.Selectables;
import org.efaps.eql2.StmtFlag;
import org.efaps.eql2.bldr.AbstractSelectables;
import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.CI;
import org.efaps.mock.datamodel.Company;
import org.efaps.mock.datamodel.Company.CompanyBuilder;
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.SimpleType.getId() + ".4")
            .attribute(Mocks.TestAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintOneAttributeUsingCI()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.SimpleType.getId() + ".4")
            .attribute(CI.SimpleType.TestAttr)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintVariousAttributes()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        final String sql = String.format("select T0.%s,T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        final String sql = String.format("select T0.%s,T0.%s,T0.ID from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
            .attribute(Mocks.AllAttrBooleanAttribute.getName()).as("BlaBla")
            .attribute(Mocks.AllAttrStringAttribute.getName()).as("BlaBla2")
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testObjPrintRespectsCompany()
        throws EFapsException
    {
        final Company company = new CompanyBuilder()
                        .withName("Mock Company")
                        .build();
        Context.getThreadContext().setCompany(org.efaps.admin.user.Company.get(company.getId()));

        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.ID = 4 and T0.%s = %s" ,
                        Mocks.CompanyStringAttribute.getSQLColumnName(),
                        Mocks.CompanyTypeSQLTable.getSqlTableName(),
                        Mocks.CompanyCompanyAttribute.getSQLColumnName(),
                        company.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Mocks.CompanyType.getId() + ".4")
            .attribute(Mocks.CompanyStringAttribute.getName())
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintRespectsCompany()
        throws EFapsException
    {
        final Company company = new CompanyBuilder()
                        .withName("Mock Company")
                        .build();
        Context.getThreadContext().setCompany(org.efaps.admin.user.Company.get(company.getId()));

        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s = %s" ,
                        Mocks.CompanyStringAttribute.getSQLColumnName(),
                        Mocks.CompanyTypeSQLTable.getSqlTableName(),
                        Mocks.CompanyCompanyAttribute.getSQLColumnName(),
                        company.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print()
            .query(CI.CompanyType)
            .select()
            .attribute(CI.CompanyType.StringAttribute)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintRespectsCompanyIndependent()
        throws EFapsException, IllegalAccessException
    {
        final Company company = new CompanyBuilder()
                        .withName("Mock Company")
                        .build();
        Context.getThreadContext().setCompany(org.efaps.admin.user.Company.get(company.getId()));

        final Person person = Context.getThreadContext().getPerson();
        FieldUtils.writeDeclaredField(person, "companies", Collections.singleton(company.getId()), true);

        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s = %s" ,
                        Mocks.CompanyStringAttribute.getSQLColumnName(),
                        Mocks.CompanyTypeSQLTable.getSqlTableName(),
                        Mocks.CompanyCompanyAttribute.getSQLColumnName(),
                        company.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .with(StmtFlag.COMPANYINDEPENDENT)
            .print()
                .query(CI.CompanyType)
            .select()
                .attribute(CI.CompanyType.StringAttribute)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintRespectsCompanyIndependentMultiple()
        throws EFapsException, IllegalAccessException
    {
        final Company company = new CompanyBuilder()
                        .withName("Mock Company1")
                        .build();
        final Company company2 = new CompanyBuilder()
                        .withName("Mock Company2")
                        .build();
        Context.getThreadContext().setCompany(org.efaps.admin.user.Company.get(company.getId()));

        final Person person = Context.getThreadContext().getPerson();
        FieldUtils.writeDeclaredField(person, "companies",
                        new LinkedHashSet<>(Arrays.asList(company.getId(), company2.getId())), true);

        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s in (%s,%s)" ,
                        Mocks.CompanyStringAttribute.getSQLColumnName(),
                        Mocks.CompanyTypeSQLTable.getSqlTableName(),
                        Mocks.CompanyCompanyAttribute.getSQLColumnName(),
                        company.getId(),
                        company2.getId());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .with(StmtFlag.COMPANYINDEPENDENT)
            .print()
                .query(CI.CompanyType)
            .select()
                .attribute(CI.CompanyType.StringAttribute)
            .stmt()
            .execute();
        verify.verify();
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
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        final String sql = String.format("select T0.%s,T1.%s,T0.ID,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        EQL.builder()
            .print(Mocks.TypedType.getId() + ".4")
            .oid()
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectCIAttr()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(CI.TypedType.TestAttr)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectAttr()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(AbstractSelectables.attribute(CI.TypedType.TestAttr.name))
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectCIAttrs()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.%s,T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                        Mocks.TypedTypeIDAttribute.getSQLColumnName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.builder()
            .print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(CI.TypedType.TestAttr, CI.TypedType.ID)
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T0.ID,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.builder()
            .print(Instance.get(Mocks.AllAttrType.getId() + ".4"))
            .select(Selectables.linkto(CI.AllAttrType.LinkAttribute).attr(CI.SimpleType.TestAttr))
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testPrintInstanceSelectAttributeAndLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T1.%s,T0.ID,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.builder()
            .print(Mocks.AllAttrType.getId() + ".4")
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
        EQL.builder()
            .print(Instance.get(Mocks.TypedType.getId() + ".4"))
            .select(AbstractSelectables.instance())
            .stmt()
            .execute();
        verify.verify();
    }
}

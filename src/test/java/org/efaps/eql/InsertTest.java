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

package org.efaps.eql;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.CI;
import org.efaps.mock.datamodel.Company;
import org.efaps.mock.datamodel.Company.CompanyBuilder;
import org.efaps.mock.esjp.AccessCheck;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

public class InsertTest
    extends AbstractTest
{
    @Test
    public void testInsertOneAttribute()
        throws EFapsException
    {
        final String sql = String.format("insert into %s (%s,ID)values(?,?)",
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.TestAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.insert(CI.SimpleType)
            .set(CI.SimpleType.TestAttr, "A Value")
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testInsertMultipleAttributes()
        throws EFapsException
    {
        final String sql = String.format("insert into %s (%s,%s,%s,%s,%s,ID)values(?,?,?,?,?,?)",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrIntegerAttribute.getSQLColumnName(),
                        Mocks.AllAttrDecimalAttribute.getSQLColumnName(),
                        Mocks.AllAttrDateAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.insert(CI.AllAttrType)
            .set(CI.AllAttrType.StringAttribute, "A String Value")
            .set(CI.AllAttrType.LongAttribute, 248651L)
            .set(CI.AllAttrType.IntegerAttribute, 4)
            .set(CI.AllAttrType.DecimalAttribute, new BigDecimal("12.98"))
            .set(CI.AllAttrType.DateAttribute, LocalDate.of(2018, 8, 22))
            .stmt()
            .execute();
        verify.verify();
        final List<Parameter> parameters = verify.getSqlParameters();
        assertEquals(parameters.get(0).getValue(), "A String Value");
        assertEquals(parameters.get(1).getValue(), 248651L);
        assertEquals(parameters.get(2).getValue(), 4L);
        assertEquals(parameters.get(3).getValue(), new BigDecimal("12.98"));
        assertEquals(parameters.get(4).getKey().sqlTypeName, "TIMESTAMP");
        assertEquals(parameters.get(4).getValue().toString(), "2018-08-22 00:00:00.0");
    }

    @Test
    public void testInsertReturnsInstance()
        throws EFapsException
    {
        final String sql = String.format("select nextval('%s_ID_SEQ')",
                        Mocks.SimpleTypeSQLTable.getSqlTableName());

        MockResult.builder().withSql(sql)
                    .withResult(RowLists.rowList1(Long.class)
                                    .append(3435L)
                                    .asResult())
                    .build();

        final Instance instance = EQL.insert(CI.SimpleType)
            .set(CI.SimpleType.TestAttr, "A Value")
            .stmt()
            .execute();
        assertEquals(instance.getType(), CI.SimpleType.getType());
        assertEquals(instance.getId(), 3435L);
    }

    @Test
    public void testInsertSetsCompany()
        throws EFapsException
    {
        final Company company = new CompanyBuilder()
                        .withName("Mock Company")
                        .build();
        Context.getThreadContext().setCompany(org.efaps.admin.user.Company.get(company.getId()));
        final String sql = String.format("insert into %s (%s,%s,ID)values(?,?,?)",
                        Mocks.CompanyTypeSQLTable.getSqlTableName(),
                        Mocks.CompanyCompanyAttribute.getSQLColumnName(),
                        Mocks.CompanyStringAttribute.getSQLColumnName(),
                        Mocks.CompanyStringAttribute);

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.insert(CI.CompanyType)
            .set(CI.CompanyType.StringAttribute, "A Value")
            .stmt()
            .execute();
        verify.verify();
    }

    @Test(expectedExceptions = { EFapsException.class })
    public void testInsertNoAccess()
        throws EFapsException
    {
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 0L), false);
        EQL.insert(Mocks.AccessType.getName())
            .set(Mocks.AccessTypeStringAttribute.getName(), "A Value")
            .stmt()
            .execute();
    }

    @Test
    public void testInsertAccess()
        throws EFapsException
    {
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 0L), true);
        final String sql = String.format("insert into %s (%s,ID)values(?,?)",
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessTypeStringAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.insert(Mocks.AccessType.getName())
            .set(Mocks.AccessTypeStringAttribute.getName(), "A Value")
            .stmt()
            .execute();
        verify.verify();
    }
}

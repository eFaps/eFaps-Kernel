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

import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.testng.annotations.Test;

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
        //verify.verify();
    }
}

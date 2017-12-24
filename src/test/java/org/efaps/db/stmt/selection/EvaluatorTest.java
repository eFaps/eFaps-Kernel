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

package org.efaps.db.stmt.selection;

import static org.testng.Assert.assertEquals;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.eql.EQL;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
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

        final Evaluator eval = EQL.print(EQL.query(Mocks.AllAttrType.getName()))
            .attribute(Mocks.AllAttrStringAttribute.getName(), Mocks.AllAttrLongAttribute.getName())
            .stmt()
            .execute()
            .evaluator();

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

        final Evaluator eval = EQL.print(EQL.query(Mocks.AccessType.getName()))
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluator();

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

        final Evaluator eval = EQL.print(EQL.query(Mocks.AccessType.getName()))
                        .attribute(Mocks.AccessTypeStringAttribute.getName())
                        .stmt()
                        .execute()
                        .evaluator();

        assertEquals(eval.count(), 2);
    }
}

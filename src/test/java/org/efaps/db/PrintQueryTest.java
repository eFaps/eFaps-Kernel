/*
 * Copyright 2003 - 2018 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.db;

import static org.testng.Assert.assertEquals;

import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

/**
 * The Class PrintQueryTest.
 */
public class PrintQueryTest
    extends AbstractTest
{

    @Test
    public void testSimpleType()
        throws EFapsException
    {
        MockResult.builder()
            .withSql("select T0.ID,T0.TestAttribute_COL from T_DEMO T0 where T0.ID in ( 1 )")
            .withResult(RowLists.rowList2(Long.class, String.class)
                    .append(1L, "A Value")
                    .asResult())
            .build();

        final String oid = String.format("%s.1", Mocks.SimpleType.getId());
        final PrintQuery print = new PrintQuery(Instance.get(oid));
        print.addAttribute(Mocks.TestAttribute.getName());
        print.execute();
        assertEquals(print.getAttribute(Mocks.TestAttribute.getName()), "A Value");
    }

    @Test
    public void testTypedType()
        throws EFapsException
    {
        MockResult.builder()
            .withSql(String.format("select T0.ID,T0.TYPE,T0.%s from %s T0 where T0.ID in ( 1 )",
                            Mocks.TypedTypeTestAttr.getSQLColumnName(),
                            Mocks.TypedTypeSQLTable.getSqlTableName()))
            .withResult(RowLists.rowList3(Long.class, Long.class, String.class)
                    .append(1L, Mocks.TypedType.getId(), "This is a Value")
                    .asResult())
            .build();

        final String oid = String.format("%s.1", Mocks.TypedType.getId());
        final PrintQuery print = new PrintQuery(Instance.get(oid));
        print.addAttribute(Mocks.TypedTypeTestAttr.getName());
        print.execute();
        assertEquals(print.getAttribute(Mocks.TypedTypeTestAttr.getName()), "This is a Value");
    }
}

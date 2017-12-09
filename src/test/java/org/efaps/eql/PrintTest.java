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
}

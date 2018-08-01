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

import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.CI;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

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
        final String sql = String.format("insert into %s (%s,%s,%s,ID)values(?,?,?,?)",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrIntegerAttribute.getSQLColumnName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();
        EQL.insert(CI.AllAttrType)
            .set(CI.AllAttrType.StringAttribute, "A String Value")
            .set(CI.AllAttrType.LongAttribute, 248651L)
            .set(CI.AllAttrType.IntegerAttribute, 4)
            .stmt()
            .execute();
        verify.verify();
    }
}

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

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.mock.Mocks;
import org.efaps.mock.esjp.AccessCheck;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;


public class DeleteTest
    extends AbstractTest
{
    @Test
    public void testDeleteOid()
        throws EFapsException
    {
        final String sql = String.format("delete from %s where ID=4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.delete(Mocks.AllAttrType.getId() + ".4")
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testDeleteOidChildTable()
        throws EFapsException
    {
        final String sql1 = String.format("delete from %s where ID=4",
                        Mocks.SimpleTypeSQLTable.getSqlTableName());
        final String sql2 = String.format("delete from %s where ID=4",
                        Mocks.SimpleTypeChildSQLTable.getSqlTableName());

        final SQLVerify verify1 = SQLVerify.builder().withSql(sql1).build();
        final SQLVerify verify2 = SQLVerify.builder().withSql(sql2).build();
        EQL.delete(Mocks.SimpleType.getId() + ".4")
            .stmt()
            .execute();
        verify1.verify();
        verify2.verify();
    }

    @Test
    public void testDeleteOids()
        throws EFapsException
    {
        final String sql1 = String.format("delete from %s where ID=4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());
        final String sql2 = String.format("delete from %s where ID=5",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());
        final SQLVerify verify1 = SQLVerify.builder().withSql(sql1).build();
        final SQLVerify verify2 = SQLVerify.builder().withSql(sql2).build();

        EQL.delete(Mocks.AllAttrType.getId() + ".4", Mocks.AllAttrType.getId() + ".5")
            .stmt()
            .execute();
        verify1.verify();
        verify2.verify();
    }

    @Test
    public void testDeleteInstance()
        throws EFapsException
    {
        final String sql = String.format("delete from %s where ID=4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final SQLVerify verify = SQLVerify.builder().withSql(sql).build();

        EQL.delete(Instance.get(Mocks.AllAttrType.getId() + ".4"))
            .stmt()
            .execute();
        verify.verify();
    }

    @Test
    public void testDeleteInstances()
        throws EFapsException
    {
        final String sql1 = String.format("delete from %s where ID=4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());
        final String sql2 = String.format("delete from %s where ID=5",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());
        final SQLVerify verify1 = SQLVerify.builder().withSql(sql1).build();
        final SQLVerify verify2 = SQLVerify.builder().withSql(sql2).build();

        EQL.delete(Instance.get(Mocks.AllAttrType.getId() + ".4"), Instance.get(Mocks.AllAttrType.getId() + ".5"))
            .stmt()
            .execute();
        verify1.verify();
        verify2.verify();
    }

    @Test(expectedExceptions = { EFapsException.class })
    public void testDeleteInstanceNoAccess()
        throws EFapsException
    {
        final Instance inst = Instance.get(Type.get(Mocks.AccessType.getId()), 4L);
        AccessCheck.RESULTS.put(inst, false);
        EQL.delete(inst.getOid())
            .stmt()
            .execute();
    }

    @Test(expectedExceptions = { EFapsException.class })
    public void testDeleteInstancesNoAccess()
        throws EFapsException
    {
        final Instance inst1 = Instance.get(Type.get(Mocks.AccessType.getId()), 3L);
        final Instance inst2 = Instance.get(Type.get(Mocks.AccessType.getId()), 4L);
        AccessCheck.RESULTS.put(inst1, false);
        AccessCheck.RESULTS.put(inst2, false);
        EQL.delete(inst1, inst2)
            .stmt()
            .execute();
    }
}

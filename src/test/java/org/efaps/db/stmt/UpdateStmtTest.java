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

package org.efaps.db.stmt;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.eql2.EQL;
import org.efaps.eql2.IUpdateListStatement;
import org.efaps.eql2.IUpdateObjectStatement;
import org.efaps.mock.Mocks;
import org.efaps.mock.esjp.AccessCheck;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

public class UpdateStmtTest
    extends AbstractTest
{
    @Test
    public void testSimpleUpdateObject()
        throws EFapsException
    {
        final String sql = String.format("update %s set %s=? where ID=?",
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.TestAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder()
                        .withSql(sql)
                        .build();
        final String eql = String.format("update obj %s.4 set attribute[%s] = \"Hello World\"",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName());

        final IUpdateObjectStatement stmt = (IUpdateObjectStatement) EQL.parse(eql);
        final UpdateStmt updateStmt = UpdateStmt.get(stmt);
        updateStmt.execute();
        verify.verify();
    }

    @Test
    public void testUpdateObjectAccess()
        throws EFapsException
    {
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 4L), true);

        final String sql = String.format("update %s set %s=? where ID=?",
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessTypeStringAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder()
                        .withSql(sql)
                        .build();
        final String eql = String.format("update obj %s.4 set attribute[%s] = \"Hello World\"",
                        Mocks.AccessType.getId(), Mocks.AccessTypeStringAttribute.getName());

        final IUpdateObjectStatement stmt = (IUpdateObjectStatement) EQL.parse(eql);
        final UpdateStmt updateStmt = UpdateStmt.get(stmt);
        updateStmt.execute();
        verify.verify();
    }

    @Test(expectedExceptions = { EFapsException.class })
    public void testUpdateObjectNoAccess()
        throws EFapsException
    {
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 4L), false);

        final String eql = String.format("update obj %s.4 set attribute[%s] = \"Hello World\"",
                        Mocks.AccessType.getId(), Mocks.AccessTypeStringAttribute.getName());

        final IUpdateObjectStatement stmt = (IUpdateObjectStatement) EQL.parse(eql);
        final UpdateStmt updateStmt = UpdateStmt.get(stmt);
        updateStmt.execute();
    }

    @Test
    public void testSimpleUpdateList()
        throws EFapsException
    {
        final String sql = String.format("update %s set %s=? where ID in (?,?,?)",
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.TestAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder()
                        .withSql(sql)
                        .build();
        final String eql = String.format("update list (%1$s.4,%1$s.5,%1$s.6) set attribute[%2$s] = \"Hello World\"",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName());

        final IUpdateListStatement stmt = (IUpdateListStatement) EQL.parse(eql);
        final UpdateStmt updateStmt = UpdateStmt.get(stmt);
        updateStmt.execute();
        verify.verify();
    }

    @Test
    public void testUpdateListAccess()
        throws EFapsException
    {
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 4L), true);
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 5L), true);
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 6L), true);

        final String sql = String.format("update %s set %s=? where ID in (?,?,?)",
                        Mocks.AccessTypeSQLTable.getSqlTableName(),
                        Mocks.AccessTypeStringAttribute.getSQLColumnName());
        final SQLVerify verify = SQLVerify.builder()
                        .withSql(sql)
                        .build();
        final String eql = String.format("update list (%1$s.4,%1$s.5,%1$s.6) set attribute[%2$s] = \"Hello World\"",
                        Mocks.AccessType.getId(), Mocks.AccessTypeStringAttribute.getName());

        final IUpdateListStatement stmt = (IUpdateListStatement) EQL.parse(eql);
        final UpdateStmt updateStmt = UpdateStmt.get(stmt);
        updateStmt.execute();
        verify.verify();
    }

    @Test(expectedExceptions = { EFapsException.class })
    public void testUpdateListNoAccess()
        throws EFapsException
    {
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 4L), true);
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 5L), false);
        AccessCheck.RESULTS.put(Instance.get(Type.get(Mocks.AccessType.getId()), 6L), true);

        final String eql = String.format("update list (%1$s.4,%1$s.5,%1$s.6) set attribute[%2$s] = \"Hello World\"",
                        Mocks.AccessType.getId(), Mocks.AccessTypeStringAttribute.getName());

        final IUpdateListStatement stmt = (IUpdateListStatement) EQL.parse(eql);
        final UpdateStmt updateStmt = UpdateStmt.get(stmt);
        updateStmt.execute();
    }
}

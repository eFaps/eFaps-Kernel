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

package org.efaps.eql.builder;

import static org.testng.Assert.assertNotNull;

import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql.EQL;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

public class PrintTest
    extends AbstractTest
{
    @Test
    public void testGetStmt()
    {
        final PrintStmt stmt = EQL.print("123.45").stmt();
        assertNotNull(stmt);
    }

    @Test
    public void testExecute()
        throws EFapsException
    {
        final PrintStmt stmt = EQL.print("123.45").execute();
        assertNotNull(stmt);
    }

    @Test
    public void testEvaluate()
        throws EFapsException
    {
        final Evaluator eval = EQL.print("123.45").evaluate();
        assertNotNull(eval);
    }

}

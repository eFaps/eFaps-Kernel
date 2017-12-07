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

package org.efaps.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

/**
 * The Class QueryBuilderTest.
 */
public class QueryBuilderTest
    extends AbstractTest
{

    @Test
    public void testInit()
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(Mocks.SimpleType.getUuid());
        assertEquals(queryBldr.getType().getUUID(), Mocks.SimpleType.getUuid());
        queryBldr.addWhereAttrEqValue(Mocks.TestAttribute.getName(), "Test");
        final InstanceQuery query = queryBldr.getQuery();
        assertTrue(query.execute().isEmpty());
    }
}

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

import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.efaps.db.Instance;
import org.efaps.eql2.IWhereTerm;
import org.efaps.eql2.impl.WhereElementTerm;
import org.efaps.test.AbstractTest;
import org.testng.annotations.Test;

public class WhereTest
    extends AbstractTest
{

    @Test
    public void testIn()
        throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
        InvocationTargetException
    {
        final List<Instance> instances = new ArrayList<>();
        instances.add(Instance.get(UUID.randomUUID(), 14L));
        instances.add(Instance.get(UUID.randomUUID(), 56L));
        instances.add(Instance.get(UUID.randomUUID(), 99L));
        final TestWhere where = (TestWhere) new TestWhere().in(instances);

        final WhereElementTerm term = (WhereElementTerm) where.getCurrentTerm();
        final List<String> values = term.element().getValuesList();
        assertTrue(values.contains("14"));
        assertTrue(values.contains("56"));
        assertTrue(values.contains("99"));
    }

    public static class TestWhere extends Where {

        @Override
        public IWhereTerm<?> getCurrentTerm()
        {
            return super.getCurrentTerm();
        }
    }
}

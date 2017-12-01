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

import org.efaps.test.AbstractTest;
import org.testng.annotations.Test;

/**
 * The Class InstanceTest.
 */
public class InstanceTest
    extends AbstractTest
{

    @Test
    public void getInstance4String()
    {
        final String oid = String.format("%s.1", DemoType.getId());
        final Instance instance = Instance.get(oid);
        assertEquals(instance.getOid(), oid);
        assertEquals(instance.getType().getName(), "DemoType");
        assertEquals(instance.getId(), 1L);
    }

    @Test
    public void getInstance4UUID()
    {
        final Instance instance = Instance.get(DemoType.getUuid(), 1L);
        assertEquals(instance.getType().getName(), "DemoType");
        assertEquals(instance.getTypeUUID(), DemoType.getUuid());
        assertEquals(Long.valueOf(instance.getType().getId()), DemoType.getId());
        assertEquals(instance.getId(), 1L);
    }
}

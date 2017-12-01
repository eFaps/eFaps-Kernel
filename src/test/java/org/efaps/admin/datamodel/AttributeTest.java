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

package org.efaps.admin.datamodel;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.efaps.test.AbstractTest;
import org.efaps.util.cache.CacheReloadException;
import org.testng.annotations.Test;

/**
 * The Class AttributeTest.
 */
public class AttributeTest
    extends AbstractTest
{
    @Test
    public void testAttributeInstantiated()
        throws CacheReloadException {
        final Attribute attr = Attribute.get(AbstractTest.TestAttribute.getId());
        assertFalse(attr.isRequired());
        assertEquals(attr.getSize(), 500);
        assertEquals(attr.getScale(), 0);
        assertEquals(attr.getParentId(), TestAttribute.getDataModelTypeId());
    }
}

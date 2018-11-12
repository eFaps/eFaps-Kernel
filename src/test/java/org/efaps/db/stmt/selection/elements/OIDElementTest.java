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

package org.efaps.db.stmt.selection.elements;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.efaps.admin.datamodel.Type;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.Test;

public class OIDElementTest
    extends AbstractTest
{

    @Test
    public void testGetThis()
        throws CacheReloadException
    {
        final OIDElement oidElement = new OIDElement(Type.get(Mocks.SimpleType.getId()));
        assertEquals(oidElement.getThis(), oidElement);
    }

    @Test
    public void testGetObjectNull()
        throws EFapsException
    {
        final OIDElement oidElement = new OIDElement(Type.get(Mocks.SimpleType.getId()));
        assertNull(oidElement.getObject(null));
    }

    @Test
    public void testGetObject()
        throws EFapsException
    {
        final OIDElement oidElement = new OIDElement(Type.get(Mocks.SimpleType.getId()));
        Whitebox.setInternalState(oidElement, "idColIdxs", 0);
        Whitebox.setInternalState(oidElement, "typeColIdxs", 1);
        assertEquals(String.format("%s.%s", Mocks.SimpleType.getId(), 33l), oidElement.getObject(new Object[] { 33L,
                        Mocks.SimpleType.getId() }));
    }

}

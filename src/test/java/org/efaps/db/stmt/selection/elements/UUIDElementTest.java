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
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.replay;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.efaps.admin.datamodel.Type;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class UUIDElementTest
    extends AbstractTest
{

    @Test
    public void testGetThis()
    {
        final UUIDElement uuidElement = new UUIDElement();
        assertEquals(uuidElement.getThis(), uuidElement);
    }

    @Test
    public void testGetObjectNull()
        throws EFapsException
    {
        final UUIDElement uuidElement = new UUIDElement();
        assertNull(uuidElement.getObject(null));
    }

    @Test
    public void testGetObjectNullArray()
        throws EFapsException
    {
        final UUIDElement uuidElement = new UUIDElement();
        assertNull(uuidElement.getObject(new Object[] { null }));
    }

    @Test
    public void testGetObject4Type()
        throws EFapsException
    {
        final UUIDElement uuidElement = new UUIDElement();
        final Type type = Type.get(Mocks.TypedType.getId());
        assertEquals(type.getUUID(), uuidElement.getObject(new Object[] { type }));
    }

    @Test
    @PrepareForTest({UUIDElement.class, LoggerFactory.class})
    public void testGetObject4UnexpectedObject()
        throws EFapsException
    {
        final Logger mockLogger = createMock(Logger.class);
        Whitebox.setInternalState(UUIDElement.class, mockLogger);
        final String unexpectedObject = "Should just be returned";
        mockLogger.warn(anyString(), eq(unexpectedObject));
        replay(mockLogger);

        final UUIDElement uuidElement = new UUIDElement();
        assertEquals(unexpectedObject, uuidElement.getObject(new Object[] { unexpectedObject }));
        verify(mockLogger);
    }

}

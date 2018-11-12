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

import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class FormatElementTest
    extends AbstractTest
{
    @Test
    public void testGetThis()
    {
        final FormatElement formatElement = new FormatElement();
        assertEquals(formatElement.getThis(), formatElement);
    }

    @Test
    public void testGetObjectNull()
        throws EFapsException
    {
        final FormatElement formatElement = new FormatElement();
        assertNull(formatElement.getObject(null));
    }

    @Test
    public void testGetObjectNullArray()
        throws EFapsException
    {
        final FormatElement formatElement = new FormatElement();
        assertNull(formatElement.getObject(new Object[] { null }));
    }

    @Test
    public void testGetObject4DateTimeDefaultFormat()
        throws EFapsException
    {
        final FormatElement formatElement = new FormatElement();
        final DateTime date = new DateTime();
        assertEquals(date.toString(), formatElement.getObject(new Object[] { date }));
    }

    @Test
    public void testGetObject4DateTime()
        throws EFapsException
    {
        final FormatElement formatElement = new FormatElement().setPattern("YYYY");
        final DateTime date = new DateTime();
        assertEquals(date.toString("YYYY"), formatElement.getObject(new Object[] { date }));
    }

    @Test
    @PrepareForTest({FormatElement.class, LoggerFactory.class})
    public void testGetObject4UnexpectedObject()
        throws EFapsException
    {
        final Logger mockLogger = createMock(Logger.class);
        Whitebox.setInternalState(UUIDElement.class, mockLogger);
        final String unexpectedObject = "Should just be returned";
        mockLogger.warn(anyString(), eq(unexpectedObject));
        replay(mockLogger);

        final FormatElement formatElement = new FormatElement();
        assertEquals(unexpectedObject, formatElement.getObject(new Object[] { unexpectedObject }));
        verify(mockLogger);
    }
}

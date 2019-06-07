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

package org.efaps.db.stmt.filter;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.eq;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.testng.Assert.assertEquals;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.stmt.selection.elements.NameElement;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.cache.CacheReloadException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.testng.annotations.Test;
public class FilterTest
    extends AbstractTest
{

    @Test
    public void testConvertStatusValueId()
        throws CacheReloadException
    {
        final Attribute attr = Attribute.get(Mocks.StatusAttribute.getId());
        final String openId = String.valueOf(Mocks.StatusGrp.getStatusId("Open"));
        final Filter filter = new Filter();
        assertEquals(filter.convertStatusValue(attr, openId), openId);
    }

    @Test
    public void testConvertStatusValueKey()
        throws CacheReloadException
    {
        final Attribute attr = Attribute.get(Mocks.StatusAttribute.getId());
        final String openId = String.valueOf(Mocks.StatusGrp.getStatusId("Open"));
        final Filter filter = new Filter();
        assertEquals(filter.convertStatusValue(attr, "Open"), openId);
    }

    @Test
    @PrepareForTest({ Logger.class, Filter.class })
    public void testConvertStatusValueInvalidKey()
        throws CacheReloadException
    {
        final Logger mockLogger = createMock(Logger.class);
        Whitebox.setInternalState(NameElement.class, mockLogger);
        final String key = "This is not valid";
        final Attribute attr = Attribute.get(Mocks.StatusAttribute.getId());
        mockLogger.warn(anyString(), eq(key), eq(attr));
        replay(mockLogger);

        final Filter filter = new Filter();
        assertEquals(filter.convertStatusValue(attr, key), key);
    }
}

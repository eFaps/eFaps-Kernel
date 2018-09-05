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

package org.efaps.admin.access;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.UUID;

import org.efaps.util.cache.CacheReloadException;
import org.testng.annotations.Test;

/**
 * The Class AccessTypeEnumTest.
 */
public class AccessTypeEnumTest
{
    @Test
    public void testGet()
        throws CacheReloadException
    {
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.CHECKIN.uuid), AccessTypeEnums.CHECKIN);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.CHECKOUT.uuid), AccessTypeEnums.CHECKOUT);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.CREATE.uuid), AccessTypeEnums.CREATE);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.DELETE.uuid), AccessTypeEnums.DELETE);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.GRANT.uuid), AccessTypeEnums.GRANT);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.MODIFY.uuid), AccessTypeEnums.MODIFY);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.READ.uuid), AccessTypeEnums.READ);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.SHOW.uuid), AccessTypeEnums.SHOW);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.STATUS_BOOST.uuid), AccessTypeEnums.STATUS_BOOST);
       assertEquals(AccessTypeEnums.get(AccessTypeEnums.STATUS_MODIFY.uuid), AccessTypeEnums.STATUS_MODIFY);
       assertNull(AccessTypeEnums.get(UUID.fromString("11110a52-3249-4959-9a4d-c9aadb894104")));
    }
}

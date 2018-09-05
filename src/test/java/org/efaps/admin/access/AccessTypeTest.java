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

import org.efaps.mock.MockResult;
import org.efaps.test.AbstractTest;
import org.efaps.util.cache.CacheReloadException;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

public class AccessTypeTest extends AbstractTest
{

    @Test
    public void testgetAccessTypeById() throws CacheReloadException {
        AccessType.initialize();
        MockResult.builder()
            .withSql("select ID,UUID,NAME from T_ACCESSTYPE T0 where T0.ID = ?")
            .withResult(RowLists.rowList3(Long.class, String.class, String.class)
                        .append(12345L, AccessTypeEnums.CHECKIN.uuid.toString() , "ACCESTYPENAME")
                        .asResult())
            .build();

        final AccessType accessType = AccessType.getAccessType(12345L);
        assertEquals(accessType.getId(), 12345L);
        assertEquals(accessType.getName(), "ACCESTYPENAME");
        assertEquals(accessType.getUUID(), AccessTypeEnums.CHECKIN.uuid);
    }

    @Test
    public void testgetAccessTypeByName() throws CacheReloadException {
        AccessType.initialize();
        MockResult.builder()
            .withSql("select ID,UUID,NAME from T_ACCESSTYPE T0 where T0.NAME = ?")
            .withResult(RowLists.rowList3(Long.class, String.class, String.class)
                        .append(12345L, AccessTypeEnums.CHECKIN.uuid.toString() , "ACCESTYPENAME")
                        .asResult())
            .build();

        final AccessType accessType = AccessType.getAccessType("ACCESTYPENAME");
        assertEquals(accessType.getId(), 12345L);
        assertEquals(accessType.getName(), "ACCESTYPENAME");
        assertEquals(accessType.getUUID(), AccessTypeEnums.CHECKIN.uuid);
    }

    @Test
    public void testgetAccessTypeByUUID() throws CacheReloadException {
        AccessType.initialize();
        MockResult.builder()
            .withSql("select ID,UUID,NAME from T_ACCESSTYPE T0 where T0.UUID = ?")
            .withResult(RowLists.rowList3(Long.class, String.class, String.class)
                        .append(12345L, AccessTypeEnums.CHECKIN.uuid.toString() , "ACCESTYPENAME")
                        .asResult())
            .build();

        final AccessType accessType = AccessType.getAccessType(AccessTypeEnums.CHECKIN.uuid);
        assertEquals(accessType.getId(), 12345L);
        assertEquals(accessType.getName(), "ACCESTYPENAME");
        assertEquals(accessType.getUUID(), AccessTypeEnums.CHECKIN.uuid);
    }

    @Test
    public void testgetAccessTypeIsCached() throws CacheReloadException {
        AccessType.initialize();
        MockResult.builder()
            .withSql("select ID,UUID,NAME from T_ACCESSTYPE T0 where T0.ID = ?")
            .withResult(RowLists.rowList3(Long.class, String.class, String.class)
                    .append(12345L, AccessTypeEnums.CHECKIN.uuid.toString() , "ACCESTYPENAME")
                    .asResult())
        .build();

        final AccessType accessTypeById = AccessType.getAccessType(12345L);

        final AccessType accessTypeByIdCached = AccessType.getAccessType(12345L);
        final AccessType accessTypeByName = AccessType.getAccessType("ACCESTYPENAME");
        final AccessType accessTypeByUUID = AccessType.getAccessType(AccessTypeEnums.CHECKIN.uuid);

        assertEquals(accessTypeById, accessTypeByIdCached);
        assertEquals(accessTypeById, accessTypeByName);
        assertEquals(accessTypeById, accessTypeByUUID);
    }

}

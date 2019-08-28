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
package org.efaps.util;

import static org.testng.Assert.assertEquals;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.test.AbstractTest;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DateTimeUtilTest
    extends AbstractTest
{

    @Test(description = "Test Converter", dataProvider = "DataProvider")
    public void testValues(final Object _value,
                           final OffsetDateTime _expected)
        throws EFapsException
    {
        final OffsetDateTime datetime = DateTimeUtil.toDateTime(_value);
        assertEquals(datetime, _expected);
    }

    @DataProvider(name = "DataProvider")
    public static Iterator<Object[]> dataProvider(final ITestContext _context)
    {
        final List<Object[]> ret = new ArrayList<>();
        final Instant instant = Instant.ofEpochMilli(1234567896L);
        ret.add(new Object[] { Date.from(instant), OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()) });
        ret.add(new Object[] { "2019-08-12T10:15:30Z", OffsetDateTime.of(2019, 8, 12, 10, 15, 30, 0,
                        ZoneId.of("Z").getRules().getOffset(Instant.now())) });
        ret.add(new Object[] { "2019-08-12T10:15:30-05", OffsetDateTime.of(2019, 8, 12, 10, 15, 30, 0,
                        ZoneOffset.ofHours(-5)) });
        ret.add(new Object[] { "2019-08-12T10:15:30+08", OffsetDateTime.of(2019, 8, 12, 10, 15, 30, 0,
                        ZoneOffset.ofHours(8)) });
        ret.add(new Object[] { "2019-08-12T10:15:30+04:15", OffsetDateTime.of(2019, 8, 12, 10, 15, 30, 0,
                        ZoneOffset.ofHoursMinutes(4, 15)) });
        ret.add(new Object[] { "2019-08-12T10:15:30.12358-05", OffsetDateTime.of(2019, 8, 12, 10, 15, 30, 123580000,
                        ZoneOffset.ofHours(-5)) });
        ret.add(new Object[] { LocalDate.of(2019, 8, 23), OffsetDateTime.of(2019, 8, 23, 0, 0, 0, 0,
                        ZoneId.systemDefault().getRules().getOffset(LocalDateTime.of(2019, 8, 23, 0, 0))) });
        ret.add(new Object[] { LocalDateTime.of(2019, 8, 23, 16, 30, 15), OffsetDateTime.of(2019, 8, 23, 16, 30, 15, 0,
                        ZoneId.systemDefault().getRules().getOffset(LocalDateTime.of(2019, 8, 23, 0, 0))) });

        return ret.iterator();
    }
}

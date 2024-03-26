/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.eql.builder;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.db.Instance;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConverterTest
    extends AbstractTest
{
    @Test(description = "Test Converter", dataProvider = "DataProvider")
    public void convert(final Object _value,
                        final String _convertedValue)
        throws EFapsException
    {
        assertEquals(Converter.convert(_value), _convertedValue);
    }

    @DataProvider(name = "DataProvider")
    public static Iterator<Object[]> dataProvider(final ITestContext _context)
    {
        final List<Object[]> ret = new ArrayList<>();
        ret.add(new Object[] { "just a value", "just a value" });
        ret.add(new Object[] { Instance.get(Mocks.AllAttrType.getUuid(), 4), Mocks.AllAttrType.getId() + ".4" });
        ret.add(new Object[] { 25635345L, "25635345" });
        ret.add(new Object[] { 3, "3" });
        ret.add(new Object[] { LocalDate.of(2018, 8, 22), "2018-08-22" });
        ret.add(new Object[] { new BigDecimal("12.986"), "12.986" });
        final Object unknown = new Object();
        ret.add(new Object[] { unknown, unknown.toString() });
        return ret.iterator();
    }
}

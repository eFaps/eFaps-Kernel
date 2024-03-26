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
package org.efaps.db;

import java.time.LocalDate;

import org.efaps.mock.datamodel.CI;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

public class InsertTest
    extends AbstractTest
{

    @Test
    public void testLocalDateInsert()
        throws EFapsException
    {
        final Insert insert = new Insert(CI.AllAttrType);
        insert.add(CI.AllAttrType.DateAttribute, LocalDate.of(2019,8,8));
        insert.execute();
    }

}

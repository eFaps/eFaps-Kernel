/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.mock.db;

import org.efaps.db.databases.information.ColumnInformation;
import org.efaps.db.databases.information.TableInformation;

/**
 * The Class MockTableInformation.
 */
public class MockTableInformation extends TableInformation
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new mock table information.
     *
     * @param _tableName the table name
     */
    public MockTableInformation(final String _tableName)
    {
        super(_tableName);
    }

    @Override
    public ColumnInformation getColInfo(final String _columnName)
    {
        return new MockColumnInformation(_columnName);
    }
}

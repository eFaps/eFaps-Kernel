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

import java.util.Collections;

import org.efaps.db.databases.AbstractDatabase.ColumnType;
import org.efaps.db.databases.information.ColumnInformation;

/**
 * The Class MockColumnInformation.
 */
public class MockColumnInformation
    extends ColumnInformation
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new mock column information.
     *
     * @param _name the name
     */
    protected MockColumnInformation(final String _name)
    {
        super(_name, Collections.singleton(ColumnType.STRING_SHORT), 500, 0, true);
    }
}

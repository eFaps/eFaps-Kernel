/*
 * Copyright 2003 - 2010 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.test;

import org.efaps.db.Context;
import org.efaps.init.StartupDatabaseConnection;
import org.efaps.init.StartupException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractTest
{
    /**
     * Connects to the database.
     *
     * @param _bootstrap    name of the bootstrap
     * @throws StartupException if connect to the eFaps database failed
     */
    @BeforeTest(description = "connects to the eFaps database",
                 groups = "connect")
    @Parameters("bootstrap")
    public void connect(@Optional("postgresql") final String _bootstrap)
        throws StartupException
    {
        System.out.println("connect to " + _bootstrap);
        StartupDatabaseConnection.startup(_bootstrap);
        System.out.println("... using class "+Context.getDbType().getClass());
    }

    /**
     * Connects from the database.
     *
     * @throws StartupException if disconnect from the database failed
     */
    @AfterTest(dependsOnGroups = "cleanup")
    public void disconnect()
        throws StartupException
    {
        StartupDatabaseConnection.shutdown();
    }
}

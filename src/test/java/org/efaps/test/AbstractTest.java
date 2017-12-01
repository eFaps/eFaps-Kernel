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

package org.efaps.test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.zaxxer.hikari.HikariJNDIFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.efaps.db.Context;
import org.efaps.init.StartupDatabaseConnection;
import org.efaps.init.StartupException;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.mock.datamodel.Attribute;
import org.efaps.mock.datamodel.AttributeType;
import org.efaps.mock.datamodel.Person;
import org.efaps.mock.datamodel.SQLTable;
import org.efaps.mock.datamodel.Type;
import org.efaps.mock.db.MockDatabase;
import org.efaps.util.EFapsException;
import org.jgroups.util.UUID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import acolyte.jdbc.CompositeHandler;
import acolyte.jdbc.StatementHandler;

/**
 * The Class AbstractTest.
 *
 * @author The eFaps Team
 */
public abstract class AbstractTest
{

    /** The Constant JDBCURL. */
    public static final String JDBCURL = "jdbc:acolyte:anything-you-want?handler=my-handler-id";

    /**
     * Prepare JNDI.
     *
     * @throws StartupException the startup exception
     */
    @BeforeSuite
    public void prepareJNDI()
        throws StartupException
    {
        Person.builder()
            .withId(1L)
            .withName("Administrator")
            .build();

        final Type type = Type.builder()
            .withId(Long.valueOf(123))
            .withName("DemoType")
            .build();

        final SQLTable sqlTable = SQLTable.builder()
            .withName("DemoTypeSQLTable")
            .build();

        final AttributeType stringAttrType = AttributeType.builder()
            .withName("String")
            .withUuid(UUID.fromString("72221a59-df5d-4c56-9bec-c9167de80f2b"))
            .withClassName("org.efaps.admin.datamodel.attributetype.StringType")
            .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
            .build();

        Attribute.builder()
            .withName("TestAttribute")
            .withDataModelTypeId(type.getId())
            .withSqlTableId(sqlTable.getId())
            .withAttributeTypeId(stringAttrType.getId())
            .build();

        final StatementHandler handler = new CompositeHandler().withQueryDetection("^ select ")
                        .withQueryHandler(EFapsQueryHandler.get());
        acolyte.jdbc.Driver.register("my-handler-id", handler);

        final Map<String, String> connectionProperties = new HashMap<>();
        connectionProperties.put("jdbcUrl", JDBCURL);
        final Map<String, String> eFapsProperties = new HashMap<>();
        StartupDatabaseConnection.startup(MockDatabase.class.getName(), HikariJNDIFactory.class.getName(),
                        connectionProperties, TransactionManagerImple.class.getName(),
                        TransactionSynchronizationRegistryImple.class.getName(), eFapsProperties);

        AppAccessHandler.init(null, Collections.emptySet());
    }

    /**
     * Open context.
     *
     * @throws EFapsException the eFaps exception
     */
    @BeforeMethod
    public void openContext()
        throws EFapsException
    {
        Context.begin("Administrator");
    }
}

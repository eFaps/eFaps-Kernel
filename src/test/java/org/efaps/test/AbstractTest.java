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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.db.Context;
import org.efaps.init.StartupDatabaseConnection;
import org.efaps.init.StartupException;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.IDataModel;
import org.efaps.mock.datamodel.Person;
import org.efaps.mock.db.MockDatabase;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    /**
     * Prepare the Test Suite.
     *
     * @throws StartupException the startup exception
     */
    @BeforeSuite
    public void prepareSuite()
        throws StartupException
    {
        Person.builder()
            .withId(1L)
            .withName("Administrator")
            .build();

        Field[] fields = IDataModel.class.getDeclaredFields();
        fields = ArrayUtils.addAll(fields, Mocks.class.getDeclaredFields());
        for (final Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    f.get(null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOG.error("Catched", e);
                }
            }
        }

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

    /**
     * Close context.
     *
     * @throws EFapsException the eFaps exception
     */
    @AfterMethod
    public void closeContext()
        throws EFapsException
    {
        Context.commit();
        EFapsQueryHandler.get().cleanUp();
    }
}

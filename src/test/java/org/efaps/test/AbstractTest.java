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
import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;
import org.efaps.ci.CIAdminDataModel;
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

    /** The Constant DemoType. */
    public static final Type SimpleType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("SimpleType")
                    .build();

    public static final SQLTable SimpleTypeSQLTable = SQLTable.builder()
                    .withName("SimpleTypeSQLTable")
                    .build();

    /** The Constant DemoType. */
    public static final Type TypedType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("TypedType")
                    .build();

    public static final SQLTable TypedTypeSQLTable = SQLTable.builder()
                    .withName("TypedTypeSQLTable")
                    .withTypeColumn("TYPE")
                    .build();

    public static final Type TYPE_AttributeSet = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.AttributeSet.uuid)
                    .withName("AttributeSet")
                    .build();

    public static final Type TYPE_Attribute = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.Attribute.uuid)
                    .withName("Attribute")
                    .build();

    public static final AttributeType StringAttrType = AttributeType.builder()
                    .withName("String")
                    .withUuid(UUID.fromString("72221a59-df5d-4c56-9bec-c9167de80f2b"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.StringType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    public static final AttributeType TypeAttrType = AttributeType.builder()
                    .withName("String")
                    .withUuid(UUID.fromString("acfb7dd8-71e9-43c0-9f22-8d98190f7290"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.TypeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.TypeUI")
                    .build();

    public static final Attribute TestAttribute = Attribute.builder()
                    .withName("TestAttribute")
                    .withDataModelTypeId(SimpleType.getId())
                    .withSqlTableId(SimpleTypeSQLTable.getId())
                    .withAttributeTypeId(StringAttrType.getId())
                    .build();

    public static final Attribute TypedTypeTestAttr = Attribute.builder()
                    .withName("TestAttr")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(StringAttrType.getId())
                    .build();

    public static final Attribute TypedTypeTypeAttr = Attribute.builder()
                    .withName("TypeAttr")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(TypeAttrType.getId())
                    .build();

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
    }
}

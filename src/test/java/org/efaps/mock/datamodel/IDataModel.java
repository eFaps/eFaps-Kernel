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

package org.efaps.mock.datamodel;

import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.ci.CIAdminEvent;
import org.efaps.ci.CIAdminProgram;

/**
 * The Interface IDataModel.
 */
public interface IDataModel
{

    AttributeType StringType = AttributeType.builder()
                    .withName("String")
                    .withUuid(UUID.fromString("72221a59-df5d-4c56-9bec-c9167de80f2b"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.StringType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    AttributeType LongType = AttributeType.builder()
                    .withName("Long")
                    .withUuid(UUID.fromString("b9d0e298-f96b-4b78-aa6c-ae8c71952f6c"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.LongType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.NumberUI")
                    .build();

    AttributeType IntegerType = AttributeType.builder()
                    .withName("Integer")
                    .withUuid(UUID.fromString("41451b64-cb24-4e77-8d9e-5b6eb58df56f"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.IntegerType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.NumberUI")
                    .build();

    AttributeType DecimalType = AttributeType.builder()
                    .withName("Decimal")
                    .withUuid(UUID.fromString("58d1f0e-43ae-425d-a4a0-8d5bad6f40d7"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.DecimalType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.NumberUI")
                    .build();

    AttributeType BooleanType = AttributeType.builder()
                    .withName("Boolean")
                    .withUuid(UUID.fromString("7fb3799d-4e31-45a3-8c5e-4fbf445ec3c1"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.BooleanType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.BooleanUI")
                    .build();

    AttributeType DateType = AttributeType.builder()
                    .withName("Date")
                    .withUuid(UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.DateType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateUI")
                    .build();

    AttributeType TimeType = AttributeType.builder()
                    .withName("Time")
                    .withUuid(UUID.fromString("d8ddc848-115e-4abf-be66-0856ac64b21a"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.TimeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    AttributeType DateTimeType = AttributeType.builder()
                    .withName("DateTime")
                    .withUuid(UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.DateTimeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateTimeUI")
                    .build();

    AttributeType CreatedType = AttributeType.builder()
                    .withName("Created")
                    .withUuid(UUID.fromString("513d35f5-58e2-4243-acd2-5fec5359778a"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.CreatedType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateTimeUI")
                    .build();

    AttributeType ModifiedType = AttributeType.builder()
                    .withName("Modified")
                    .withUuid(UUID.fromString("a8556408-a15d-4f4f-b740-6824f774dc1d"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.ModifiedType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateTimeUI")
                    .build();

    AttributeType TypeType = AttributeType.builder()
                    .withName("Type")
                    .withUuid(UUID.fromString("acfb7dd8-71e9-43c0-9f22-8d98190f7290"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.TypeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.TypeUI")
                    .build();

    AttributeType LinkType = AttributeType.builder()
                    .withName("Link")
                    .withUuid(UUID.fromString("440f472f-7be2-41d3-baec-4a2f0e4e5b31"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.LinkType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    AttributeType CompanyLinkType = AttributeType.builder()
                    .withName("CompanyLink")
                    .withUuid(UUID.fromString("66c5d239-47d7-4fef-a79b-9dac432ab7ba"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.CompanyLinkType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.UserUI")
                    .withCreateUpdate(true)
                    .build();

    AttributeType StatusType = AttributeType.builder()
                    .withName("StatusType")
                    .withUuid(UUID.fromString("0161bcdb-45e9-4839-a709-3a1c56f8a76a"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.StatusType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.LinkWithRangesUI")
                    .build();

    Type Admin_Abstract = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdmin.Abstract.uuid)
                    .withName("Admin_Abstract")
                    .build();

    SQLTable Admin_DataModel_SQLTable = SQLTable.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(UUID.fromString("5ffb40ef-3518-46c8-a78f-da3ffbfea4c0"))
                    .withName("Admin_DataModel_SQLTable")
                    .withSqlTableName("T_DMTABLE")
                    .withTypeColumn("TYPEID")
                    .build();

    Attribute TypeAttr = Attribute.builder()
                    .withName("Type")
                    .withDataModelTypeId(Admin_Abstract.getId())
                    .withSqlTableId(Admin_DataModel_SQLTable.getId())
                    .withAttributeTypeId(IDataModel.TypeType.getId())
                    .build();

    Attribute NameAttr = Attribute.builder()
                    .withName("Name")
                    .withDataModelTypeId(Admin_Abstract.getId())
                    .withSqlTableId(Admin_DataModel_SQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute UUIDAttr = Attribute.builder()
                    .withName("UUID")
                    .withDataModelTypeId(Admin_Abstract.getId())
                    .withSqlTableId(Admin_DataModel_SQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Type Admin_DataModel_Abstract = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.Abstract.uuid)
                    .withName("Admin_DataModel_Abstract")
                    .withParentTypeId(Admin_Abstract.getId())
                    .build();

    Type Admin_DataModel_AttributeSet = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.AttributeSet.uuid)
                    .withName("Admin_DataModel_AttributeSet")
                    .build();

    Type Admin_DataModel_Attribute = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.Attribute.uuid)
                    .withName("Admin_DataModel_Attribute")
                    .build();

    Type Admin_Program_Abstract = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_Program_Abstract")
                    .withUuid(CIAdminProgram.Abstract.uuid)
                    .withParentTypeId(Admin_Abstract.getId())
                    .build();

    Type Admin_Program_Java = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_Program_Java")
                    .withUuid(CIAdminProgram.Java.uuid)
                    .withParentTypeId(Admin_Program_Abstract.getId())
                    .build();

    Type Admin_Event_Definition = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_Event_Definition")
                    .withUuid(CIAdminEvent.Definition.uuid)
                    .withParentTypeId(Admin_DataModel_Abstract.getId())
                    .build();

    SQLTable Admin_Event_DefinitionSQLTable = SQLTable.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(UUID.fromString("5ffb40ef-3518-46c8-a78f-da3ffbfea4c0"))
                    .withName("Admin_Event_DefinitionSQLTable")
                    .withSqlTableName("T_EVENTDEF")
                    .build();

    Attribute AbstractAttr = Attribute.builder()
                    .withName(CIAdminEvent.Definition.Abstract.name)
                    .withDataModelTypeId(Admin_Event_Definition.getId())
                    .withSqlTableId(Admin_Event_DefinitionSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .build();

    Attribute JavaProgAttr = Attribute.builder()
                    .withName(CIAdminEvent.Definition.JavaProg.name)
                    .withDataModelTypeId(Admin_Event_Definition.getId())
                    .withSqlTableId(Admin_Event_DefinitionSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(Admin_Program_Java.getId())
                    .build();

    Attribute IndexPosition = Attribute.builder()
                    .withName(CIAdminEvent.Definition.IndexPosition.name)
                    .withDataModelTypeId(Admin_Event_Definition.getId())
                    .withSqlTableId(Admin_Event_DefinitionSQLTable.getId())
                    .withAttributeTypeId(IDataModel.IntegerType.getId())
                    .build();

    Attribute Method = Attribute.builder()
                    .withName(CIAdminEvent.Definition.Method.name)
                    .withDataModelTypeId(Admin_Event_Definition.getId())
                    .withSqlTableId(Admin_Event_DefinitionSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Type Admin_DataModel_TypeAccessCheckEvent = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_DataModel_TypeAccessCheckEvent")
                    .withUuid(UUID.fromString("03f0ab61-aaf6-4566-a6a9-d86b343acd66"))
                    .withParentTypeId(Admin_Event_Definition.getId())
                    .build();

    Type Admin_DataModel_Type_Trigger_DeletePre = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_DataModel_Type_Trigger_DeletePre")
                    .withUuid(UUID.fromString("7d64b1b6-c7d6-40a8-8abc-0a6a04d1b7b7"))
                    .withParentTypeId(Admin_Event_Definition.getId())
                    .build();

    Type Admin_DataModel_Type_Trigger_DeletePost = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_DataModel_Type_Trigger_DeletePost")
                    .withUuid(UUID.fromString("590af1fc-b058-49fd-a0bc-f56950577af8"))
                    .withParentTypeId(Admin_Event_Definition.getId())
                    .build();

    Type Admin_DataModel_Type_Trigger_DeleteOverride = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_DataModel_Type_Trigger_DeleteOverride")
                    .withUuid(UUID.fromString("3be4ca23-671c-4700-bf21-a3bec7bf4830"))
                    .withParentTypeId(Admin_Event_Definition.getId())
                    .build();

    Type Admin_Common_Property = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("Admin_Common_Property")
                    .withUuid(CIAdminCommon.Property.uuid)
                    .build();

    SQLTable Admin_Common_PropertySQLTable = SQLTable.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(UUID.fromString("5cf99cd6-06d6-4322-a344-55d206666c9c"))
                    .withName("Admin_Common_PropertySQLTable")
                    .withSqlTableName("T_CMPROPERTY")
                    .build();

    Attribute Admin_Common_Property_AbstractAttr = Attribute.builder()
                    .withName(CIAdminCommon.Property.Abstract.name)
                    .withDataModelTypeId(Admin_Common_Property.getId())
                    .withSqlTableId(Admin_Common_PropertySQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(Admin_Program_Java.getId())
                    .build();

    Attribute Admin_Common_Property_NameAttr = Attribute.builder()
                    .withName(CIAdminCommon.Property.Name.name)
                    .withDataModelTypeId(Admin_Common_Property.getId())
                    .withSqlTableId(Admin_Common_PropertySQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute Admin_Common_Property_ValueAttr = Attribute.builder()
                    .withName(CIAdminCommon.Property.Value.name)
                    .withDataModelTypeId(Admin_Common_Property.getId())
                    .withSqlTableId(Admin_Common_PropertySQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();
}

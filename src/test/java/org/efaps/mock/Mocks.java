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

package org.efaps.mock;

import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.mock.datamodel.Attribute;
import org.efaps.mock.datamodel.AttributeType;
import org.efaps.mock.datamodel.SQLTable;
import org.efaps.mock.datamodel.Type;

/**
 * The Interface ITypes.
 */
public interface Mocks
{

    Type TYPE_AttributeSet = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.AttributeSet.uuid)
                    .withName("AttributeSet")
                    .build();

    Type TYPE_Attribute = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withUuid(CIAdminDataModel.Attribute.uuid)
                    .withName("Attribute")
                    .build();

    AttributeType StringAttrType = AttributeType.builder()
                    .withName("String")
                    .withUuid(UUID.fromString("72221a59-df5d-4c56-9bec-c9167de80f2b"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.StringType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    AttributeType LongAttrType = AttributeType.builder()
                    .withName("Long")
                    .withUuid(UUID.fromString("b9d0e298-f96b-4b78-aa6c-ae8c71952f6c"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.LongType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.NumberUI")
                    .build();

    AttributeType IntegerAttrType = AttributeType.builder()
                    .withName("Integer")
                    .withUuid(UUID.fromString("41451b64-cb24-4e77-8d9e-5b6eb58df56f"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.IntegerType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.NumberUI")
                    .build();

    AttributeType BooleanAttrType = AttributeType.builder()
                    .withName("Boolean")
                    .withUuid(UUID.fromString("7fb3799d-4e31-45a3-8c5e-4fbf445ec3c1"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.BooleanType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.BooleanUI")
                    .build();

    AttributeType DateAttrType = AttributeType.builder()
                    .withName("Date")
                    .withUuid(UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.DateType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateUI")
                    .build();

    AttributeType TimeAttrType = AttributeType.builder()
                    .withName("Time")
                    .withUuid(UUID.fromString("d8ddc848-115e-4abf-be66-0856ac64b21a"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.TimeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    AttributeType DateTimeAttrType = AttributeType.builder()
                    .withName("DateTime")
                    .withUuid(UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.DateTimeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateTimeUI")
                    .build();

    AttributeType CreatedAttrType = AttributeType.builder()
                    .withName("Created")
                    .withUuid(UUID.fromString("513d35f5-58e2-4243-acd2-5fec5359778a"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.CreatedType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateTimeUI")
                    .build();

    AttributeType ModifiedAttrType = AttributeType.builder()
                    .withName("Modified")
                    .withUuid(UUID.fromString("a8556408-a15d-4f4f-b740-6824f774dc1d"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.ModifiedType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.DateTimeUI")
                    .build();

    AttributeType TypeAttrType = AttributeType.builder()
                    .withName("Type")
                    .withUuid(UUID.fromString("acfb7dd8-71e9-43c0-9f22-8d98190f7290"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.TypeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.TypeUI")
                    .build();

    AttributeType LinkAttrType = AttributeType.builder()
                    .withName("Link")
                    .withUuid(UUID.fromString("440f472f-7be2-41d3-baec-4a2f0e4e5b31"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.LinkType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.StringUI")
                    .build();

    Type TypedType = Type.builder().withId(RandomUtils.nextLong()).withName("TypedType").build();

    SQLTable TypedTypeSQLTable = SQLTable.builder().withName("TypedTypeSQLTable").withTypeColumn("TYPE").build();

    Attribute TypedTypeTestAttr = Attribute.builder()
                    .withName("TestAttr")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(StringAttrType.getId())
                    .build();

    Attribute TypedTypeTypeAttr = Attribute.builder()
                    .withName("TypeAttr")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(TypeAttrType.getId())
                    .build();

    Type SimpleType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("SimpleType")
                    .build();

    SQLTable SimpleTypeSQLTable = SQLTable.builder()
                    .withName("SimpleTypeSQLTable")
                    .build();

    Attribute IDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(SimpleType.getId())
                    .withSqlTableId(SimpleTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.LongAttrType.getId())
                    .build();

    Attribute TestAttribute = Attribute.builder()
                    .withName("TestAttribute")
                    .withDataModelTypeId(SimpleType.getId())
                    .withSqlTableId(SimpleTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.StringAttrType.getId())
                    .build();

    Type AllAttrType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("AllAttrType")
                    .build();

    SQLTable AllAttrTypeSQLTable = SQLTable.builder()
                    .withName("AllAttrTypeSQLTable")
                    .withSqlTableName("JUSTANAME")
                    .build();

    Attribute AllAttrStringAttribute = Attribute.builder()
                    .withName("AllAttrStringAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.StringAttrType.getId())
                    .build();

    Attribute AllAttrLongAttribute = Attribute.builder()
                    .withName("AllAttrLongAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.LongAttrType.getId())
                    .build();

    Attribute AllAttrIntegerAttribute = Attribute.builder()
                    .withName("AllAttrIntegerAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.IntegerAttrType.getId())
                    .build();

    Attribute AllAttrBooleanAttribute = Attribute.builder()
                    .withName("AllAttrBooleanAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.BooleanAttrType.getId())
                    .build();

    Attribute AllAttrDateAttribute = Attribute.builder()
                    .withName("AllAttrDateAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.DateAttrType.getId())
                    .build();

    Attribute AllAttrTimeAttribute = Attribute.builder()
                    .withName("AllAttrDateAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.TimeAttrType.getId())
                    .build();

    Attribute AllAttrDateTimeAttribute = Attribute.builder()
                    .withName("AllAttrDateTimeAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.DateTimeAttrType.getId())
                    .build();

    Attribute AllAttrCreatedAttribute = Attribute.builder()
                    .withName("AllAttrCreatedAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.CreatedAttrType.getId())
                    .build();

    Attribute AllAttrModifiedAttribute = Attribute.builder()
                    .withName("AllAttrModifiedAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.ModifiedAttrType.getId())
                    .build();

    Attribute AllAttrLinkAttribute = Attribute.builder()
                    .withName("AllAttrLinkAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.LinkAttrType.getId())
                    .withLinkTypeId(SimpleType.getId())
                    .build();
}

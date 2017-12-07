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

    AttributeType TypeAttrType = AttributeType.builder()
                    .withName("String")
                    .withUuid(UUID.fromString("acfb7dd8-71e9-43c0-9f22-8d98190f7290"))
                    .withClassName("org.efaps.admin.datamodel.attributetype.TypeType")
                    .withClassNameUI("org.efaps.admin.datamodel.ui.TypeUI")
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

    Attribute TestAttribute = Attribute.builder()
                    .withName("TestAttribute")
                    .withDataModelTypeId(SimpleType.getId())
                    .withSqlTableId(SimpleTypeSQLTable.getId())
                    .withAttributeTypeId(Mocks.StringAttrType.getId())
                    .build();
}

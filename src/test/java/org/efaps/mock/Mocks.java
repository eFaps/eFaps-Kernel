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

import org.apache.commons.lang3.RandomUtils;
import org.efaps.admin.datamodel.Type.Purpose;
import org.efaps.mock.datamodel.Attribute;
import org.efaps.mock.datamodel.EventDefinition;
import org.efaps.mock.datamodel.IDataModel;
import org.efaps.mock.datamodel.SQLTable;
import org.efaps.mock.datamodel.Type;
import org.efaps.mock.esjp.AccessCheck;

/**
 * The Interface ITypes.
 */
public interface Mocks
{

    Type TypedType = Type.builder().withId(RandomUtils.nextLong()).withName("TypedType").build();

    SQLTable TypedTypeSQLTable = SQLTable.builder().withName("TypedTypeSQLTable").withTypeColumn("TYPE").build();

    Attribute TypedTypeTestAttr = Attribute.builder()
                    .withName("TestAttr")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute TypedTypeTypeAttr = Attribute.builder()
                    .withName("TypeAttr")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.TypeType.getId())
                    .build();

    Attribute TypedTypeIDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(TypedType.getId())
                    .withSqlTableId(TypedTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Type TypedType2 = Type.builder().withId(RandomUtils.nextLong()).withName("TypedType2").build();

    SQLTable TypedType2SQLTable = SQLTable.builder().withName("TypedType2SQLTable").withTypeColumn("TYPE").build();

    Attribute TypedType2TestAttr = Attribute.builder()
                    .withName("TestAttr")
                    .withDataModelTypeId(TypedType2.getId())
                    .withSqlTableId(TypedType2SQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute TypedType2TypeAttr = Attribute.builder()
                    .withName("TypeAttr")
                    .withDataModelTypeId(TypedType2.getId())
                    .withSqlTableId(TypedType2SQLTable.getId())
                    .withAttributeTypeId(IDataModel.TypeType.getId())
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
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute TestAttribute = Attribute.builder()
                    .withName("TestAttribute")
                    .withDataModelTypeId(SimpleType.getId())
                    .withSqlTableId(SimpleTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
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
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute AllAttrLongAttribute = Attribute.builder()
                    .withName("AllAttrLongAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute AllAttrIntegerAttribute = Attribute.builder()
                    .withName("AllAttrIntegerAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.IntegerType.getId())
                    .build();

    Attribute AllAttrBooleanAttribute = Attribute.builder()
                    .withName("AllAttrBooleanAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.BooleanType.getId())
                    .build();

    Attribute AllAttrDateAttribute = Attribute.builder()
                    .withName("AllAttrDateAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.DateType.getId())
                    .build();

    Attribute AllAttrTimeAttribute = Attribute.builder()
                    .withName("AllAttrTimeAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.TimeType.getId())
                    .build();

    Attribute AllAttrDateTimeAttribute = Attribute.builder()
                    .withName("AllAttrDateTimeAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.DateTimeType.getId())
                    .build();

    Attribute AllAttrCreatedAttribute = Attribute.builder()
                    .withName("AllAttrCreatedAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.CreatedType.getId())
                    .build();

    Attribute AllAttrModifiedAttribute = Attribute.builder()
                    .withName("AllAttrModifiedAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.ModifiedType.getId())
                    .build();

    Attribute AllAttrLinkAttribute = Attribute.builder()
                    .withName("AllAttrLinkAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(SimpleType.getId())
                    .build();

    Attribute AllAttrLinkAttributeTyped = Attribute.builder()
                    .withName("AllAttrLinkAttributeTyped")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(TypedType.getId())
                    .build();


    Type AbstractType = Type.builder().withId(RandomUtils.nextLong())
                    .withName("AbstractType")
                    .withPurposeId(Purpose.ABSTRACT.getInt())
                    .build();

    SQLTable AbstractTypeSQLTable = SQLTable.builder().withName("AbstractTypeSQLTable")
                    .withTypeColumn("TYPE")
                    .build();

    Attribute AbstractTypeTypeAttr = Attribute.builder()
                    .withName("TypeAttr")
                    .withDataModelTypeId(AbstractType.getId())
                    .withSqlTableId(AbstractTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.TypeType.getId())
                    .build();

    Attribute AbstractTypeStringAttribute = Attribute.builder()
                    .withName("AbstractTypeStringAttribute")
                    .withDataModelTypeId(AbstractType.getId())
                    .withSqlTableId(AbstractTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute AbstractTypeIDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(AbstractType.getId())
                    .withSqlTableId(AbstractTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Type ChildType1 = Type.builder().withId(RandomUtils.nextLong())
                    .withName("ChildType1")
                    .withParentTypeId(AbstractType.getId())
                    .build();

    Type ChildType2 = Type.builder().withId(RandomUtils.nextLong())
                    .withName("ChildType2")
                    .withParentTypeId(AbstractType.getId())
                    .build();

    Type AccessType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("AccessType")
                    .build();

    SQLTable AccessTypeSQLTable = SQLTable.builder()
                    .withName("AccessTypeSQLTable")
                    .build();

    Attribute AccessTypeStringAttribute = Attribute.builder()
                    .withName("StringAttribute")
                    .withDataModelTypeId(AccessType.getId())
                    .withSqlTableId(AccessTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition AccessTypeEvent = EventDefinition.builder()
                    .withObjectLink(AccessType.getId())
                    .withInstId(1L)
                    .withTypeId(IDataModel.Admin_DataModel_TypeAccessCheckEvent.getId())
                    .withESJP(AccessCheck.class.getName())
                    .withMethod("execute")
                    .build();

}

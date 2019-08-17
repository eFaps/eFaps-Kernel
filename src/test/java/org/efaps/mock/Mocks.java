/*
 * Copyright 2003 - 2018 The eFaps Team
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
import org.efaps.mock.datamodel.Property;
import org.efaps.mock.datamodel.SQLTable;
import org.efaps.mock.datamodel.StatusGroup;
import org.efaps.mock.datamodel.Type;
import org.efaps.mock.esjp.AccessCheck;
import org.efaps.mock.esjp.TriggerEvent;

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

    SQLTable SimpleTypeChildSQLTable = SQLTable.builder()
                    .withName("SimpleTypeChildSQLTable")
                    .withSqlTableName("SIMPTYPESQLCHILDTABLE")
                    .withMainTableId(SimpleTypeSQLTable.getId())
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

    Attribute SimpleTypeInChildSQLAttribute = Attribute.builder()
                    .withName("SimpleTypeInChildSQLAttribute")
                    .withDataModelTypeId(SimpleType.getId())
                    .withSqlTableId(SimpleTypeChildSQLTable.getId())
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

    SQLTable AllAttrTypeChildSQLTable = SQLTable.builder()
                    .withName("AllAttrTypeChildSQLTable")
                    .withSqlTableName("AATYPESQLCHILDTABLE")
                    .withMainTableId(AllAttrTypeSQLTable.getId())
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

    Attribute AllAttrDecimalAttribute = Attribute.builder()
                    .withName("AllAttrDecimalAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.DecimalType.getId())
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

    Attribute AllAttrInChildSQLAttribute = Attribute.builder()
                    .withName("AllAttrInChildSQLAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeChildSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Attribute AllAttrLinkInChildSQLAttribute = Attribute.builder()
                    .withName("AllAttrLinkInChildSQLAttribute")
                    .withDataModelTypeId(AllAttrType.getId())
                    .withSqlTableId(AllAttrTypeChildSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(SimpleType.getId())
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

    Type AccessType2 = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("AccessType2")
                    .build();

    SQLTable AccessType2SQLTable = SQLTable.builder()
                    .withName("AccessType2SQLTable")
                    .withSqlTableName("T_ACCESSTYPE2")
                    .build();

    Attribute AccessType2IDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(AccessType2.getId())
                    .withSqlTableId(AccessType2SQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute AccessType2StringAttribute = Attribute.builder()
                    .withName("StringAttribute")
                    .withDataModelTypeId(AccessType2.getId())
                    .withSqlTableId(AccessType2SQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition AccessType2Event = EventDefinition.builder()
                    .withObjectLink(AccessType2.getId())
                    .withInstId(1L)
                    .withTypeId(IDataModel.Admin_DataModel_TypeAccessCheckEvent.getId())
                    .withESJP(AccessCheck.class.getName())
                    .withMethod("execute")
                    .build();

    Attribute AccessTypeLinkAttribute = Attribute.builder()
                    .withName("AccessTypeLink")
                    .withDataModelTypeId(AccessType.getId())
                    .withSqlTableId(AccessTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(AccessType2.getId())
                    .build();

    Type AccessType3 = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("AccessType3")
                    .build();

    SQLTable AccessType3SQLTable = SQLTable.builder()
                    .withName("AccessType3SQLTable")
                    .withSqlTableName("T_ACCESSTYPE3")
                    .build();

    Attribute AccessType3IDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(AccessType3.getId())
                    .withSqlTableId(AccessType3SQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute AccessType3StringAttribute = Attribute.builder()
                    .withName("StringAttribute")
                    .withDataModelTypeId(AccessType3.getId())
                    .withSqlTableId(AccessType3SQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition AccessType3Event = EventDefinition.builder()
                    .withObjectLink(AccessType3.getId())
                    .withInstId(1L)
                    .withTypeId(IDataModel.Admin_DataModel_TypeAccessCheckEvent.getId())
                    .withESJP(AccessCheck.class.getName())
                    .withMethod("execute")
                    .build();

    Attribute AccessType2LinkAttribute = Attribute.builder()
                    .withName("AccessType2Link")
                    .withDataModelTypeId(AccessType2.getId())
                    .withSqlTableId(AccessType2SQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(AccessType3.getId())
                    .build();

    Type RelationType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("RelationType")
                    .build();

    SQLTable RelationTypeSQLTable = SQLTable.builder()
                    .withName("RelationTypeSQLTable")
                    .withSqlTableName("RELTABLE")
                    .build();

    Attribute RealtionFromLinkAttribute = Attribute.builder()
                    .withName("FromLink")
                    .withDataModelTypeId(RelationType.getId())
                    .withSqlTableId(RelationTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(SimpleType.getId())
                    .build();

    Attribute RealtionToLinkAttribute = Attribute.builder()
                    .withName("ToLink")
                    .withDataModelTypeId(RelationType.getId())
                    .withSqlTableId(RelationTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(TypedType.getId())
                    .build();

    Attribute RealtionStringAttribute = Attribute.builder()
                    .withName("Value")
                    .withDataModelTypeId(RelationType.getId())
                    .withSqlTableId(RelationTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Type RelationAccessType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("RelationAccessType")
                    .build();

    SQLTable RelationAccessTypeSQLTable = SQLTable.builder()
                    .withName("RelationAccessTypeSQLTable")
                    .withSqlTableName("RELACCTABLE")
                    .build();

    Attribute RelationAccessTypeIDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(RelationAccessType.getId())
                    .withSqlTableId(RelationAccessTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute RealtionAccessFromLinkAttribute = Attribute.builder()
                    .withName("FromLink")
                    .withDataModelTypeId(RelationAccessType.getId())
                    .withSqlTableId(RelationAccessTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LinkType.getId())
                    .withLinkTypeId(AccessType.getId())
                    .build();

    EventDefinition RealtionAccessTypeEvent = EventDefinition.builder()
                    .withObjectLink(RelationAccessType.getId())
                    .withInstId(2L)
                    .withTypeId(IDataModel.Admin_DataModel_TypeAccessCheckEvent.getId())
                    .withESJP(AccessCheck.class.getName())
                    .withMethod("execute")
                    .build();

    Type ClassType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withPurposeId(2)
                    .withName("ClassType")
                    .build();

    Property classProperty = Property.builder()
                    .withName("classLinkAttribute")
                    .withValue("ClassLinkAttribute")
                    .withAbstractId(ClassType.getId())
                    .build();

    SQLTable ClassTypeSQLTable = SQLTable.builder()
                    .withName("ClassTypeSQLTable")
                    .withSqlTableName("CLASSTABLE")
                    .build();

    Attribute ClassTypeIDAttribute = Attribute.builder()
                    .withName("ID")
                    .withDataModelTypeId(ClassType.getId())
                    .withSqlTableId(ClassTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute ClassTypeLinkAttribute = Attribute.builder()
                    .withName("ClassLinkAttribute")
                    .withDataModelTypeId(ClassType.getId())
                    .withSqlTableId(ClassTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.LongType.getId())
                    .build();

    Attribute ClassTypeStringAttribute = Attribute.builder()
                    .withName("ClassStringAttr")
                    .withDataModelTypeId(ClassType.getId())
                    .withSqlTableId(ClassTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Type CompanyType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("CompanyType")
                    .build();

    SQLTable CompanyTypeSQLTable = SQLTable.builder()
                    .withName("CompanyTypeSQLTable")
                    .withSqlTableName("COMPANYTABLE")
                    .build();

    Attribute CompanyCompanyAttribute = Attribute.builder()
                    .withName("Company")
                    .withDataModelTypeId(CompanyType.getId())
                    .withSqlTableId(CompanyTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.CompanyLinkType.getId())
                    .build();

    Attribute CompanyStringAttribute = Attribute.builder()
                    .withName("StringAttribute")
                    .withDataModelTypeId(CompanyType.getId())
                    .withSqlTableId(CompanyTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    StatusGroup StatusGrp = StatusGroup.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("TestStatusGroup")
                    .withKeys("Open", "Closed", "Canceled")
                    .build();

    Type StatusType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("StatusType")
                    .build();

    SQLTable StatusTypeSQLTable = SQLTable.builder()
                    .withName("StatusTypeSQLTable")
                    .build();

    Attribute StatusAttribute = Attribute.builder()
                    .withName("StatusAttribute")
                    .withDataModelTypeId(StatusType.getId())
                    .withSqlTableId(StatusTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StatusType.getId())
                    .withLinkTypeId(StatusGrp.getId())
                    .build();

    Attribute StatusStringAttribute = Attribute.builder()
                    .withName("StatusStringAttribute")
                    .withDataModelTypeId(StatusType.getId())
                    .withSqlTableId(StatusTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    Type AllEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("AllEventType")
                    .build();

    SQLTable AllEventTypeSQLTable = SQLTable.builder()
                    .withName("AllEventTypeSQLTable")
                    .build();

    Attribute AllEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(AllEventType.getId())
                    .withSqlTableId(AllEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition AllEventTypeEvent1 = EventDefinition.builder()
                    .withObjectLink(AllEventType.getId())
                    .withInstId(100L, 101L, 102L, 103L, 104L, 105L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_DeletePre.getId(),
                                IDataModel.Admin_DataModel_Type_Trigger_DeleteOverride.getId(),
                                IDataModel.Admin_DataModel_Type_Trigger_DeletePost.getId(),
                                IDataModel.Admin_DataModel_Type_Trigger_InsertPre.getId(),
                                IDataModel.Admin_DataModel_Type_Trigger_InsertOverride.getId(),
                                IDataModel.Admin_DataModel_Type_Trigger_InsertPost.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("event")
                    .build();

    Type DeletePreEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("DeletePreEventType")
                    .build();

    SQLTable DeletePreEventTypeSQLTable = SQLTable.builder()
                    .withName("DeletePreEventTypeSQLTable")
                    .build();

    Attribute DeletePreEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(DeletePreEventType.getId())
                    .withSqlTableId(DeletePreEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition DeletePreEventTypeEvent = EventDefinition.builder()
                    .withObjectLink(DeletePreEventType.getId())
                    .withInstId(44L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_DeletePre.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("deletePre")
                    .build();

    Type DeletePostEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("DeletePostEventType")
                    .build();

    SQLTable DeletePostEventTypeSQLTable = SQLTable.builder()
                    .withName("DeletePostEventTypeSQLTable")
                    .build();

    Attribute DeletePostEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(DeletePostEventType.getId())
                    .withSqlTableId(DeletePostEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition DeletePostEventTypeEvent = EventDefinition.builder()
                    .withObjectLink(DeletePostEventType.getId())
                    .withInstId(45L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_DeletePost.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("deletePost")
                    .build();

    Type DeleteOverrideEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("DeleteOverrideEventType")
                    .build();

    SQLTable DeleteOverrideEventTypeSQLTable = SQLTable.builder()
                    .withName("DeleteOverrideEventTypeSQLTable")
                    .build();

    Attribute DeleteOverrideEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(DeleteOverrideEventType.getId())
                    .withSqlTableId(DeleteOverrideEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition DeleteOverrideEventTypeEvent = EventDefinition.builder()
                    .withObjectLink(DeleteOverrideEventType.getId())
                    .withInstId(46L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_DeleteOverride.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("deleteOverride")
                    .build();

    Type InsertPreEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("InsertPreEventType")
                    .build();

    SQLTable InsertPreEventTypeSQLTable = SQLTable.builder()
                    .withName("InsertPreEventTypeSQLTable")
                    .build();

    Attribute InsertPreEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(InsertPreEventType.getId())
                    .withSqlTableId(InsertPreEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition InsertPreEventTypeEvent = EventDefinition.builder()
                    .withObjectLink(InsertPreEventType.getId())
                    .withInstId(244L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_InsertPre.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("insertPre")
                    .build();

    Type InsertPostEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("InsertPostEventType")
                    .build();

    SQLTable InsertPostEventTypeSQLTable = SQLTable.builder()
                    .withName("InsertPostEventTypeSQLTable")
                    .build();

    Attribute InsertPostEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(InsertPostEventType.getId())
                    .withSqlTableId(InsertPostEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition InsertPostEventTypeEvent = EventDefinition.builder()
                    .withObjectLink(InsertPostEventType.getId())
                    .withInstId(245L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_InsertPost.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("insertPost")
                    .build();

    Type InsertOverrideEventType = Type.builder()
                    .withId(RandomUtils.nextLong())
                    .withName("InsertOverrideEventType")
                    .build();

    SQLTable InsertOverrideEventTypeSQLTable = SQLTable.builder()
                    .withName("InsertOverrideEventTypeSQLTable")
                    .build();

    Attribute InsertOverrideEventTypeStringAttribute = Attribute.builder()
                    .withName("EventAttribute")
                    .withDataModelTypeId(InsertOverrideEventType.getId())
                    .withSqlTableId(InsertOverrideEventTypeSQLTable.getId())
                    .withAttributeTypeId(IDataModel.StringType.getId())
                    .build();

    EventDefinition InsertOverrideEventTypeEvent = EventDefinition.builder()
                    .withObjectLink(InsertOverrideEventType.getId())
                    .withInstId(246L)
                    .withTypeId(IDataModel.Admin_DataModel_Type_Trigger_InsertOverride.getId())
                    .withESJP(TriggerEvent.class.getName())
                    .withMethod("insertOverride")
                    .build();
}

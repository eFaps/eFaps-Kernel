/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.admin;

import java.util.HashMap;
import java.util.UUID;

/**
 * The enumeration hold all required type definitions depending on the UUID.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public enum EFapsClassNames
{
    /** Attribute type Link. */
    ATTRTYPE_LINK("440f472f-7be2-41d3-baec-4a2f0e4e5b31"),
    /** Attribute type Link with Ranges. */
    ATTRTYPE_LINK_WITH_RANGES("9d6b2e3e-68ce-4509-a5f0-eae42323a696"),
    /** Attribute type Creator Link. */
    ATTRTYPE_CREATOR_LINK("76122fe9-8fde-4dd4-a229-e48af0fb4083"),
    /** Attribute type Modifier Link. */
    ATTRTYPE_MODIFIER_LINK("447a7c87-8395-48c4-b2ed-d4e96d46332c"),
    /** Attribute type Multi Line Array. */
    ATTRTYPE_MULTILINEARRAY("adb13c3d-9506-4da2-8d75-b54c76779c6c"),

    /** Admin_Common_SystemConfiguration. */
    CONFIG("5fecab1b-f4a8-447d-ad64-cf5965fe5d3b"),
    /** Admin_Common_SystemConfigurationAttribute. */
    CONFIG_ATTR("21c731b4-e717-47dd-92a3-9a6cf731b164"),
    /** Admin_Common_SystemConfigurationLink. */
    CONFIG_LINK("227048fb-f7a5-4bf5-b620-c88e6c87eed7"),

    /** Admin_DataModel_Type. */
    DATAMODEL_TYPE("8770839d-60fd-4bb4-81fd-3903d4c916ec"),
    /** Admin_DataModel_TypeEventIsAllowedFor. */
    DATAMODEL_TYPEEVENTISALLOWEDFOR("bf3d70ce-206e-4328-aa35-761c4aeb9d1d"),
    /** Admin_DataModel_TypeClassifies. */
    DATAMDOEL_TYPECLASSIFIES("276e9d68-08db-4ad2-91cb-26aa3947c690"),
    /** Admin_DataModel_TypeClassifyRelation. */
    DATAMDOEL_TYPECLASSIFYRELATION("fa6abe39-f23a-48bc-806d-0ee30d83449f"),
    /** Admin_DataModel_Attribute. */
    DATAMODEL_ATTRIBUTE("518a9802-cf0e-4359-9b3c-880f71e1387f"),
    /** Admin_DataModel_AttributeSet. */
    DATAMODEL_ATTRIBUTESET("a23b6c9f-5220-438f-93d0-f4651c3ba455"),
    /** Admin_DataModel_AttributeSetAttribute. */
    DATAMODEL_ATTRIBUTESETATTRIBUTE("f601ffc5-819c-41a0-8663-3e1b0fb35a9b"),

    /** Admin_DataModel_Type2Store. */
    DATAMODEL_TYPE2STORE("433f8358-dd69-4d53-9161-5ae9b0b51c57"),
    /** DB_Resource. */
    DB_RESOURCE("231082cc-1d04-4ea3-a618-da5e997c3a9c"),
    /** DB_Store. */
    DB_STORE("0ca5fb5f-74b3-416e-b80d-80d4baf9abfd"),
    /** DB_Store2Resource. */
    DB_STORE2RESOURCE("34be8b90-a753-45d5-98a7-78c1bcc34b72"),

    /** Admin_User_Abstract. */
    USER_ABSTRACT("4c3e33a2-a024-4bb7-b857-69886bce7132"),
    /** Admin_User_Person. */
    USER_PERSON("fe9d94fd-2ed8-4c44-b1f0-00e150555888"),
    /** Admin_User_Role. */
    USER_ROLE("e4d6ecbe-f198-4f84-aa69-5a9fd3165112"),
    /** Admin_User_Group. */
    USER_GROUP("f5e1e2ff-bfa9-40d9-8340-a259f48d5ad9"),
    /** Admin_User_JaasKey. */
    USER_JAASKEY("0e7650c6-8ec3-4c63-b377-f3eb5fb85f16"),
    /** Admin_User_JaasSystem. */
    USER_JAASSYSTEM("28e45c59-946d-4502-94b9-58a1bf23ab88"),
    /** Admin_User_Abstract2Abstract. */
    USER_ABSTRACT2ABSTRACT("1ded9229-3daa-4c27-8e2a-175e5760470b"),
    /** Admin_User_AttributeAbstract. */
    USER_ATTRIBUTEABSTRACT("d9dd0971-0bb9-4ac1-ba46-8aefd5e8badb"),
    /** Admin_User_Person2Role. */
    USER_PERSON2ROLE("37deb6ae-3e1c-4642-8823-715120386fc3"),
    /** Admin_User_Person2Group. */
    USER_PERSON2GROUP("fec64148-a39b-4f69-bedd-9c3bcfe8e1602"),

    /** Admin_Event_Definition. */
    EVENT_DEFINITION("9c1d52f4-94d6-4f95-ab81-bed23884cf03"),
    /** Admin_UI_Collection. */
    COLLECTION("0ad74515-5d2f-4579-bf67-4ed55c02ae9e"),
    /** Admin_UI_Field. */
    FIELD("1b3e1892-74bb-4df8-8e56-1de9c47cb3b8"),
    /** Admin_UI_FieldClassification. **/
    FIELDCLASSIFICATION("f4ad729b-f96a-4057-865f-ad910114e695"),
    /** Admin_UI_FieldAction. */
    FIELDCOMMAND("6e8fa8bf-5368-485d-a5ba-12dc600c54a5"),
    /** Admin_UI_FieldGroup. */
    FIELDGROUP("629cd86d-5103-4ee5-9eef-aef9de7862c3"),
    /** Admin_UI_FieldHeading. */
    FIELDHEADING("29e30382-63cd-44ac-91b6-ca1ca58ff434"),
    /** Admin_UI_FieldTable. */
    FIELDTABLE("d72728e9-2878-47fd-9250-21b5a04ebadb"),
    /** Admin_UI_FieldSet. */
    FIELDSET("415c196f-c1aa-4aa0-b96e-f8541332a921"),

    /** Admin_UI_Command. */
    COMMAND("65e8da96-7bd3-4a9f-867d-b2188dc2d882"),
    /** Admin_UI_Form. */
    FORM("e6ddf834-e4f4-481e-8afb-95bf3760b6ba"),
    /** Admin_UI_Image. */
    IMAGE("6e70fbed-fdfc-4ed3-a0f8-d0bc1858419d"),
    /** Admin_UI_Menu. */
    MENU("209d2e8b-608b-4b09-bdbb-ef5b98d0e2ab"),
    /** Admin_UI_Picker. */
    PICKER("259e8dda-dc0e-492c-96dc-850a2fa13d98"),
    /** Admin_UI_Search. */
    SEARCH("2cb35fbd-d495-4680-b7ad-e236507a5e94"),
    /** Admin_UI_Table. */
    TABLE("6f3695cb-fab5-45e5-8d8e-eb1e6870dcd3"),

    /** */
    LINK_ICON("c21150d9-f160-4eaf-b93f-66042697867e"),
    /** */
    LINK_ICONISTYPEICONFOR("74b91e57-e5a3-43df-b0e4-43815ad79fec"),
    /** */
    LINK_MENUISTYPETREEFOR("ce5087b5-ee5c-49c3-adfb-5da18f95a4d0"),
    /** Admin_UI_LinkIsTypeFormFor. */
    LINK_MENUISTYPEFORMFOR("3daa3a22-9399-4716-a670-2adaacfb6be4"),
    /** Admin_UI_LinkTargetCommand. */
    LINK_TARGET_CMD("8909acf2-2f38-474d-ba7f-713b3bddbef7"),
    /** */
    LINK_TARGET_FORM("3eb6f003-c04e-48f0-8fac-797438ed6501"),
    /** */
    LINK_TARGET_MENU("c646804e-29ad-4c7a-ac70-d024a77d131e"),
    /** */
    LINK_TARGET_SEARCH("c78c1f61-3f64-4f69-92fc-e01854bc7512"),
    /** */
    LINK_TARGET_TABLE("27eae97f-c6f4-4c4e-9947-c1c9bc4ea297"),
    /** */
    LINK_DEFAULT_SEARCHCOMMAND("3f827900-eda2-409f-be92-497dcacb0eef"),

    /** Admin_Program_Java. */
    ADMIN_PROGRAM_JAVA("11043a35-f73c-481c-8c77-00306dbce824"),
    /** Admin_Program_JavaClass. */
    ADMIN_PROGRAM_JAVACLASS("9118e1e3-ed4c-425d-8578-8d1f1d385110"),
    /** Admin_Program_StaticCompile. */
    ADMIN_PROGRAM_STATICCOMPILED("76fb464e-1d14-4437-ad23-092ab12669dd"),

    /** Admin_Program_CSS. */
    ADMIN_PROGRAM_CSS("f5a5bcf6-3cc7-4530-a5a0-7808a392381b"),
    /** Admin_Program_CSS2CSSS. */
    ADMIN_PROGRAM_CSS2CSS("9d69ef63-b248-4f50-9130-5f33d64d81f0"),
    /** Admin_Program_CSSCompiled. */
    ADMIN_PROGRAM_CSSCOMPILED("0607ea90-b48f-4b76-96f5-67cab19bd7b1"),

    /** Admin_Program_JavaScript. */
    ADMIN_PROGRAM_JAVASCRIPT("1c9ce325-7e4f-401f-aeb8-74e2e0c9e224"),
    /** Admin_Program_JavaScript2JavaScript. */
    ADMIN_PROGRAM_JAVASCRIPT2JAVASCRIPT("2d24e861-580c-43ad-a59c-3266021ea190"),
    /** Admin_Program_JavaScriptCompiled. */
    ADMIN_PROGRAM_JAVASCRIPTCOMPILED("0607ea90-b48f-4b76-96f5-67cab19bd7b1"),

    /** Admin_Program_XSL. */
    ADMIN_PROGRAM_XSL("2e40c566-a55c-4b3b-a79b-b786e20f8d1c"),

    /** Admin_CommonVersion. */
    ADMIN_COMMON_VERSION("1bb051f3-b664-43db-b409-c0c4009f5972"),
    /** Admin_Common_Property. */
    ADMIN_COMMON_PROPERTY("f3d54a86-c323-43d8-9c78-284d61d955b3");

    /**
     * The class is only used to define a mapping between UUID's and an instance
     * of this enumeration. Because such variable could not be defined as static
     * within enumeration definitions.
     */
    private static final class Mapper
    {

        /**
         * Mapping between the UUID and the enumeration instance.
         */
        private static HashMap<UUID, EFapsClassNames> MAPPER = new HashMap<UUID, EFapsClassNames>();

        /**
         * Empty Constructor.
         */
        private Mapper()
        {
        }
    }

    /**
     * Stored the UUID for the given type.
     */
    private final UUID uuid;

    /**
     * Private Constructor.
     *
     * @param _uuid UUID to set
     */
    private EFapsClassNames(final String _uuid)
    {
        this.uuid = UUID.fromString(_uuid);
        EFapsClassNames.Mapper.MAPPER.put(getUuid(), this);
    }

    /**
     * Get an enum.
     *
     * @param _uuid UUID the enum is wanted for
     * @return EFapsClassNames
     */
    public static EFapsClassNames getEnum(final UUID _uuid)
    {
        return EFapsClassNames.Mapper.MAPPER.get(_uuid);
    }

    /**
     * @return the uuid
     */
    public UUID getUuid()
    {
        return this.uuid;
    }
}

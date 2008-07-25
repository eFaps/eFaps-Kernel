/*
 * Copyright 2003-2008 The eFaps Team
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
 * @author tmo
 * @version $Id$
 */
public enum EFapsClassNames
{
  ATTRTYPE_LINK("440f472f-7be2-41d3-baec-4a2f0e4e5b31"),
  ATTRTYPE_LINK_WITH_RANGES("9d6b2e3e-68ce-4509-a5f0-eae42323a696"),
  ATTRTYPE_CREATOR_LINK("76122fe9-8fde-4dd4-a229-e48af0fb4083"),
  ATTRTYPE_MODIFIER_LINK("447a7c87-8395-48c4-b2ed-d4e96d46332c"),

  DATAMODEL_TYPE("8770839d-60fd-4bb4-81fd-3903d4c916ec"),
  DATAMODEL_TYPEEVENTISALLOWEDFOR("bf3d70ce-206e-4328-aa35-761c4aeb9d1d"),
  DATAMODEL_ATTRIBUTE("518a9802-cf0e-4359-9b3c-880f71e1387f"),

  USER_ABSTRACT("4c3e33a2-a024-4bb7-b857-69886bce7132"),
  USER_PERSON("fe9d94fd-2ed8-4c44-b1f0-00e150555888"),
  USER_ROLE("e4d6ecbe-f198-4f84-aa69-5a9fd3165112"),
  USER_GROUP("f5e1e2ff-bfa9-40d9-8340-a259f48d5ad9"),
  USER_JAASKEY("0e7650c6-8ec3-4c63-b377-f3eb5fb85f16"),
  USER_JAASSYSTEM("28e45c59-946d-4502-94b9-58a1bf23ab88"),

  USER_ABSTRACT2ABSTRACT("1ded9229-3daa-4c27-8e2a-175e5760470b"),
  USER_ATTRIBUTEABSTRACT("d9dd0971-0bb9-4ac1-ba46-8aefd5e8badb"),
  USER_PERSON2ROLE("37deb6ae-3e1c-4642-8823-715120386fc3"),
  USER_PERSON2GROUP("fec64148-a39b-4f69-bedd-9c3bcfe8e1602"),

  EVENT_DEFINITION("9c1d52f4-94d6-4f95-ab81-bed23884cf03"),

  COLLECTION("0ad74515-5d2f-4579-bf67-4ed55c02ae9e"),
  FIELD("1b3e1892-74bb-4df8-8e56-1de9c47cb3b8"),
  FIELDGROUP("629cd86d-5103-4ee5-9eef-aef9de7862c3"),
  FIELDHEADING("29e30382-63cd-44ac-91b6-ca1ca58ff434"),
  FIELDTABLE("d72728e9-2878-47fd-9250-21b5a04ebadb"),

  FORM("e6ddf834-e4f4-481e-8afb-95bf3760b6ba"),
  TABLE("6f3695cb-fab5-45e5-8d8e-eb1e6870dcd3"),
  COMMAND("65e8da96-7bd3-4a9f-867d-b2188dc2d882"),
  MENU("209d2e8b-608b-4b09-bdbb-ef5b98d0e2ab"),
  SEARCH("2cb35fbd-d495-4680-b7ad-e236507a5e94"),
  IMAGE("6e70fbed-fdfc-4ed3-a0f8-d0bc1858419d"),

  LINK_ICON("c21150d9-f160-4eaf-b93f-66042697867e"),
  LINK_ICONISTYPEICONFOR("74b91e57-e5a3-43df-b0e4-43815ad79fec"),
  LINK_MENUISTYPETREEFOR("ce5087b5-ee5c-49c3-adfb-5da18f95a4d0"),
  LINK_TARGET_FORM("3eb6f003-c04e-48f0-8fac-797438ed6501"),
  LINK_TARGET_MENU("c646804e-29ad-4c7a-ac70-d024a77d131e"),
  LINK_TARGET_SEARCH("c78c1f61-3f64-4f69-92fc-e01854bc7512"),
  LINK_TARGET_TABLE("27eae97f-c6f4-4c4e-9947-c1c9bc4ea297"),
  LINK_DEFAULT_SEARCHCOMMAND("3f827900-eda2-409f-be92-497dcacb0eef"),

  ADMIN_PROGRAM_JAVA("11043a35-f73c-481c-8c77-00306dbce824"),
  ADMIN_PROGRAM_JAVACLASS("9118e1e3-ed4c-425d-8578-8d1f1d385110"),
  ADMIN_PROGRAM_STATICCOMPILED("76fb464e-1d14-4437-ad23-092ab12669dd"),

  ADMIN_COMMON_VERSION("1bb051f3-b664-43db-b409-c0c4009f5972");

  /**
   * The class is only used to define a mapping between UUID's and an instance
   * of this enumeration. Because such variable could not be defined as static
   * within enumeration definitions.
   */
  private static class Mapper
  {
    /**
     * Mapping between the UUID and the enumeration instance.
     */
    static private HashMap<UUID, EFapsClassNames> mapper
            = new HashMap<UUID, EFapsClassNames>();
  }

  /**
   * Stored the UUID for the given type.
   */
  public final UUID uuid;

  private EFapsClassNames(final String _uuid)
  {
    this.uuid = UUID.fromString(_uuid);
    Mapper.mapper.put(this.uuid, this);
  }

  /**
   *
   * @param _uuid
   * @return
   */
  static public EFapsClassNames getEnum(final UUID _uuid)
  {
    return Mapper.mapper.get(_uuid);
  }
}

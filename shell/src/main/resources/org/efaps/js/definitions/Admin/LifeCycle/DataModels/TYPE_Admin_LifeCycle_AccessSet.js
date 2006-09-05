/*
 * Copyright 2006 The eFaps Team
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
 * Author:          tmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

with (TYPE)  {
  with (addAttribute("OID"))  {
    setAttributeType("OID");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Name"))  {
    setAttributeType("String");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("NAME");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_LifeCycle_AccessSetSQLTable");
    setSQLColumn("MODIFIED");
  }
//  addProperty("Icon",                   "${ROOTURL}/servlet/image/eFapsAdminLifeCycleAccessType");
  addProperty("Tree",                   "Admin_LifeCycle_AccessSetTree");
}

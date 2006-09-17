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
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Name"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("NAME");
  }
  with (addAttribute("UUID"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("UUID");
  }
  with (addAttribute("Revision"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("REVISION");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("MODIFIED");
  }
  with (addAttribute("ClassNamePerson"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("CLASSNAMEPERSON");
  }
  with (addAttribute("MethodNamePersonKey"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONKEY");
  }
  with (addAttribute("MethodNamePersonName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONNAME");
  }
  with (addAttribute("MethodNamePersonFirstName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONFIRSTNAME");
  }
  with (addAttribute("MethodNamePersonLastName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONLASTNAME");
  }
  with (addAttribute("MethodNamePersonEmail"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONEMAIL");
  }
  with (addAttribute("MethodNamePersonOrganisation"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONORG");
  }
  with (addAttribute("MethodNamePersonUrl"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONURL");
  }
  with (addAttribute("MethodNamePersonPhone"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONPHONE");
  }
  with (addAttribute("MethodNamePersonMobile"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONMOBILE");
  }
  with (addAttribute("MethodNamePersonFax"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODPERSONFAX");
  }
  with (addAttribute("ClassNameRole"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("CLASSNAMEROLE");
  }
  with (addAttribute("MethodNameRoleKey"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODROLEKEY");
  }
  with (addAttribute("ClassNameGroup"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("CLASSNAMEGROUP");
  }
  with (addAttribute("MethodNameGroupKey"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_JAASSystemSQLTable");
    setSQLColumn("METHODGROUPKEY");
  }
  addProperty("Icon",                   "${ROOTURL}/servlet/image/Admin_User_JAASSystemImage");
  addProperty("Tree",                   "Admin_User_JAASSystemTree");
}

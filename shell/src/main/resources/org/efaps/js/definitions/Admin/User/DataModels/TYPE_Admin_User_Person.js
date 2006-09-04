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
  setParentType(new Type("Admin_User_Abstract"));
  with (addAttribute("FirstName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("FIRSTNAME");
  }
  with (addAttribute("LastName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("LASTNAME");
  }
  with (addAttribute("Email"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("EMAIL");
  }
  with (addAttribute("Organisation"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("ORG");
  }
  with (addAttribute("Url"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("URL");
  }
  with (addAttribute("Phone"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("PHONE");
  }
  with (addAttribute("Mobile"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("MOBILE");
  }
  with (addAttribute("Fax"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("FAX");
  }
  with (addAttribute("Password"))  {
    setAttributeType("Password");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("PASSWORD");
  }
  with (addAttribute("LastLogin"))  {
    setAttributeType("DateTime");
    setSQLTable("Admin_User_PersonSQLTable");
    setSQLColumn("LASTLOGIN");
  }
  addProperty("Form",                   "Admin_User_PersonForm");
  addProperty("Icon",                   "${ROOTURL}/servlet/image/Admin_User_PersonImage");
  addProperty("Tree",                   "Admin_User_PersonTree");
}

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
  setParentType(new Type("Admin_DataModel_Abstract"));
  with (addAttribute("SQLTable"))  {
    setAttributeType("String");
    setSQLTable("Admin_DataModel_SQLTableSQLTable");
    setSQLColumn("SQLTABLE");
  }
  with (addAttribute("SQLColumnID"))  {
    setAttributeType("String");
    setSQLTable("Admin_DataModel_SQLTableSQLTable");
    setSQLColumn("SQLCOLUMNID");
  }
  with (addAttribute("SQLColumnType"))  {
    setAttributeType("String");
    setSQLTable("Admin_DataModel_SQLTableSQLTable");
    setSQLColumn("SQLCOLUMNTYPE");
  }
  with (addAttribute("DMTableMain"))  {
    setAttributeType("Link");
    setTypeLink("Admin_DataModel_SQLTable");
    setSQLTable("Admin_DataModel_SQLTableSQLTable");
    setSQLColumn("DMTABLEMAIN");
  }
  addProperty("Icon",                   "${ROOTURL}/servlet/image/Admin_DataModel_SQLTableImage");
  addProperty("Tree",                   "Admin_DataModel_SQLTableTree");
}

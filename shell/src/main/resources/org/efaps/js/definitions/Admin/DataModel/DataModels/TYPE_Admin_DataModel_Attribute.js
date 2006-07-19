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
  with (addAttribute("SQLColumn"))  {
    setAttributeType("String");
    setSQLTable("Admin_DataModel_AttributeTable");
    setSQLColumn("SQLCOLUMN");
  }
  with (addAttribute("Table"))  {
    setAttributeType("Link");
    setTypeLink("Admin_DataModel_SQLTable");
    setSQLTable("Admin_DataModel_AttributeTable");
    setSQLColumn("DMTABLE");
  }
  with (addAttribute("ParentType"))  {
    setAttributeType("Link");
    setTypeLink("Admin_DataModel_Type");
    setSQLTable("Admin_DataModel_AttributeTable");
    setSQLColumn("DMTYPE");
  }
  with (addAttribute("AttributeType"))  {
    setAttributeType("Link");
    setTypeLink("Admin_DataModel_AttributeType");
    setSQLTable("Admin_DataModel_AttributeTable");
    setSQLColumn("DMATTRIBUTETYPE");
  }
  with (addAttribute("TypeLink"))  {
    setAttributeType("Link");
    setTypeLink("Admin_DataModel_Type");
    setSQLTable("Admin_DataModel_AttributeTable");
    setSQLColumn("DMTYPELINK");
  }
  addProperty("Icon",                   "${ROOTURL}/servlet/image/Admin_DataModel_AttributeImage");
  addProperty("Tree",                   "Admin_DataModel_AttributeTree");
}

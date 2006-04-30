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
  setParentType(new Type("Admin_UI_Direct"));
  with (addAttribute("FileName"))  {
    setAttributeType("String");
    setSQLTable("Admin_UI_FileTable");
    setSQLColumn("FILENAME");
  }
  with (addAttribute("FileLength"))  {
    setAttributeType("Long");
    setSQLTable("Admin_UI_FileTable");
    setSQLColumn("FILELENGTH");
  }
  addProperty("Icon",                   "${ROOTURL}/servlet/image/Admin_UI_FileImage");
  addProperty("StoreAttributeFileLength","FileLength");
  addProperty("StoreAttributeFileName", "FileName");
  addProperty("StoreJDBCBlob",          "FILECONTENT");
  addProperty("StoreJDBCKey",           "ID");
  addProperty("StoreJDBCTable",         "UIFILE");
  addProperty("StoreResource",          "org.efaps.db.transaction.JDBCStoreResource");
  addProperty("Tree",                   "Admin_UI_FileTree");
}

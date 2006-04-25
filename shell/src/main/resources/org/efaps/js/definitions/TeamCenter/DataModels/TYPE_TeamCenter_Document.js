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
  setParentType(new Type("Admin_Abstract"));
  with (addAttribute("FileName"))  {
    setAttributeType("String");
    setSQLTable("TeamCenter_DocumentSQLTable");
    setSQLColumn("FILENAME");
  }
  with (addAttribute("FileLength"))  {
    setAttributeType("Long");
    setSQLTable("TeamCenter_DocumentSQLTable");
    setSQLColumn("FILELENGTH");
  }
  addProperty("Icon",                   "${ROOTURL}/servlet/image/TeamCenter_DocumentImage");
  addProperty("StoreAttributeFileLength","FileLength");
  addProperty("StoreAttributeFileName", "FileName");
  addProperty("Tree",                   "TeamCenter_DocumentTree");
  addProperty("VFSPrefix",              "S:/efaps/TESTSTORE");
  addProperty("VFSProvider",            "org.apache.commons.vfs.provider.local.DefaultLocalFileProvider");
}

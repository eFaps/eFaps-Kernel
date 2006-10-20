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
 * Revision:        $Rev: 231 $
 * Last Changed:    $Date: 2006-06-05 23:36:55 +0200 (Mo, 05 Jun 2006) $
 * Last Changed By: $Author: tmo $
 */

with (FORM)  {
  with (addField("type"))  {
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Type");
    addProperty("Searchable",             "false");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("name"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Name");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
  with (addField("uuid"))  {
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "UUID");
    addProperty("Searchable",             "true");
  }
  with (addField("revision"))  {
    addProperty("Columns",                "20");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Revision");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
  with (addField("createGroup"))  {
    addProperty("Creatable",              "false");
    addProperty("GroupCount",             "2");
  }
  with (addField("creator"))  {
    addProperty("AlternateOID",           "Creator.OID");
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Creator");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Searchable",             "true");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("created"))  {
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Created");
    addProperty("Searchable",             "true");
  }
  with (addField("modifyGroup"))  {
    addProperty("Creatable",              "false");
    addProperty("GroupCount",             "2");
  }
  with (addField("modifier"))  {
    addProperty("AlternateOID",           "Modifier.OID");
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Modifier");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Searchable",             "true");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("modified"))  {
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Modified");
    addProperty("Searchable",             "true");
  }
  with (addField("SQLCacheExpr"))  {
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "SQLCacheExpr");
    addProperty("Searchable",             "true");
  }
  with (addField("ParentType"))  {
    addProperty("AlternateOID",           "ParentType.OID");
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "ParentType.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_Type/ParentType.Label");
    addProperty("Searchable",             "false");
    addProperty("ShowTypeIcon",           "true");
  }
}

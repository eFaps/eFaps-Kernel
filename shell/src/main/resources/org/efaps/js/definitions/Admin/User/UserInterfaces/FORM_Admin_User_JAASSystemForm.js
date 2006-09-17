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
    addProperty("Searchable",             "true");
  }
  with (addField("createGroup"))  {
    addProperty("Creatable",              "false");
    addProperty("Searchable",             "false");
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
    addProperty("Searchable",             "false");
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
  with (addField("ClassNamePerson"))  {
    addProperty("Columns",                "80");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "ClassNamePerson");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonKey"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonKey");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonName"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonName");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonFirstName"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonFirstName");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonLastName"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonLastName");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonEmail"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonEmail");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonOrganisation"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonOrganisation");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonUrl"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonUrl");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonPhone"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonPhone");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonMobile"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonMobile");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNamePersonFax"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNamePersonFax");
    addProperty("Searchable",             "true");
  }
  with (addField("ClassNameRole"))  {
    addProperty("Columns",                "80");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "ClassNameRole");
    addProperty("Required",               "false");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNameRoleKey"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNameRoleKey");
    addProperty("Required",               "false");
    addProperty("Searchable",             "true");
  }
  with (addField("ClassNameGroup"))  {
    addProperty("Columns",                "80");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "ClassNameGroup");
    addProperty("Required",               "false");
    addProperty("Searchable",             "true");
  }
  with (addField("MethodNameGroupKey"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "MethodNameGroupKey");
    addProperty("Required",               "false");
    addProperty("Searchable",             "true");
  }
}

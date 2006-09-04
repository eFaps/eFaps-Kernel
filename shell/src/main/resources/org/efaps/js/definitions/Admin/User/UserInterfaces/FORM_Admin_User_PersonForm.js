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
  with (addField("lastlogin"))  {
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "LastLogin");
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
  with (addField("LastName"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "LastName");
    addProperty("Searchable",             "true");
  }
  with (addField("FirstName"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "FirstName");
    addProperty("Searchable",             "true");
  }
  with (addField("Email"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Email");
    addProperty("Searchable",             "true");
  }
  with (addField("Organisation"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Organisation");
    addProperty("Searchable",             "true");
  }
  with (addField("Url"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Url");
    addProperty("Searchable",             "true");
  }
  with (addField("Phone"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Phone");
    addProperty("Searchable",             "true");
  }
  with (addField("Mobile"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Mobile");
    addProperty("Searchable",             "true");
  }
  with (addField("Fax"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Fax");
    addProperty("Searchable",             "true");
  }
}

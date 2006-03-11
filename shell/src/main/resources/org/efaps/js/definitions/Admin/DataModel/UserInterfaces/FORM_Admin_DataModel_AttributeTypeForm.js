/*******************************************************************************
* Admin_UI_Form:
* ~~~~~~~~~~~~~~
* Admin_DataModel_AttributeTypeForm
*
* Description:
* ~~~~~~~~~~~~
*
* History:
* ~~~~~~~~
* Revision: $Rev$
* Date:     $Date$
* By:       $Author$
*
* Author:
* ~~~~~~~
* TMO
*******************************************************************************/

with (FORM)  {
  with (addField("id"))  {
    addProperty("Columns",                "20");
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "ID");
    addProperty("Searchable",             "true");
  }
  with (addField("type"))  {
    addProperty("Columns",                "20");
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Type");
    addProperty("Searchable",             "false");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("name"))  {
    addProperty("Columns",                "20");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Name");
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
  with (addField("Classname"))  {
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Classname");
    addProperty("Searchable",             "true");
  }
  with (addField("ClassnameUI"))  {
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "ClassnameUI");
    addProperty("Searchable",             "true");
  }
  with (addField("AlwaysUpdate"))  {
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "AlwaysUpdate");
    addProperty("Searchable",             "true");
  }
  with (addField("CreateUpdate"))  {
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "CreateUpdate");
    addProperty("Searchable",             "true");
  }
}

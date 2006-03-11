/*******************************************************************************
* Admin_UI_Form:
* ~~~~~~~~~~~~~~
* Admin_UI_FieldForm
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
  with (addField("name"))  {
    addProperty("Columns",                "20");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Name");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
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
  with (addField("collection"))  {
    addProperty("AlternateOID",           "Collection.OID");
    addProperty("Creatable",              "false");
    addProperty("Editable",               "false");
    addProperty("Expression",             "Collection");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Searchable",             "false");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("collectionType"))  {
    addProperty("Expression",             "Collection.Type");
  }
}

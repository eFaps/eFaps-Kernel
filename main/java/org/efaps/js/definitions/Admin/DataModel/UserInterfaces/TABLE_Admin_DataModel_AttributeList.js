/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_DataModel_AttributeList
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

with (TABLE)  {
  with (addField("id"))  {
    addProperty("Expression",             "ID");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_Attribute/ID.Label");
  }
  with (addField("name"))  {
    addProperty("Expression",             "Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_Attribute/Name.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("AttributeType"))  {
    addProperty("Expression",             "AttributeType.Name");
    addProperty("Label",                  "Admin_DataModel_Attribute/AttributeType.Label");
  }
  with (addField("ParentType"))  {
    addProperty("AlternateOID",           "ParentType.OID");
    addProperty("Expression",             "ParentType.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_Attribute/ParentType.Label");
  }
  with (addField("SQLColumn"))  {
    addProperty("Expression",             "SQLColumn");
    addProperty("Label",                  "Admin_DataModel_Attribute/SQLColumn.Label");
  }
  with (addField("Table"))  {
    addProperty("AlternateOID",           "Table.OID");
    addProperty("Expression",             "Table.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_Attribute/Table.Label");
  }
  with (addField("SQLTable"))  {
    addProperty("AlternateOID",           "Table.OID");
    addProperty("Expression",             "Table.SQLTable");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_Attribute/Table.Label");
  }
  with (addField("buttonOpenInNewWindow"))  {
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}

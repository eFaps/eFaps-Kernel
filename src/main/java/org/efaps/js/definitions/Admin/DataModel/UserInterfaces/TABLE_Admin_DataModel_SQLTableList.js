/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_DataModel_SQLTableList
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
  with (addField("name"))  {
    addProperty("Expression",             "Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_SQLTable/Name.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("SQLTable"))  {
    addProperty("Expression",             "SQLTable");
    addProperty("Label",                  "Admin_DataModel_SQLTable/SQLTable.Label");
  }
  with (addField("SQLColumnID"))  {
    addProperty("Expression",             "SQLColumnID");
    addProperty("Label",                  "Admin_DataModel_SQLTable/SQLColumnID.Label");
  }
  with (addField("SQLColumnType"))  {
    addProperty("Expression",             "SQLColumnType");
    addProperty("Label",                  "Admin_DataModel_SQLTable/SQLColumnType.Label");
  }
  with (addField("SQLNewIDSelect"))  {
    addProperty("Expression",             "SQLNewIDSelect");
    addProperty("Label",                  "Admin_DataModel_SQLTable/SQLNewIDSelect.Label");
  }
  with (addField("DMTableMain"))  {
    addProperty("AlternateOID",           "DMTableMain.OID");
    addProperty("Expression",             "DMTableMain.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_SQLTable/DMTableMain.Label");
  }
  with (addField("buttonOpenInNewWindow"))  {
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}

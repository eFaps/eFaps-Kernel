/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_DataModel_AttributeTypeList
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
    addProperty("Label",                  "Admin_DataModel_AttributeType/ID.Label");
  }
  with (addField("name"))  {
    addProperty("Expression",             "Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_DataModel_AttributeType/Name.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("classname"))  {
    addProperty("Expression",             "Classname");
    addProperty("Label",                  "Admin_DataModel_AttributeType/Classname.Label");
  }
  with (addField("alwaysupdate"))  {
    addProperty("Expression",             "AlwaysUpdate");
    addProperty("Label",                  "Admin_DataModel_AttributeType/AlwaysUpdate.Label");
  }
  with (addField("createupdate"))  {
    addProperty("Expression",             "CreateUpdate");
    addProperty("Label",                  "Admin_DataModel_AttributeType/CreateUpdate.Label");
  }
  with (addField("buttonOpenInNewWindow"))  {
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}

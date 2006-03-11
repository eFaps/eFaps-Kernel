/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_DataModel_TypeTree_Attributes
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

with (COMMAND)  {
  addProperty("Target",                 "content");
  addProperty("TargetExpand",           "Admin_DataModel_Attribute\\ParentType");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminDataModelAttributeList");
  addTargetTable("Admin_DataModel_AttributeList");
}

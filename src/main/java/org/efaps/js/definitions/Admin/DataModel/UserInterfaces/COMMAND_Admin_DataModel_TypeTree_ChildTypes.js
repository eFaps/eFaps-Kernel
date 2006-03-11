/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_DataModel_TypeTree_ChildTypes
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
  addProperty("TargetExpand",           "Admin_DataModel_Type\\ParentType");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminDataModelTypeList");
  addTargetTable("Admin_DataModel_TypeList");
}

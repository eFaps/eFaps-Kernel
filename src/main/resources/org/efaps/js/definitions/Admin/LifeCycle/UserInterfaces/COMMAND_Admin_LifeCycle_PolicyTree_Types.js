/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_LifeCycle_PolicyTree_Types
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
  addProperty("TargetExpand",           "Admin_DataModel_Type2Policy\\PolicyLink.TypeLink");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminDataModelTypeList");
  addTargetTable("Admin_DataModel_TypeList");
}

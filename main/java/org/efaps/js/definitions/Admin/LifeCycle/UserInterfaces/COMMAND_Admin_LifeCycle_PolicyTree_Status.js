/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_LifeCycle_PolicyTree_Status
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
  addProperty("TargetExpand",           "Admin_LifeCycle_Status\\PolicyLink");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminLifeCycleStatusList");
  addTargetTable("Admin_LifeCycle_StatusList");
}

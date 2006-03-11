/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_DataModel_TypeTree_Policies
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
  addProperty("TargetExpand",           "Admin_DataModel_Type2Policy\\TypeLink.PolicyLink");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminLifeCyclePolicyList");
  addTargetTable("Admin_LifeCycle_PolicyList");
}

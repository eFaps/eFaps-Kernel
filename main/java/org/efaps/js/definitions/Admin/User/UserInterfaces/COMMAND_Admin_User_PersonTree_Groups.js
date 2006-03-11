/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_PersonTree_Groups
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
  addProperty("TargetExpand",           "Admin_User_Person2Group\\UserFromLink.UserToLink");
  addProperty("TargetMode",             "view");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("eFapsAdminUserGroupList");
  addTargetMenu("Admin_User_PersonTree_Groups_Menu");
  addTargetTable("Admin_User_AbstractList");
}

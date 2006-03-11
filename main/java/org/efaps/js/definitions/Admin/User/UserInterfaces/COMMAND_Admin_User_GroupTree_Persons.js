/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_GroupTree_Persons
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
  addProperty("TargetExpand",           "Admin_User_Person2Group\\UserToLink.UserFromLink");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminUserPersonList");
  addTargetTable("Admin_User_PersonList");
}

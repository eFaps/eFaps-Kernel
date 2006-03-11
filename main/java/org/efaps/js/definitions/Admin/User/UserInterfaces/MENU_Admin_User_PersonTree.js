/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_User_PersonTree
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

with (MENU)  {
  addProperty("Target",                 "content");
  addProperty("TargetMode",             "view");
  addTargetForm("Admin_User_PersonForm");
  addTargetMenu("Admin_User_PersonTree_Menu");
  addChild(new Command("Admin_User_PersonTree_Roles"));
  addChild(new Command("Admin_User_PersonTree_Groups"));
}

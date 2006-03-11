/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_UI_FieldTree_Links_Menu_Action
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
  addProperty("Target",                 "popup");
  addTargetSearch("Admin_UI_FieldTree_Links_Menu_Action_AddExisting_Search");
  addChild(new Command("Admin_UI_FieldTree_Links_Menu_Action_AddExisting"));
  addChild(new Command("Admin_UI_CommandTree_Links_Menu_Action_Disconnect"));
}

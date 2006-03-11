/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_UI_FieldTree
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
  addTargetForm("Admin_UI_FieldForm");
  addTargetMenu("Admin_UI_FieldTree_Menu");
  addChild(new Command("Admin_UI_AbstractTree_Properties"));
  addChild(new Command("Admin_UI_FieldTree_Links"));
}

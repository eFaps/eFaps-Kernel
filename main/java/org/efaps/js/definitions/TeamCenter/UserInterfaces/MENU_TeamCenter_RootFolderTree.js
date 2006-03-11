/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* TeamCenter_RootFolderTree
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
  addProperty("Target",                       "content");
  addProperty("TargetMode",                   "view");
  addTargetForm("TeamCenter_FolderForm");
  addTargetMenu("TeamCenter_RootFolderTree_Menu");
  addChild(new Command("TeamCenter_FolderTree_SubFolders"));
//  addChild(new Command("Admin_UI_AbstractTree_Properties"));
//  addChild(new Command("Admin_UI_DirectTree_Used"));
}

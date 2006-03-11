/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* TeamCenter_FolderTree
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
  addTargetMenu("TeamCenter_FolderTree_Menu");
  addChild(new Command("TeamCenter_FolderTree_SubFolders"));
  addChild(new Command("TeamCenter_FolderTree_Documents"));
//  addChild(new Command("Admin_UI_DirectTree_Used"));
}

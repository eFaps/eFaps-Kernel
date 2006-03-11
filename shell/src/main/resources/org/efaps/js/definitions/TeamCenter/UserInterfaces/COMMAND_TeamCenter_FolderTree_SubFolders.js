/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* TeamCenter_FolderTree_SubFolders
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
  addProperty("Target",                       "content");
  addProperty("TargetExpand",                 "TeamCenter_Folder\\ParentFolder");
  addProperty("TargetMode",                   "view");
  addProperty("TargetShowCheckBoxes",         "true");
//  addIcon("Admin_PropertyListImage");
  addTargetMenu("TeamCenter_FolderTree_SubFolders_Menu");
  addTargetTable("TeamCenter_FolderList");
}

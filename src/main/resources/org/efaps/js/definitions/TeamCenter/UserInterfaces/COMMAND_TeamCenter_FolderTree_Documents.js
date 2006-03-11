/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* TeamCenter_FolderTree_Documents
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
  addProperty("DefaultSelected",              "true");
  addProperty("Target",                       "content");
  addProperty("TargetExpand",                 "TeamCenter_Document2Folder\\Folder.Document");
  addProperty("TargetMode",                   "view");
  addProperty("TargetShowCheckBoxes",         "true");
//  addIcon("Admin_UI_CommandChildListImage");
  addTargetMenu("TeamCenter_FolderTree_Documents_Menu");
  addTargetTable("TeamCenter_DocumentList");
}

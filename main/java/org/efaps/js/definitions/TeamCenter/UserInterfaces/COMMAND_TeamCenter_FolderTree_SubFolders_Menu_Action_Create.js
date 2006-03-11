/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* TeamCenter_FolderTree_SubFolders_Menu_Action_Create
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
  addProperty("Target",                       "popup");
  addProperty("TargetConnectAttribute",       "TeamCenter_Folder/ParentFolder");
  addProperty("TargetCreateType",             "TeamCenter_Folder");
  addProperty("TargetMode",                   "create");
  addTargetForm("TeamCenter_FolderForm");
}

/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* TeamCenter_RootFolderMyDesk
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
  addProperty("TargetQueryTypes",             "TeamCenter_RootFolder");
  addProperty("TargetShowCheckBoxes",         "true");
//  addIcon("Admin_UI_ImageImage");
  addTargetMenu("TeamCenter_RootFolderMyDesk_Menu");
  addTargetTable("TeamCenter_FolderList");
}

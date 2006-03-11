/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* TeamCenter_RootFolderMyDesk_Menu_Action_Create
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
  addProperty("TargetCreateType",             "TeamCenter_RootFolder");
  addProperty("TargetMode",                   "create");
  addTargetForm("TeamCenter_FolderForm");
}

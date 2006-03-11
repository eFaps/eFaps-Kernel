/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* TeamCenter_FolderTree_Documents_Menu_Action_Create
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
  addProperty("TargetConnectChildAttribute",  "Document");
  addProperty("TargetConnectParentAttribute", "Folder");
  addProperty("TargetConnectType",            "TeamCenter_Document2Folder");
  addProperty("TargetCreateType",             "TeamCenter_Document");
  addProperty("TargetMode",                   "create");
  addTargetForm("TeamCenter_DocumentForm");
}

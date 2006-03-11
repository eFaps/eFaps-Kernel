/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_CommandTree_Parents
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
  addProperty("Target",                 "content");
  addProperty("TargetExpand",           "Admin_UI_Menu2Command\\ToCommand.FromMenu");
  addProperty("TargetMode",             "view");
  addIcon("Admin_UI_MenuParentListImage");
  addTargetTable("Admin_UI_AbstractList");
}

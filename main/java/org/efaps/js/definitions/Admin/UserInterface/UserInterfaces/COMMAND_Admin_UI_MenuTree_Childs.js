/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_MenuTree_Childs
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
  addProperty("TargetExpand",           "Admin_UI_Menu2Command\\FromMenu.ToCommand");
  addProperty("TargetMode",             "view");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_CommandChildListImage");
  addTargetMenu("Admin_UI_MenuTree_Childs_Menu");
  addTargetTable("Admin_UI_AbstractList");
}

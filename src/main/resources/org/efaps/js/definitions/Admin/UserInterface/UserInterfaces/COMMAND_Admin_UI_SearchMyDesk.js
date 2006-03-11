/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_SearchMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_UI_Search");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_SearchImage");
  addTargetMenu("Admin_UI_SearchMyDesk_Menu");
  addTargetTable("Admin_UI_AbstractList");
}

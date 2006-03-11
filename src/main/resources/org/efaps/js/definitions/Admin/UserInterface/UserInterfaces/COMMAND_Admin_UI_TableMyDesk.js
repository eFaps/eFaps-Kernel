/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_TableMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_UI_Table");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_TableImage");
  addTargetMenu("Admin_UI_TableMyDesk_Menu");
  addTargetTable("Admin_UI_AbstractList");
}

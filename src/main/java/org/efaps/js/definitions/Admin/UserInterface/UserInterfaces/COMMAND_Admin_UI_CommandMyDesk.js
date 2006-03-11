/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_CommandMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_UI_Command");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_CommandImage");
  addTargetMenu("Admin_UI_CommandMyDesk_Menu");
  addTargetTable("Admin_UI_AbstractList");
}

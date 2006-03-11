/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_MenuMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_UI_Menu");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_MenuImage");
  addTargetMenu("Admin_UI_MenuMyDesk_Menu");
  addTargetTable("Admin_UI_AbstractList");
}

/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_FormMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_UI_Form");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_FormImage");
  addTargetMenu("Admin_UI_FormMyDesk_Menu");
  addTargetTable("Admin_UI_AbstractList");
}

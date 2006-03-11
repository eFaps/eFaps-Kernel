/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_GroupMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_User_Group");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("eFapsAdminUserGroup");
  addTargetMenu("Admin_User_GroupMyDesk_Menu");
  addTargetTable("Admin_User_AbstractList");
}

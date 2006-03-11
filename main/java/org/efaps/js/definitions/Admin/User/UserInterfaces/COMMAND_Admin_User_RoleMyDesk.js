/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_RoleMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_User_Role");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("eFapsAdminUserRole");
  addTargetMenu("Admin_User_RoleMyDesk_Menu");
  addTargetTable("Admin_User_AbstractList");
}

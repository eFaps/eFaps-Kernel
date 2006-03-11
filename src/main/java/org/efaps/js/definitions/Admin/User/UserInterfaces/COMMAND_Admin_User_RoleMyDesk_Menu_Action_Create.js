/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_RoleMyDesk_Menu_Action_Create
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
  addProperty("Target",                 "popup");
  addProperty("TargetCreateType",       "Admin_User_Role");
  addProperty("TargetMode",             "create");
  addTargetForm("Admin_User_AbstractForm");
}

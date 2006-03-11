/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_PersonMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_User_Person");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("eFapsAdminUserPerson");
  addTargetMenu("Admin_User_PersonMyDesk_Menu");
  addTargetTable("Admin_User_PersonList");
}

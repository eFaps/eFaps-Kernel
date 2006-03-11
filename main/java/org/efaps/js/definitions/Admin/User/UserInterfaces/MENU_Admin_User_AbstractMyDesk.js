/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_User_AbstractMyDesk
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

with (MENU)  {
  addChild(new Command("Admin_User_PersonMyDesk"));
  addChild(new Command("Admin_User_RoleMyDesk"));
  addChild(new Command("Admin_User_GroupMyDesk"));
}

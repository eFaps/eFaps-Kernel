/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_User_AbstractSearch
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
  addChild(new Command("Admin_User_PersonSearch"));
  addChild(new Command("Admin_User_RoleSearch"));
  addChild(new Command("Admin_User_GroupSearch"));
}

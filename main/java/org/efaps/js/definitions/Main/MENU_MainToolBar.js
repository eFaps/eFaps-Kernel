/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* MainToolBar
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
  addChild(new Menu("Admin.MyDesk.Admin"));
  addChild(new Menu("TeamCenter_MyDesk"));
  addChild(new Command("Main_ReloadCacheToolBar"));
  addChild(new Command("Main_LogoutToolBar"));
}

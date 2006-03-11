/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin.MyDesk.Admin
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
  addChild(new Command("Admin_MyDesk_Admin_Search"));
  addChild(new Menu("Admin_User_AbstractMyDesk"));
  addChild(new Menu("Admin_DataModel_AbstractMyDesk"));
  addChild(new Menu("Admin_LifeCycle_AbstractMyDesk"));
  addChild(new Menu("Admin_UI_AbstractMyDesk"));
}

/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_UI_AbstractMyDesk
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
  addChild(new Command("Admin_UI_CommandMyDesk"));
  addChild(new Command("Admin_UI_MenuMyDesk"));
  addChild(new Command("Admin_UI_FormMyDesk"));
  addChild(new Command("Admin_UI_TableMyDesk"));
  addChild(new Command("Admin_UI_SearchMyDesk"));
  addChild(new Command("Admin_UI_ImageMyDesk"));
}

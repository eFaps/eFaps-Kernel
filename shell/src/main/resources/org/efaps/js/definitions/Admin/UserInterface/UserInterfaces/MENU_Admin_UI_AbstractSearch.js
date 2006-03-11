/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_UI_AbstractSearch
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
  addChild(new Command("Admin_UI_CommandSearch"));
  addChild(new Command("Admin_UI_MenuSearch"));
  addChild(new Command("Admin_UI_FormSearch"));
  addChild(new Command("Admin_UI_TableSearch"));
  addChild(new Command("Admin_UI_SearchSearch"));
}

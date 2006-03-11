/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_LifeCycle_PolicyTree
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
  addProperty("Target",                 "content");
  addProperty("TargetMode",             "view");
  addTargetForm("Admin_LifeCycle_PolicyForm");
  addChild(new Command("Admin_LifeCycle_PolicyTree_Status"));
  addChild(new Command("Admin_LifeCycle_PolicyTree_Types"));
}

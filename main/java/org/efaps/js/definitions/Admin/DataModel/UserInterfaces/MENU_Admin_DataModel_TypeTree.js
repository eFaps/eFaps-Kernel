/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_DataModel_TypeTree
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
  addTargetForm("Admin_DataModel_TypeForm");
  addTargetMenu("Admin_DataModel_TypeTree_Menu");
  addChild(new Command("Admin_DataModel_TypeTree_Attributes"));
  addChild(new Command("Admin_DataModel_TypeTree_ChildTypes"));
  addChild(new Command("Admin_DataModel_TypeTree_Policies"));
  addChild(new Command("Admin_DataModel_AbstractTree_Properties"));
}

/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_DataModel_AttributeTree
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
  addTargetForm("Admin_DataModel_AttributeForm");
  addTargetMenu("Admin_DataModel_AttributeTree_Menu");
  addChild(new Command("Admin_DataModel_AbstractTree_Properties"));
}

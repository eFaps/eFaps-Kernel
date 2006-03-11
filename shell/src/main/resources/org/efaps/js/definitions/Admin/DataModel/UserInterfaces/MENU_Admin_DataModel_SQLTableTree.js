/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_DataModel_SQLTableTree
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
  addTargetForm("Admin_DataModel_SQLTableForm");
  addChild(new Command("Admin_DataModel_SQLTableTree_Attributes"));
  addChild(new Command("Admin_DataModel_SQLTableTree_SQLTables"));
  addChild(new Command("Admin_DataModel_AbstractTree_Properties"));
}

/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* Admin_DataModel_AttributeTypeTree
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
  addTargetForm("Admin_DataModel_AttributeTypeForm");
  addChild(new Command("Admin_DataModel_AttributeTypeTree_Attributes"));
}

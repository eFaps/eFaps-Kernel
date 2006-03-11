/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_CollectionTree_Fields
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

with (COMMAND)  {
  addProperty("Target",                 "content");
  addProperty("TargetExpand",           "Admin_UI_Field\\Collection");
  addProperty("TargetMode",             "view");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_FieldListImage");
  addTargetMenu("Admin_UI_CollectionTree_Fields_Menu");
  addTargetTable("Admin_UI_FieldList");
}

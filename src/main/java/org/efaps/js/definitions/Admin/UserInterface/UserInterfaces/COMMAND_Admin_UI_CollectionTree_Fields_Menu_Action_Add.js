/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_CollectionTree_Fields_Menu_Action_Add
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
  addProperty("Target",                 "popup");
  addProperty("TargetConnectAttribute", "Admin_UI_Field/Collection");
  addProperty("TargetCreateType",       "Admin_UI_Field");
  addProperty("TargetMode",             "create");
  addTargetForm("Admin_UI_FieldForm");
}

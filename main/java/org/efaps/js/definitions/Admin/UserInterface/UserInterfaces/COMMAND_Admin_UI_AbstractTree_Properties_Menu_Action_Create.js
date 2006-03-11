/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_AbstractTree_Properties_Menu_Action_Create
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
  addProperty("TargetConnectAttribute", "Admin_Property/Abstract");
  addProperty("TargetCreateType",       "Admin_Property");
  addProperty("TargetMode",             "create");
  addTargetForm("Admin_PropertyForm");
}

/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_CommandTree_Links
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
  addProperty("TargetExpand",           "Admin_UI_Link\\From");
  addProperty("TargetMode",             "view");
  addProperty("TargetShowCheckBoxes",   "true");
  addTargetMenu("Admin_UI_CommandTree_Links_Menu");
  addTargetTable("Admin_UI_LinkList4Links");
}

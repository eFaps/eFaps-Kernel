/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_ImageMyDesk
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
  addProperty("TargetQueryTypes",       "Admin_UI_Image");
  addProperty("TargetShowCheckBoxes",   "true");
  addIcon("Admin_UI_ImageImage");
  addTargetMenu("Admin_UI_ImageMyDesk_Menu");
  addTargetTable("Admin_UI_FileList");
}

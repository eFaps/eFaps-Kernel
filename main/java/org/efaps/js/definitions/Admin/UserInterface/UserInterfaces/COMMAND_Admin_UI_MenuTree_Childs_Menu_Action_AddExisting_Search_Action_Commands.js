/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_UI_MenuTree_Childs_Menu_Action_AddExisting_Search_Action_Commands
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
  addProperty("ConnectChildAttribute",  "ToCommand");
  addProperty("ConnectParentAttribute", "FromMenu");
  addProperty("ConnectType",            "Admin_UI_Menu2Command");
  addProperty("ResultTable",            "Admin_UI_AbstractList");
  addProperty("SearchForm",             "Admin_UI_AbstractForm");
  addProperty("SearchType",             "Admin_UI_Command");
  addProperty("TargetMode",             "connect");
}

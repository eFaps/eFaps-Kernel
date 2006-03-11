/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_User_PersonTree_Roles_Menu_Action_AddExisting_Search_Action_Roles
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
  addProperty("ConnectChildAttribute",  "UserToLink");
  addProperty("ConnectParentAttribute", "UserFromLink");
  addProperty("ConnectType",            "Admin_User_Person2Role");
  addProperty("ResultTable",            "Admin_User_AbstractList");
  addProperty("SearchForm",             "Admin_User_AbstractForm");
  addProperty("SearchType",             "Admin_User_Role");
  addProperty("TargetMode",             "connect");
}

/*******************************************************************************
* Admin_UI_Command:
* ~~~~~~~~~~~~~~~~~
* Admin_DataModel_SQLTableTree_SQLTables
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
  addProperty("TargetExpand",           "Admin_DataModel_SQLTable\\DMTableMain");
  addProperty("TargetMode",             "view");
  addIcon("eFapsAdminDataModelSQLTableList");
  addTargetTable("Admin_DataModel_SQLTableList");
}

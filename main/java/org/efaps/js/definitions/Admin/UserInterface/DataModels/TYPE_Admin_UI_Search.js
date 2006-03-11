/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Search
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

with (TYPE)  {
  setParentType(new Type("Admin_UI_Menu"));
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=Admin_UI_SearchImage");
  addProperty("Tree",                   "Admin_UI_SearchTree");
}

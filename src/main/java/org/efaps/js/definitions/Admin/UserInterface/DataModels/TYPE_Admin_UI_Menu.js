/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Menu
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
  setParentType(new Type("Admin_UI_Command"));
  addProperty("Form",                   "Admin.UI.MenuForm");
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=Admin_UI_MenuImage");
  addProperty("Tree",                   "Admin_UI_MenuTree");
}

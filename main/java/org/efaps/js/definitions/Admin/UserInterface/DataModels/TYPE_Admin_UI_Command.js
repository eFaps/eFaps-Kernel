/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Command
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
  setParentType(new Type("Admin_UI_Direct"));
  addProperty("Form",                   "Admin.UI.CommandForm");
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=Admin_UI_CommandImage");
  addProperty("Tree",                   "Admin_UI_CommandTree");
}

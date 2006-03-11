/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_User_Role
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
  setParentType(new Type("Admin_User_Abstract"));
  addProperty("Form",                   "Admin_User_AbstractForm");
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=eFapsAdminUserRole");
  addProperty("Tree",                   "Admin_User_RoleTree");
}

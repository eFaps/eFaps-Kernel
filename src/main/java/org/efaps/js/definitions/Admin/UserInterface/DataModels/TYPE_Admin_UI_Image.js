/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Image
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
  setParentType(new Type("Admin_UI_File"));
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=Admin_UI_ImageImage");
  addProperty("Tree",                   "Admin_UI_ImageTree");
}

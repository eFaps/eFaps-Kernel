/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Field
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
  setParentType(new Type("Admin_Abstract"));
  with (addAttribute("Collection"))  {
    setAttributeType("Link");
    setTypeLink("Admin_UI_Collection");
    setSQLTable("Admin_UI_FieldTable");
    setSQLColumn("COLLECTION");
  }
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=Admin_UI_FieldImage");
  addProperty("Tree",                   "Admin_UI_FieldTree");
}
